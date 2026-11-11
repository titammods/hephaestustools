package com.titammods.hephaestus_tools.compat.neovitae;

import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.table.ToolRole;
import com.titammods.hephaestus_tools.table.ToolUpgrade;
import com.titammods.hephaestus_tools.tools.nbt.ToolConstructionData;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;
import java.util.List;

public final class StaffModifiers {
    public static final Identifier BLOOD_SURGE = id("blood_surge");
    public static final Identifier QUICKENING = id("quickening");
    public static final Identifier THRIFT = id("thrift");
    public static final Identifier FAR_REACH = id("far_reach");
    public static final Identifier IMPALE = id("impale");
    public static final Identifier VAMPIRIC = id("vampiric");
    public static final Identifier CORROSIVE = id("corrosive_infusion");
    public static final Identifier DESTRUCTIVE = id("destructive_infusion");
    public static final Identifier STEADFAST = id("steadfast_infusion");
    public static final Identifier VENGEFUL = id("vengeful_infusion");

    public static final List<Identifier> INFUSIONS = List.of(CORROSIVE, DESTRUCTIVE, STEADFAST, VENGEFUL);
    public static final List<Identifier> ALL = List.of(BLOOD_SURGE, QUICKENING, THRIFT, FAR_REACH, IMPALE, VAMPIRIC,
            CORROSIVE, DESTRUCTIVE, STEADFAST, VENGEFUL);

    private StaffModifiers() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, path);
    }

    public static int level(ItemStack staff, Identifier modifier) {
        if (!ToolStack.isInitialized(staff)) return 0;
        for (ToolConstructionData.ModifierEntry entry : ToolStack.getModifiers(staff)) {
            if (entry.id().equals(modifier)) return entry.level();
        }
        return 0;
    }

    public static int infusion(ItemStack staff) {
        for (int i = 0; i < INFUSIONS.size(); i++) {
            if (level(staff, INFUSIONS.get(i)) > 0) return i;
        }
        return -1;
    }

    public static void addUpgrades(List<ToolUpgrade> registry) {
        registry.add(upgrade(BLOOD_SURGE, 5, vitae("raw_crystal_shard"), 2));
        registry.add(upgrade(QUICKENING, 3, Items.AMETHYST_SHARD, 4));
        registry.add(upgrade(THRIFT, 5, vitae("raw_spiritus"), 2));
        registry.add(upgrade(FAR_REACH, 3, Items.ENDER_PEARL, 2));
        registry.add(upgrade(IMPALE, 3, vitae("throwing_dagger"), 2));
        registry.add(upgrade(VAMPIRIC, 3, vitae("tainted_flesh"), 2));
        registry.add(upgrade(CORROSIVE, 3, vitae("spiritus_ruina_shard"), 2));
        registry.add(upgrade(DESTRUCTIVE, 3, vitae("spiritus_nihilum_shard"), 2));
        registry.add(upgrade(STEADFAST, 3, vitae("spiritus_invictus_shard"), 2));
        registry.add(upgrade(VENGEFUL, 3, vitae("spiritus_vindicta_shard"), 2));
    }

    private static ToolUpgrade upgrade(Identifier id, int maxLevel, Item cost, int baseCost) {
        return new ToolUpgrade(id, "upgrade.hephaestus_tools." + id.getPath(), EnumSet.of(ToolRole.MAGIC),
                maxLevel, cost, baseCost,
                Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID,
                        "textures/gui/forge_table/modifier_icon/modifier_" + id.getPath() + ".png"));
    }

    private static Item vitae(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(NeoVitaeCompat.MOD_ID, path));
    }
}
