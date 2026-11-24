package com.titammods.hephaestus_tools.client.renderer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.client.ToolColorHandler;
import com.titammods.hephaestus_tools.client.model.ModifierModelRegistry;
import com.titammods.hephaestus_tools.config.HephaestusConfig;
import com.titammods.hephaestus_tools.materials.MaterialAnimation;
import com.titammods.hephaestus_tools.tools.nbt.ToolConstructionData;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ToolItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final Cache<TextureAtlasSprite, List<BakedQuad>> QUAD_CACHE =
            CacheBuilder.newBuilder().maximumSize(256).expireAfterWrite(5, TimeUnit.MINUTES).build();

    private static final Map<String, List<String>> TOOL_LAYERS = Map.ofEntries(
            Map.entry("pickaxe", List.of(
                    "item/tool/pickaxe/handle",
                    "item/tool/pickaxe/head",
                    "item/tool/pickaxe/binding")),
            Map.entry("sledge_hammer", List.of(
                    "item/tool/sledge_hammer/handle",
                    "item/tool/sledge_hammer/head",
                    "item/tool/sledge_hammer/front",
                    "item/tool/sledge_hammer/back")),
            Map.entry("vein_hammer", List.of(
                    "item/tool/vein_hammer/handle",
                    "item/tool/vein_hammer/head",
                    "item/tool/vein_hammer/front",
                    "item/tool/vein_hammer/grip")),
            Map.entry("mattock", List.of(
                    "item/tool/mattock/handle",
                    "item/tool/mattock/axe",
                    "item/tool/mattock/pick")),
            Map.entry("paxel", List.of(
                    "item/tool/paxel/handle",
                    "item/tool/paxel/head")),
            Map.entry("excavator", List.of(
                    "item/tool/excavator/handle",
                    "item/tool/excavator/grip",
                    "item/tool/excavator/binding",
                    "item/tool/excavator/head")),
            Map.entry("hand_axe", List.of(
                    "item/tool/hand_axe/handle",
                    "item/tool/hand_axe/head",
                    "item/tool/hand_axe/binding")),
            Map.entry("broad_axe", List.of(
                    "item/tool/broad_axe/handle",
                    "item/tool/broad_axe/blade",
                    "item/tool/broad_axe/back",
                    "item/tool/broad_axe/binding")),
            Map.entry("kama", List.of(
                    "item/tool/kama/handle",
                    "item/tool/kama/head",
                    "item/tool/kama/binding")),
            Map.entry("scythe", List.of(
                    "item/tool/scythe/handle",
                    "item/tool/scythe/accessory",
                    "item/tool/scythe/binding",
                    "item/tool/scythe/head")),
            Map.entry("dagger", List.of(
                    "item/tool/dagger/handle",
                    "item/tool/dagger/guard",
                    "item/tool/dagger/crossguard",
                    "item/tool/dagger/blade")),
            Map.entry("sword", List.of(
                    "item/tool/sword/handle",
                    "item/tool/sword/blade",
                    "item/tool/sword/guard")),
            Map.entry("cleaver", List.of(
                    "item/tool/cleaver/handle",
                    "item/tool/cleaver/head",
                    "item/tool/cleaver/shield",
                    "item/tool/cleaver/guard"))
    );

    private static final Map<String, List<String>> WORKBENCH_LAYERS = Map.ofEntries(
            Map.entry("hand_axe", List.of(
                    "item/tool/hand_axe/handle",
                    "item/tool/hand_axe/head",
                    "item/tool/hand_axe/binding"))
    );

    private static int layerMask = -1;

    public static void setLayerMask(int mask) {
        layerMask = mask;
    }

    public static List<String> workbenchLayersFor(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return null;
        List<String> layers = WORKBENCH_LAYERS.get(id.getPath());
        return layers != null ? layers : TOOL_LAYERS.get(id.getPath());
    }

    public static Map<String, List<String>> toolLayers() {
        return TOOL_LAYERS;
    }

    public static int layerCount(Item item) {
        List<String> layers = layersFor(item);
        return layers == null ? 0 : layers.size();
    }

    public static List<String> layersFor(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return null;
        return TOOL_LAYERS.get(id.getPath());
    }

    private static List<String> styledLayersFor(Item item) {
        return HephaestusConfig.modernInterface() ? workbenchLayersFor(item) : layersFor(item);
    }

    private static ResourceLocation png(String path) {
        return ResourceLocation.fromNamespaceAndPath(HephaestusTools.MOD_ID, "textures/" + path + ".png");
    }

    private static ResourceLocation pngOf(ResourceLocation texture) {
        return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "textures/" + texture.getPath() + ".png");
    }

    public static void renderGuiPreview(GuiGraphics graphics,
                                        ItemStack stack, int x, int y, int size, float alpha) {
        List<String> layers = layersFor(stack.getItem());
        if (layers == null) return;
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            for (int i = 0; i < layers.size(); i++) {
                int color = ToolColorHandler.INSTANCE.getAnimatedColor(stack, i);
                graphics.setColor(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f,
                        (color & 255) / 255f, alpha);
                graphics.blit(png(layers.get(i)), x, y, size, size, 0, 0, 16, 16, 16, 16);
            }
            graphics.setColor(1f, 1f, 1f, alpha);
            for (ResourceLocation overlay : modifierTextures(stack)) {
                graphics.blit(overlay, x, y, size, size, 0, 0, 16, 16, 16, 16);
            }
        } finally {
            graphics.setColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }
    }

    public static void renderGuiPreviewTinted(GuiGraphics graphics,
                                              ItemStack stack, int x, int y, int size, int argb) {
        List<String> layers = workbenchLayersFor(stack.getItem());
        if (layers == null) return;
        int ta = argb >>> 24 & 255, tr = argb >> 16 & 255, tg = argb >> 8 & 255, tb = argb & 255;
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            for (int i = 0; i < layers.size(); i++) {
                int base = ToolColorHandler.INSTANCE.getAnimatedColor(stack, i);
                int r = (base >> 16 & 255) * tr / 255, g = (base >> 8 & 255) * tg / 255, b = (base & 255) * tb / 255;
                graphics.setColor(r / 255f, g / 255f, b / 255f, ta / 255f);
                graphics.blit(png(layers.get(i)), x, y, size, size, 0, 0, 16, 16, 16, 16);
            }
            graphics.setColor(tr / 255f, tg / 255f, tb / 255f, ta / 255f);
            for (ResourceLocation overlay : modifierTextures(stack)) {
                graphics.blit(overlay, x, y, size, size, 0, 0, 16, 16, 16, 16);
            }
        } finally {
            graphics.setColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }
    }

    private static final Map<ResourceLocation, Boolean> TEXTURE_EXISTS = new ConcurrentHashMap<>();

    private static final float DESATURATE = 0.35f;
    private static final float HANDLE_DARKEN = 0.85f;

    public static int adjustColor(int argb, boolean handle) {
        int a = argb >>> 24 & 255;
        int[] c = {argb >> 16 & 255, argb >> 8 & 255, argb & 255};
        float lum = 0.299f * c[0] + 0.587f * c[1] + 0.114f * c[2];
        for (int i = 0; i < 3; i++) {
            float v = c[i] + (lum - c[i]) * DESATURATE;
            if (handle) v *= HANDLE_DARKEN;
            c[i] = Math.max(0, Math.min(255, Math.round(v)));
        }
        return a << 24 | c[0] << 16 | c[1] << 8 | c[2];
    }

    private static boolean exists(ResourceLocation png) {
        return TEXTURE_EXISTS.computeIfAbsent(png,
                path -> Minecraft.getInstance().getResourceManager().getResource(path).isPresent());
    }

    public static void clearTextureCache() {
        TEXTURE_EXISTS.clear();
        QUAD_CACHE.invalidateAll();
    }

    private static List<ResourceLocation> modifierTextures(ItemStack stack) {
        List<ResourceLocation> out = new ArrayList<>();
        if (!ToolStack.isInitialized(stack)) return out;
        for (ToolConstructionData.ModifierEntry mod : ToolStack.getModifiers(stack)) {
            if (mod.level() <= 0) continue;
            for (ResourceLocation tex : ModifierModelRegistry.candidates(stack.getItem(), mod.id())) {
                ResourceLocation png = pngOf(tex);
                if (exists(png)) {
                    out.add(png);
                    break;
                }
            }
        }
        return out;
    }

    public ToolItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    private static List<BakedQuad> bakeQuadsForSprite(TextureAtlasSprite sprite) {
        var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0, sprite, null);
        return UnbakedGeometryHelper.bakeElements(
                unbaked, m -> sprite, new SimpleModelState(Transformation.identity()));
    }

    private static List<BakedQuad> getQuads(TextureAtlasSprite sprite) {
        try {
            return QUAD_CACHE.get(sprite, () -> bakeQuadsForSprite(sprite));
        } catch (ExecutionException e) {
            return List.of();
        }
    }

    private static boolean isMissing(Function<ResourceLocation, TextureAtlasSprite> atlas,
                                     TextureAtlasSprite sprite) {
        return sprite == atlas.apply(MissingTextureAtlasSprite.getLocation());
    }

    private static void renderSprite(TextureAtlasSprite sprite, VertexConsumer vc, PoseStack pose,
                                     int light, int overlay, int argb) {
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        if (a <= 0f) a = 1f;
        for (BakedQuad quad : getQuads(sprite)) {
            vc.putBulkData(pose.last(), quad, r, g, b, a, light, overlay);
        }
    }

    private static final float FRONT_Z = 8.5f / 16f;
    private static final float BACK_Z = 7.5f / 16f;
    private static final float LAYER_STEP = 1f / 2048f;

    private static void renderAnimatedSprite(TextureAtlasSprite sprite, VertexConsumer vc, PoseStack pose,
                                             int light, int overlay, int argb, MaterialAnimation animation,
                                             float ticks, float lift) {
        SpriteContents contents = sprite.contents();
        int w = contents.width(), h = Math.min(contents.height(), w);
        if (w <= 0 || h <= 0) return;
        boolean[][] p = new boolean[w][h];
        for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) p[x][y] = !contents.isTransparent(0, x, y);
        if ((argb >>> 24) == 0) argb |= 0xFF000000;
        PoseStack.Pose last = pose.last();
        float zf = FRONT_Z + lift, zb = BACK_Z - lift;
        for (int px = 0; px < w; px++) for (int py = 0; py < h; py++) {
            if (!p[px][py]) continue;
            int c = animation.pixel(argb, ticks, px, py, w, h);
            int a = c >>> 24 & 255, r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255;
            float x0 = (float) px / w, x1 = (float) (px + 1) / w;
            float y0 = 1f - (float) (py + 1) / h, y1 = 1f - (float) py / h;
            float u0 = sprite.getU((px + 0.25f) / w), u1 = sprite.getU((px + 0.75f) / w);
            float v0 = sprite.getV((py + 0.25f) / h), v1 = sprite.getV((py + 0.75f) / h);

            vertex(vc, last, x0, y0, zf, u0, v1, 0f, 0f, 1f, r, g, b, a, light, overlay);
            vertex(vc, last, x1, y0, zf, u1, v1, 0f, 0f, 1f, r, g, b, a, light, overlay);
            vertex(vc, last, x1, y1, zf, u1, v0, 0f, 0f, 1f, r, g, b, a, light, overlay);
            vertex(vc, last, x0, y1, zf, u0, v0, 0f, 0f, 1f, r, g, b, a, light, overlay);

            vertex(vc, last, x0, y0, zb, u0, v1, 0f, 0f, -1f, r, g, b, a, light, overlay);
            vertex(vc, last, x0, y1, zb, u0, v0, 0f, 0f, -1f, r, g, b, a, light, overlay);
            vertex(vc, last, x1, y1, zb, u1, v0, 0f, 0f, -1f, r, g, b, a, light, overlay);
            vertex(vc, last, x1, y0, zb, u1, v1, 0f, 0f, -1f, r, g, b, a, light, overlay);

            if (!solid(p, px - 1, py)) {
                vertex(vc, last, x0, y0, zb, u0, v1, -1f, 0f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x0, y0, zf, u1, v1, -1f, 0f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x0, y1, zf, u1, v0, -1f, 0f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x0, y1, zb, u0, v0, -1f, 0f, 0f, r, g, b, a, light, overlay);
            }
            if (!solid(p, px + 1, py)) {
                vertex(vc, last, x1, y0, zb, u0, v1, 1f, 0f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x1, y1, zb, u0, v0, 1f, 0f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x1, y1, zf, u1, v0, 1f, 0f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x1, y0, zf, u1, v1, 1f, 0f, 0f, r, g, b, a, light, overlay);
            }
            if (!solid(p, px, py + 1)) {
                vertex(vc, last, x0, y0, zb, u0, v1, 0f, -1f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x1, y0, zb, u1, v1, 0f, -1f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x1, y0, zf, u1, v0, 0f, -1f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x0, y0, zf, u0, v0, 0f, -1f, 0f, r, g, b, a, light, overlay);
            }
            if (!solid(p, px, py - 1)) {
                vertex(vc, last, x0, y1, zb, u0, v1, 0f, 1f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x0, y1, zf, u0, v0, 0f, 1f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x1, y1, zf, u1, v0, 0f, 1f, 0f, r, g, b, a, light, overlay);
                vertex(vc, last, x1, y1, zb, u1, v1, 0f, 1f, 0f, r, g, b, a, light, overlay);
            }
        }
    }

    private static boolean solid(boolean[][] p, int x, int y) {
        return x >= 0 && y >= 0 && x < p.length && y < p[x].length && p[x][y];
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int r, int g, int b, int a, int light, int overlay) {
        vc.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void expandIfNotGui(ItemDisplayContext ctx, PoseStack pose) {
        if (ctx != ItemDisplayContext.GUI) {
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.scale(1.001f, 1.001f, 1.001f);
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose,
                             MultiBufferSource buffer, int light, int overlay) {
        Item item = stack.getItem();
        List<String> layers = styledLayersFor(item);
        if (layers == null) return;

        Function<ResourceLocation, TextureAtlasSprite> atlas =
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        var itemColors = Minecraft.getInstance().getItemColors();

        pose.pushPose();
        VertexConsumer vc = buffer.getBuffer(RenderType.cutout());

        for (int i = 0; i < layers.size(); i++) {
            if ((layerMask & (1 << i)) == 0) continue;
            TextureAtlasSprite sprite = atlas.apply(
                    ResourceLocation.fromNamespaceAndPath(HephaestusTools.MOD_ID, layers.get(i)));
            if (isMissing(atlas, sprite)) continue;
            int color = adjustColor(itemColors.getColor(stack, i), layers.get(i).endsWith("/handle"));
            MaterialAnimation animation = ToolColorHandler.INSTANCE.getAnimation(stack, i);
            if (animation == null) renderSprite(sprite, vc, pose, light, overlay, color);
            else renderAnimatedSprite(sprite, vc, pose, light, overlay, color, animation,
                    ToolColorHandler.animationTicks(), (i + 1) * LAYER_STEP);
        }

        if (ToolStack.isInitialized(stack)) {
            for (ToolConstructionData.ModifierEntry mod : ToolStack.getModifiers(stack)) {
                if (mod.level() <= 0) continue;
                for (ResourceLocation tex : ModifierModelRegistry.candidates(item, mod.id())) {
                    TextureAtlasSprite sprite = atlas.apply(tex);
                    if (isMissing(atlas, sprite)) continue;
                    renderSprite(sprite, vc, pose, light, overlay, 0xFFFFFFFF);
                    break;
                }
            }
        }

        expandIfNotGui(ctx, pose);
        pose.popPose();
    }
}
