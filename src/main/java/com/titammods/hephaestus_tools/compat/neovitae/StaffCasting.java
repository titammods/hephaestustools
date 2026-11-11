package com.titammods.hephaestus_tools.compat.neovitae;

import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.neovitae.api.sigil.SigilType;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.spiritus.IPlayerSpiritusHandler;
import com.breakinblocks.neovitae.api.stream.StreamEffect;
import com.breakinblocks.neovitae.api.stream.StreamPresets;
import com.breakinblocks.neovitae.client.particle.ColoredParticleOptions;
import com.breakinblocks.neovitae.common.NVSounds;
import com.breakinblocks.neovitae.common.attribute.NVAttributes;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.sigil.SigilItem;
import com.breakinblocks.neovitae.common.particle.NVParticles;
import com.breakinblocks.neovitae.registry.SigilTypeRegistry;
import com.titammods.hephaestus_tools.effect.HitDetails;
import com.titammods.hephaestus_tools.effect.ToolEffects;
import com.titammods.hephaestus_tools.effect.WearCause;
import com.titammods.hephaestus_tools.table.MasteryLevel;
import com.titammods.hephaestus_tools.table.ToolMastery;
import com.titammods.hephaestus_tools.table.ToolXp;
import com.titammods.hephaestus_tools.tools.helper.ToolDurability;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StaffCasting {
    private static final int BOLT_COST = 150;
    private static final double BOLT_RANGE = 28.0;
    private static final double SIGIL_RANGE = 24.0;
    private static final double NOVA_RADIUS = 4.0;
    private static final double VIEW_RANGE = 48.0;
    private static final int DRAIN_INTERVAL = 10;
    private static final int COOLDOWN = 10;
    private static final int BLOOD = 0xB5121F;
    private static final int SIGIL = 0xFF6A1F;
    private static final int[] SPIRITUS = {0x4FE3F0, 0x4BD66B, 0xF07A2A, 0xC8A2FF, 0xE8323C};
    private static final int[] INFUSION = {0x4BD66B, 0xF07A2A, 0xC8A2FF, 0xE8323C};

    private StaffCasting() {}

    private static int tier(ItemStack staff, String path) {
        return ToolMastery.selected(staff).equals(path) ? MasteryLevel.tier(staff) : 0;
    }

    private static int mod(ItemStack staff, Identifier modifier) {
        return StaffModifiers.level(staff, modifier);
    }

    private static @Nullable SigilType sigil(ServerLevel level, ItemStack staff) {
        if (!StaffItem.hasSigil(staff)) return null;
        Identifier id = StaffItem.sigilType(staff);
        if (id == null) return null;
        var registry = level.registryAccess().lookup(SigilTypeRegistry.SIGIL_TYPE_KEY);
        if (registry.isEmpty()) return null;
        return registry.get().get(ResourceKey.create(SigilTypeRegistry.SIGIL_TYPE_KEY, id))
                .map(Holder.Reference::value).orElse(null);
    }

    private static int color(Player player, ItemStack staff) {
        if (StaffItem.hasSigil(staff)) return SIGIL;
        int infusion = StaffModifiers.infusion(staff);
        if (infusion >= 0) return INFUSION[infusion];
        if (tier(staff, StaffItem.SOUL_REAVER) > 0) {
            IPlayerSpiritusHandler spiritus = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler();
            SpiritusType type = spiritus.getLargestSpiritusType(player);
            if (type != null && spiritus.getTotalSpiritus(type, player) >= 1.0) return SPIRITUS[type.ordinal() % SPIRITUS.length];
        }
        return BLOOD;
    }

    private static double range(ItemStack staff, double base) {
        return base + 8.0 * mod(staff, StaffModifiers.FAR_REACH);
    }

    private static void sound(ServerLevel level, Player player, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void particles(ServerLevel level, ParticleType<ColoredParticleOptions> type, int color,
                                  double x, double y, double z, int count, double spread, double speed) {
        level.sendParticles(new ColoredParticleOptions(type, color), x, y, z, count, spread, spread, spread, speed);
    }

    private static Vec3 tip(Player player) {
        return player.getEyePosition().add(player.getLookAngle().scale(0.8)).add(0.0, -0.15, 0.0);
    }

    private static Vec3 center(LivingEntity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
    }

    static void imprint(ServerLevel level, Player player, ItemStack staff) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty()) {
            if (StaffItem.hasSigil(staff)) {
                StaffItem.setSigil(staff, null, null);
                sound(level, player, NVSounds.SIGIL_TOGGLE_OFF.get(), 0.6f, 1.0f);
                player.sendOverlayMessage(Component.translatable("message.hephaestus_tools.staff.cleared"));
                return;
            }
            String form = StaffItem.cycleForm(staff);
            Vec3 tip = tip(player);
            sound(level, player, NVSounds.SIGIL_TOGGLE_ON.get(), 0.5f, 1.3f);
            particles(level, NVParticles.RUNE_GLOW.get(), color(player, staff), tip.x, tip.y, tip.z, 8, 0.2, 0.02);
            player.sendOverlayMessage(Component.translatable("tooltip.hephaestus_tools.staff.spell", StaffItem.formName(form)));
            return;
        }
        if (!(offhand.getItem() instanceof SigilItem sigilItem)) return;
        SigilType type = sigilItem.getSigilType(offhand, level);
        var registry = level.registryAccess().lookup(SigilTypeRegistry.SIGIL_TYPE_KEY);
        if (type == null || registry.isEmpty()) return;
        Identifier typeId = registry.get().listElements()
                .filter(reference -> reference.value() == type)
                .map(reference -> reference.key().identifier())
                .findFirst().orElse(null);
        if (typeId == null) return;
        StaffItem.setSigil(staff, typeId, BuiltInRegistries.ITEM.getKey(offhand.getItem()));
        sound(level, player, NVSounds.ALCHEMY_ARRAY_CRAFT.get(), 0.7f, 1.2f);
        Vec3 tip = tip(player);
        particles(level, NVParticles.RUNE_GLOW.get(), SIGIL, tip.x, tip.y, tip.z, 14, 0.25, 0.02);
        StreamEffect.builder(player).to(tip.x, tip.y, tip.z)
                .color(SIGIL).scale(0.05f).speed(2.5f).gravity(0.0f)
                .spiralInto(true).spiralRadius(0.3f).spiralSpeed(0.5f).approachHeight(0.4f)
                .alphaStart(0.2f).alphaEnd(0.9f).glow(true)
                .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
        player.sendOverlayMessage(Component.translatable("message.hephaestus_tools.staff.imprinted",
                Component.translatable(offhand.getItem().getDescriptionId())));
    }

    static void begin(ServerLevel level, Player player) {
        sound(level, player, NVSounds.SIGIL_TOGGLE_ON.get(), 0.5f, 0.8f);
    }

    static void chargeTick(ServerLevel level, Player player, ItemStack staff, int held) {
        int charge = StaffItem.chargeTicks(staff);
        int color = color(player, staff);
        double x = player.getX(), y = player.getY(), z = player.getZ();

        if (held % 2 == 0) {
            double base = held * 0.3;
            for (int i = 0; i < 3; i++) {
                double angle = base + i * (Math.PI * 2.0 / 3.0);
                particles(level, NVParticles.RUNE_GLOW.get(), color,
                        x + Math.cos(angle) * 1.2, y + 0.1, z + Math.sin(angle) * 1.2, 1, 0.0, 0.0);
            }
        }

        if (held < charge) {
            if (held % 3 == 0) {
                double angle = held * 0.55;
                double height = held % 6 == 0 ? 2.4 : 0.2;
                StreamEffect.builder(x + Math.cos(angle) * 2.4, y + height, z + Math.sin(angle) * 2.4)
                        .toTracked(player)
                        .color(color).scale(0.05f).speed(2.4f).gravity(0.0f)
                        .wobble(0.03f).wobbleFrequency(0.8f)
                        .spiralInto(true).spiralRadius(0.8f).spiralSpeed(0.5f).approachHeight(1.2f)
                        .alphaStart(0.1f).alphaEnd(0.9f).glow(true)
                        .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
            }
            if (held % 4 == 0) {
                particles(level, NVParticles.BLOOD_FLAME.get(), color, x, y + 0.4, z, 3, 0.5, 0.02);
            }
            return;
        }

        Vec3 tip = tip(player);
        if (held == charge) {
            sound(level, player, NVSounds.ALCHEMY_ARRAY_ACTIVATE.get(), 0.6f, 1.4f);
            particles(level, NVParticles.BLOOD_GLOW.get(), color, tip.x, tip.y, tip.z, 16, 0.2, 0.03);
        }
        if ((held - charge) % 8 == 0) {
            StreamPresets.emberMote(tip.x, tip.y, tip.z).color(color).scale(0.14f).lifetime(10)
                    .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
        }

        SigilType type = sigil(level, staff);
        if (type != null) {
            channelSigil(level, player, staff, type, held - charge);
        } else if (StaffItem.form(staff).equals("drain")) {
            channelDrain(level, player, staff, held - charge, color);
        }
    }

    private static void channelSigil(ServerLevel level, Player player, ItemStack staff, SigilType type, int elapsed) {
        if (!type.isToggleable()) return;
        ISigilEffect effect = type.effect().orElse(null);
        if (effect == null) return;
        if (elapsed % Math.max(1, type.drainInterval()) == 0
                && !pay(player, staff, type.getCostForContext(SigilType.UseContext.ACTIVE))) {
            player.stopUsingItem();
            return;
        }
        effect.activeTick(level, player, staff, player.getInventory().getSelectedSlot(), true);
    }

    private static void channelDrain(ServerLevel level, Player player, ItemStack staff, int elapsed, int color) {
        if (elapsed % DRAIN_INTERVAL != 0) return;
        Aim aim = aim(level, player, range(staff, BOLT_RANGE * 0.6));
        if (aim.entity() == null || !(aim.entity().getEntity() instanceof LivingEntity target)) return;
        if (!pay(player, staff, BOLT_COST * 3 / 10)) {
            player.stopUsingItem();
            return;
        }
        Power power = power(player, staff, false);
        float dealt = strike(level, player, staff, target, power.damage() * 0.35f);
        StreamEffect.builder(target).toTracked(player)
                .color(color).scale(0.07f).speed(3.0f).gravity(0.1f)
                .wobble(0.04f).wobbleFrequency(1.2f)
                .spiralInto(true).spiralRadius(0.3f).spiralSpeed(0.4f).approachHeight(0.6f)
                .alphaStart(0.3f).alphaEnd(0.95f).glow(true)
                .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
        Vec3 at = center(target);
        particles(level, NVParticles.BLOOD_DRIP.get(), color, at.x, at.y, at.z, 5, 0.25, 0.0);
        sound(level, player, NVSounds.BLOOD_SIPHON.get(), 0.4f, 0.9f);
        reward(level, player, staff, target, dealt);
        if (elapsed % (DRAIN_INTERVAL * 4) == 0) ToolDurability.hurt(staff, 1, level, player, WearCause.USE);
    }

    static void release(ServerLevel level, Player player, ItemStack staff, int held) {
        Vec3 tip = tip(player);
        if (held < StaffItem.chargeTicks(staff)) {
            sound(level, player, NVSounds.SIGIL_TOGGLE_OFF.get(), 0.4f, 0.7f);
            particles(level, NVParticles.BLOOD_DRIP.get(), BLOOD, tip.x, tip.y, tip.z, 6, 0.15, 0.0);
            return;
        }
        SigilType type = sigil(level, staff);
        String form = StaffItem.form(staff);
        if (type != null ? type.isToggleable() : form.equals("drain")) {
            sound(level, player, NVSounds.SIGIL_TOGGLE_OFF.get(), 0.5f, 1.0f);
            player.getCooldowns().addCooldown(staff, COOLDOWN);
            return;
        }
        boolean free = tier(staff, StaffItem.SIGIL_WEAVER) >= 3 && player.getRandom().nextFloat() < 0.2f;
        boolean cast = type != null ? castSigil(level, player, staff, type, free)
                : form.equals("nova") ? castNova(level, player, staff, free)
                : castBolt(level, player, staff, free);
        if (!cast) return;
        if (!free) ToolDurability.hurt(staff, 1, level, player, WearCause.USE);
        player.getCooldowns().addCooldown(staff, COOLDOWN);
    }

    private static int cost(Player player, ItemStack staff, int base) {
        double reduction = player.getAttributeValue(NVAttributes.SIGIL_COST_REDUCTION) / 100.0;
        int weaver = tier(staff, StaffItem.SIGIL_WEAVER);
        double mastery = weaver >= 3 ? 0.35 : weaver == 2 ? 0.25 : weaver == 1 ? 0.15 : 0.0;
        double thrift = 0.06 * mod(staff, StaffModifiers.THRIFT);
        return Math.max(1, (int) (base * Math.max(0.0, 1.0 - reduction) * (1.0 - mastery) * (1.0 - thrift)));
    }

    private static boolean pay(Player player, ItemStack staff, int base) {
        if (base <= 0 || player.isCreative()) return true;
        IAnima anima = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (anima == null) return false;
        return anima.syphonAndDamage(player, AnimaTicket.create(cost(player, staff, base))).success();
    }

    private record Aim(Vec3 origin, Vec3 end, BlockHitResult block, @Nullable EntityHitResult entity) {
        Vec3 point() {
            return entity != null ? entity.getLocation() : end;
        }
    }

    private static Aim aim(ServerLevel level, Player player, double range) {
        Vec3 origin = player.getEyePosition();
        Vec3 reach = origin.add(player.getLookAngle().scale(range));
        BlockHitResult block = level.clip(new ClipContext(origin, reach,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 end = block.getType() == HitResult.Type.MISS ? reach : block.getLocation();
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(player, origin, end,
                new AABB(origin, end).inflate(1.0),
                e -> !e.isSpectator() && e.isPickable() && e != player && e.isAlive(),
                0.0);
        return new Aim(origin, end, block, entity);
    }

    private static void beam(ServerLevel level, Player player, Vec3 from, Vec3 to, int color, float scale) {
        StreamEffect.builder(from.x, from.y, from.z).to(to.x, to.y, to.z)
                .color(color).scale(scale).speed(5.0f).gravity(0.0f)
                .wobble(0.01f).wobbleFrequency(2.4f)
                .spiralInto(false).approachHeight(0.0f)
                .alphaStart(0.7f).alphaEnd(0.9f).glow(true)
                .drainSpeed(4.0f).trailDensity(3).rawTrailColor(true)
                .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
    }

    private static void impact(ServerLevel level, Player player, Vec3 at, int color) {
        StreamPresets.emberMote(at.x, at.y, at.z).color(color).scale(0.22f).lifetime(12)
                .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
        particles(level, NVParticles.BLOOD_GLOW.get(), color, at.x, at.y, at.z, 12, 0.25, 0.04);
        particles(level, NVParticles.BLOOD_FLAME.get(), color, at.x, at.y, at.z, 8, 0.2, 0.03);
    }

    private static boolean castSigil(ServerLevel level, Player player, ItemStack staff, SigilType type, boolean free) {
        ISigilEffect effect = type.effect().orElse(null);
        if (effect == null) return false;
        Aim aim = aim(level, player, range(staff, SIGIL_RANGE));
        SigilType.UseContext context = SigilType.UseContext.AIR;
        boolean done = false;
        if (aim.entity() != null) {
            done = effect.useOnEntity(level, player, staff, aim.entity().getEntity());
            context = SigilType.UseContext.ENTITY;
        }
        if (!done && aim.block().getType() == HitResult.Type.BLOCK) {
            done = effect.useOnBlock(level, player, staff, aim.block().getBlockPos(),
                    aim.block().getDirection(), aim.block().getLocation());
            context = SigilType.UseContext.BLOCK;
        }
        if (!done) {
            done = effect.useOnAir(level, player, staff);
            context = SigilType.UseContext.AIR;
        }
        if (!done) {
            sound(level, player, NVSounds.SIGIL_TOGGLE_OFF.get(), 0.4f, 0.7f);
            return false;
        }
        if (!free) pay(player, staff, type.getCostForContext(context));
        Vec3 at = aim.point();
        beam(level, player, tip(player), at, SIGIL, 0.04f);
        particles(level, NVParticles.RUNE_GLOW.get(), SIGIL, at.x, at.y, at.z, 16, 0.3, 0.02);
        sound(level, player, NVSounds.SIGIL_ACTIVATE.get(), 0.6f, 1.0f);
        if (player instanceof ServerPlayer serverPlayer) ToolXp.addXp(staff, serverPlayer, 2);
        return true;
    }

    private record Power(float damage, int color, boolean empowered) {}

    private static Power power(Player player, ItemStack staff, boolean burnSpiritus) {
        int soul = tier(staff, StaffItem.SOUL_REAVER);
        int infusion = StaffModifiers.infusion(staff);
        int color = infusion >= 0 ? INFUSION[infusion] : BLOOD;
        float damage = (2.0f + ToolStack.getAttackDamage(staff)) * ToolStack.efficiency(staff);
        damage *= 1.0f + 0.15f * tier(staff, StaffItem.HEMOMANCER);
        damage *= 1.0f + 0.12f * mod(staff, StaffModifiers.BLOOD_SURGE);
        damage *= 1.0f + 0.10f * mod(staff, StaffModifiers.DESTRUCTIVE);
        boolean empowered = false;
        if (burnSpiritus && soul > 0) {
            IPlayerSpiritusHandler spiritus = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler();
            SpiritusType type = spiritus.getLargestSpiritusType(player);
            double price = soul >= 3 ? 0.5 : 1.0;
            if (type != null && spiritus.getTotalSpiritus(type, player) >= price) {
                spiritus.consumeSpiritus(type, player, price);
                empowered = true;
                if (infusion < 0) color = SPIRITUS[type.ordinal() % SPIRITUS.length];
                damage *= soul >= 3 ? 1.6f : 1.3f;
            }
        }
        return new Power(damage, color, empowered);
    }

    private static boolean castBolt(ServerLevel level, Player player, ItemStack staff, boolean free) {
        if (!free && !pay(player, staff, BOLT_COST)) {
            sound(level, player, NVSounds.SIGIL_TOGGLE_OFF.get(), 0.4f, 0.7f);
            return false;
        }
        int hemo = tier(staff, StaffItem.HEMOMANCER);
        int soul = tier(staff, StaffItem.SOUL_REAVER);
        Power power = power(player, staff, true);
        int color = power.color();
        float damage = power.damage();

        Vec3 origin = player.getEyePosition();
        Vec3 reach = origin.add(player.getLookAngle().scale(range(staff, BOLT_RANGE)));
        BlockHitResult block = level.clip(new ClipContext(origin, reach,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 end = block.getType() == HitResult.Type.MISS ? reach : block.getLocation();

        List<LivingEntity> line = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
                new AABB(origin, end).inflate(1.0),
                e -> e != player && e.isAlive() && !e.isSpectator() && e.isPickable()
                        && e.getBoundingBox().inflate(0.3).clip(origin, end).isPresent()));
        line.sort(Comparator.comparingDouble(e -> e.distanceToSqr(origin)));
        int capacity = 1 + mod(staff, StaffModifiers.IMPALE);
        List<LivingEntity> struck = new ArrayList<>(line.subList(0, Math.min(capacity, line.size())));
        Vec3 at = line.size() >= capacity && !struck.isEmpty() ? center(struck.getLast()) : end;

        beam(level, player, tip(player), at, color, power.empowered() ? 0.07f : 0.05f);
        impact(level, player, at, color);
        sound(level, player, NVSounds.BLOOD_SIPHON.get(), 0.7f, power.empowered() ? 0.7f : 1.2f);
        if (struck.isEmpty()) return true;

        float dealt = 0f;
        float falloff = 1.0f;
        for (LivingEntity target : struck) {
            dealt += strike(level, player, staff, target, damage * falloff);
            Vec3 point = center(target);
            particles(level, NVParticles.BLOOD_GLOW.get(), color, point.x, point.y, point.z, 6, 0.2, 0.03);
            falloff *= 0.8f;
        }
        LivingEntity last = struck.getLast();

        if (hemo >= 2) {
            LivingEntity previous = last;
            for (int i = 0; i < hemo - 1; i++) {
                LivingEntity from = previous;
                LivingEntity next = nearby(level, player, from, 5.0).stream()
                        .filter(e -> !struck.contains(e))
                        .min(Comparator.comparingDouble(e -> e.distanceToSqr(from)))
                        .orElse(null);
                if (next == null) break;
                beam(level, player, center(from), center(next), color, 0.04f);
                dealt += strike(level, player, staff, next, damage * 0.5f);
                struck.add(next);
                previous = next;
            }
        }

        if (soul >= 2 && power.empowered()) {
            double radius = soul >= 3 ? 4.0 : 3.0;
            for (LivingEntity other : nearby(level, player, last, radius)) {
                if (struck.contains(other)) continue;
                dealt += strike(level, player, staff, other, damage * 0.4f);
                Vec3 point = center(other);
                particles(level, NVParticles.BLOOD_FLAME.get(), color, point.x, point.y, point.z, 6, 0.2, 0.03);
            }
            StreamPresets.soulWisp(at.x, at.y, at.z).color(color).scale((float) radius * 0.12f).lifetime(14)
                    .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
        }

        reward(level, player, staff, last, dealt);
        return true;
    }

    private static boolean castNova(ServerLevel level, Player player, ItemStack staff, boolean free) {
        if (!free && !pay(player, staff, BOLT_COST * 3 / 2)) {
            sound(level, player, NVSounds.SIGIL_TOGGLE_OFF.get(), 0.4f, 0.7f);
            return false;
        }
        Power power = power(player, staff, true);
        int color = power.color();
        double radius = NOVA_RADIUS + 1.5 * mod(staff, StaffModifiers.FAR_REACH);
        Vec3 from = center(player);

        for (int i = 0; i < 16; i++) {
            double angle = i * (Math.PI * 2.0 / 16.0);
            double px = player.getX() + Math.cos(angle) * radius;
            double pz = player.getZ() + Math.sin(angle) * radius;
            particles(level, NVParticles.RUNE_GLOW.get(), color, px, player.getY() + 0.2, pz, 2, 0.1, 0.01);
            if (i % 2 == 0) beam(level, player, from, new Vec3(px, player.getY() + 0.6, pz), color, 0.045f);
        }
        StreamPresets.emberMote(from.x, from.y, from.z).color(color).scale(0.4f).lifetime(10)
                .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
        sound(level, player, NVSounds.RITUAL_ACTIVATE.get(), 0.5f, 1.5f);

        float dealt = 0f;
        LivingEntity first = null;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                e -> e != player && e.isAlive() && !(e instanceof Player) && !e.isAlliedTo(player)
                        && e.distanceToSqr(player) <= radius * radius)) {
            dealt += strike(level, player, staff, target, power.damage() * 0.6f);
            Vec3 point = center(target);
            particles(level, NVParticles.BLOOD_FLAME.get(), color, point.x, point.y, point.z, 8, 0.25, 0.03);
            if (first == null) first = target;
        }
        if (first != null) reward(level, player, staff, first, dealt);
        return true;
    }

    private static void reward(ServerLevel level, Player player, ItemStack staff, LivingEntity source, float dealt) {
        if (dealt <= 0f) return;
        int vampiric = mod(staff, StaffModifiers.VAMPIRIC);
        if (vampiric > 0) player.heal(dealt * 0.05f * vampiric);
        if (tier(staff, StaffItem.HEMOMANCER) >= 3) {
            IAnima anima = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
            if (anima != null) anima.add(AnimaTicket.create((int) (dealt * 8f)), Integer.MAX_VALUE);
        }
        if (vampiric > 0 || tier(staff, StaffItem.HEMOMANCER) >= 3) {
            StreamEffect.builder(source).toTracked(player)
                    .color(0x990011).scale(0.05f).speed(2.0f).gravity(0.25f)
                    .wobble(0.03f).wobbleFrequency(0.8f)
                    .spiralInto(true).spiralRadius(0.35f).spiralSpeed(0.35f)
                    .approachHeight(1.0f).alphaStart(0.08f).alphaEnd(0.95f)
                    .build().sendToNearby(level, player.blockPosition(), VIEW_RANGE);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ToolXp.addXp(staff, serverPlayer, Math.max(1, (int) (dealt / 3f)));
        }
    }

    private static List<LivingEntity> nearby(ServerLevel level, Player player, LivingEntity center, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, center.getBoundingBox().inflate(radius),
                e -> e != player && e != center && e.isAlive() && !(e instanceof Player) && !e.isAlliedTo(player));
    }

    private static float strike(ServerLevel level, Player player, ItemStack staff, LivingEntity target, float base) {
        float amount = ToolEffects.modifyDamage(player, target, staff, base);
        if (amount <= 0f) return 0f;
        if (!target.hurtServer(level, player.damageSources().indirectMagic(player, player), amount)) return 0f;
        ToolEffects.afterHit(player, target, staff, new HitDetails(amount, false, 1.0f));
        infuse(player, staff, target);
        return amount;
    }

    private static void infuse(Player player, ItemStack staff, LivingEntity target) {
        int corrosive = mod(staff, StaffModifiers.CORROSIVE);
        if (corrosive > 0) target.addEffect(new MobEffectInstance(MobEffects.POISON, 40 + 40 * corrosive, corrosive - 1), player);
        int destructive = mod(staff, StaffModifiers.DESTRUCTIVE);
        if (destructive > 0) target.igniteForSeconds(2 * destructive);
        int steadfast = mod(staff, StaffModifiers.STEADFAST);
        if (steadfast > 0) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, steadfast - 1));
        int vengeful = mod(staff, StaffModifiers.VENGEFUL);
        if (vengeful > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40 + 20 * vengeful, vengeful - 1), player);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40 + 20 * vengeful, vengeful - 1), player);
        }
    }
}
