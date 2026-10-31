package com.titammods.hephaestus_tools.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.client.ToolColorHandler;
import com.titammods.hephaestus_tools.client.model.ModifierModelRegistry;
import com.titammods.hephaestus_tools.config.HephaestusConfig;
import com.titammods.hephaestus_tools.materials.MaterialAnimation;
import com.titammods.hephaestus_tools.tools.nbt.ToolConstructionData;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ToolItemRenderer implements SpecialModelRenderer<ToolItemRenderer.RenderData> {

    public record Layer(Identifier texture, int color, @Nullable MaterialAnimation animation, boolean handle) {}

    public record RenderData(List<Layer> layers, List<Identifier> overlays, long phase) {}

    private static final int PHASE_MS = 16;


    public static final Identifier RENDERER_ID =
            Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, "tool_modifiers");

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
                    "item/tool/cleaver/guard")),
            Map.entry("mace", List.of(
                    "item/tool/mace/handle",
                    "item/tool/mace/head")),
            Map.entry("spear", List.of(
                    "item/tool/spear/handle",
                    "item/tool/spear/head",
                    "item/tool/spear/binding")),
            Map.entry("bow", List.of(
                    "item/tool/bow/grip",
                    "item/tool/bow/limb",
                    "item/tool/bow/string"))
    );

    private static final Map<String, List<String>> WORKBENCH_LAYERS = Map.ofEntries(
            Map.entry("hand_axe", List.of(
                    "item/tool/hand_axe/handle",
                    "item/tool/hand_axe/head",
                    "item/tool/hand_axe/binding"))
    );

    public static List<String> workbenchLayersFor(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
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
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return null;
        return TOOL_LAYERS.get(id.getPath());
    }

    private static Identifier png(String path) {
        return Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, "textures/" + path + ".png");
    }

    private static Identifier pngOf(Identifier texture) {
        return Identifier.fromNamespaceAndPath(texture.getNamespace(), "textures/" + texture.getPath() + ".png");
    }

    public static void renderGuiPreview(GuiGraphicsExtractor graphics,
                                        ItemStack stack, int x, int y, int size, float alpha) {
        List<String> layers = layersFor(stack.getItem());
        if (layers == null) return;
        int alphaBits = Math.max(0, Math.min(255, Math.round(alpha * 255f))) << 24;
        for (int i = 0; i < layers.size(); i++) {
            int color = (ToolColorHandler.INSTANCE.getAnimatedColor(stack, i) & 0x00FFFFFF) | alphaBits;
            graphics.blit(RenderPipelines.GUI_TEXTURED, png(layers.get(i)), x, y,
                    0f, 0f, size, size, 16, 16, 16, 16, color);
        }
        for (Identifier overlay : modifierTextures(stack)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, overlay, x, y,
                    0f, 0f, size, size, 16, 16, 16, 16, 0x00FFFFFF | alphaBits);
        }
    }

    public static void renderGuiPreviewTinted(GuiGraphicsExtractor graphics,
                                              ItemStack stack, int x, int y, int size, int argb) {
        List<String> layers = workbenchLayersFor(stack.getItem());
        if (layers == null) return;
        int ta = argb >>> 24 & 255, tr = argb >> 16 & 255, tg = argb >> 8 & 255, tb = argb & 255;
        for (int i = 0; i < layers.size(); i++) {
            int base = ToolColorHandler.INSTANCE.getAnimatedColor(stack, i);
            int r = (base >> 16 & 255) * tr / 255, g = (base >> 8 & 255) * tg / 255, b = (base & 255) * tb / 255;
            int color = ta << 24 | r << 16 | g << 8 | b;
            graphics.blit(RenderPipelines.GUI_TEXTURED, png(layers.get(i)), x, y,
                    0f, 0f, size, size, 16, 16, 16, 16, color);
        }
        for (Identifier overlay : modifierTextures(stack)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, overlay, x, y,
                    0f, 0f, size, size, 16, 16, 16, 16, argb);
        }
    }

    private static final Map<Identifier, Boolean> TEXTURE_EXISTS = new ConcurrentHashMap<>();
    private static final Map<Identifier, boolean[][]> PIXEL_MASKS = new ConcurrentHashMap<>();

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

    private static boolean[][] mask(Identifier png) {
        return PIXEL_MASKS.computeIfAbsent(png, path -> {
            var resource = Minecraft.getInstance().getResourceManager().getResource(path);
            if (resource.isEmpty()) return new boolean[0][0];
            try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
                int w = image.getWidth(), h = Math.min(image.getHeight(), w);
                boolean[][] out = new boolean[w][h];
                for (int x = 0; x < w; x++) for (int y = 0; y < h; y++)
                    out[x][y] = (image.getPixel(x, y) >>> 24) > 0;
                return out;
            } catch (Exception e) {
                return new boolean[0][0];
            }
        });
    }

    private static boolean exists(Identifier png) {
        return TEXTURE_EXISTS.computeIfAbsent(png,
                path -> Minecraft.getInstance().getResourceManager().getResource(path).isPresent());
    }

    public static void clearTextureCache() {
        TEXTURE_EXISTS.clear();
        PIXEL_MASKS.clear();
    }

    private static List<Identifier> modifierTextures(ItemStack stack) {
        List<Identifier> out = new ArrayList<>();
        if (!ToolStack.isInitialized(stack)) return out;
        for (ToolConstructionData.ModifierEntry mod : ToolStack.getModifiers(stack)) {
            if (mod.level() <= 0) continue;
            for (Identifier tex : ModifierModelRegistry.candidates(stack.getItem(), mod.id())) {
                Identifier png = pngOf(tex);
                if (exists(png)) {
                    out.add(png);
                    break;
                }
            }
        }
        return out;
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<RenderData> {

        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<RenderData> bake(BakingContext ctx) {
            return new ToolItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<RenderData>> type() {
            return CODEC;
        }
    }

    private static final float FRONT_Z = 8.5f / 16f;
    private static final float BACK_Z = 7.5f / 16f;

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0f, 0f, BACK_Z - 0.01f));
        output.accept(new Vector3f(1f, 0f, BACK_Z));
        output.accept(new Vector3f(0f, 1f, BACK_Z));
        output.accept(new Vector3f(1f, 1f, BACK_Z));
        output.accept(new Vector3f(0f, 0f, FRONT_Z));
        output.accept(new Vector3f(1f, 0f, FRONT_Z));
        output.accept(new Vector3f(0f, 1f, FRONT_Z));
        output.accept(new Vector3f(1f, 1f, FRONT_Z + 0.01f));
    }

    @Override
    public @Nullable RenderData extractArgument(ItemStack stack) {
        List<String> paths = HephaestusConfig.modernInterface() ? workbenchLayersFor(stack.getItem()) : layersFor(stack.getItem());
        List<Layer> layers = new ArrayList<>();
        boolean animated = false;
        if (paths != null) {
            for (int i = 0; i < paths.size(); i++) {
                MaterialAnimation animation = ToolColorHandler.INSTANCE.getAnimation(stack, i);
                animated |= animation != null;
                layers.add(new Layer(png(paths.get(i)), ToolColorHandler.INSTANCE.getColor(stack, i), animation,
                        paths.get(i).endsWith("/handle")));
            }
        }
        List<Identifier> overlays = modifierTextures(stack);
        if (layers.isEmpty() && overlays.isEmpty()) return null;
        return new RenderData(layers, overlays, animated ? Util.getMillis() / PHASE_MS : 0L);
    }

    private static final float LAYER_STEP = 1f / 2048f;
    private static final float EDGE = 1f / 512f;

    @Override
    public void submit(@Nullable RenderData data, PoseStack poseStack,
                       SubmitNodeCollector collector,
                       int packedLight, int packedOverlay,
                       boolean hasFoil, int outlineColor) {
        if (data == null) return;
        int sky = packedLight >> 16 & 0xFFFF;
        int block = packedLight & 0xFFFF;
        int depth = 0;
        float ticks = data.phase() * PHASE_MS / 50f;
        ToolRenderStyle style = collector instanceof ToolRenderStyle s ? s : null;
        boolean translucent = style != null && style.translucent();
        poseStack.pushPose();
        for (int i = 0; i < data.layers().size(); i++) {
            Layer layer = data.layers().get(i);
            float lift = depth++ * LAYER_STEP;
            if (style != null && !style.showLayer(i)) continue;
            boolean[][] pixels = mask(layer.texture());
            int color = adjustColor(layer.color(), layer.handle());
            MaterialAnimation animation = layer.animation();
            collector.submitCustomGeometry(poseStack, renderType(layer.texture(), translucent),
                    (pose, buf) -> pixelGeometry(buf, pose.pose(), pixels, color, animation, ticks, style, lift, 0f, packedOverlay, sky, block));
        }
        if (style == null || style.showOverlays()) {
            for (Identifier texture : data.overlays()) {
                boolean[][] pixels = mask(texture);
                float lift = depth++ * LAYER_STEP;
                collector.submitCustomGeometry(poseStack, renderType(texture, translucent),
                        (pose, buf) -> pixelGeometry(buf, pose.pose(), pixels, 0xFFFFFFFF, null, 0f, style, lift, EDGE, packedOverlay, sky, block));
            }
        }
        poseStack.popPose();
    }

    private static boolean solid(boolean[][] p, int x, int y) {
        return x >= 0 && y >= 0 && x < p.length && y < p[x].length && p[x][y];
    }

    private static RenderType renderType(Identifier texture, boolean translucent) {
        return translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
    }

    private static void pixelGeometry(VertexConsumer buf, Matrix4f m, boolean[][] p, int color,
                                      @Nullable MaterialAnimation animation, float ticks, @Nullable ToolRenderStyle style,
                                      float lift, float grow, int overlay, int sky, int block) {
        int w = p.length;
        if (w == 0) return;
        int h = p[0].length;
        if ((color >>> 24) == 0) color |= 0xFF000000;
        float zf = FRONT_Z + lift, zb = BACK_Z - lift;
        for (int px = 0; px < w; px++) for (int py = 0; py < h; py++) {
            if (!p[px][py]) continue;
            int c = animation == null ? color : animation.pixel(color, ticks, px, py, w, h);
            if (style != null) c = style.tint(c);
            int a = c >>> 24 & 255, r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255;
            float fx0 = (float) px / w, fx1 = (float) (px + 1) / w;
            float fy0 = 1f - (float) (py + 1) / h, fy1 = 1f - (float) py / h;
            float u0 = (px + 0.25f) / w, u1 = (px + 0.75f) / w;
            float v0 = (py + 0.25f) / h, v1 = (py + 0.75f) / h;

            q(buf, m, fx0, fy0, zf, u0, v1, 0f, 0f, 1f, r, g, b, a, overlay, sky, block);
            q(buf, m, fx1, fy0, zf, u1, v1, 0f, 0f, 1f, r, g, b, a, overlay, sky, block);
            q(buf, m, fx1, fy1, zf, u1, v0, 0f, 0f, 1f, r, g, b, a, overlay, sky, block);
            q(buf, m, fx0, fy1, zf, u0, v0, 0f, 0f, 1f, r, g, b, a, overlay, sky, block);

            q(buf, m, fx0, fy0, zb, u0, v1, 0f, 0f, -1f, r, g, b, a, overlay, sky, block);
            q(buf, m, fx0, fy1, zb, u0, v0, 0f, 0f, -1f, r, g, b, a, overlay, sky, block);
            q(buf, m, fx1, fy1, zb, u1, v0, 0f, 0f, -1f, r, g, b, a, overlay, sky, block);
            q(buf, m, fx1, fy0, zb, u1, v1, 0f, 0f, -1f, r, g, b, a, overlay, sky, block);

            float x0 = fx0 - grow, x1 = fx1 + grow, y0 = fy0 - grow, y1 = fy1 + grow;
            if (!solid(p, px - 1, py)) {
                q(buf, m, x0, y0, zb, u0, v1, -1f, 0f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x0, y0, zf, u1, v1, -1f, 0f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x0, y1, zf, u1, v0, -1f, 0f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x0, y1, zb, u0, v0, -1f, 0f, 0f, r, g, b, a, overlay, sky, block);
            }
            if (!solid(p, px + 1, py)) {
                q(buf, m, x1, y0, zb, u0, v1, 1f, 0f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x1, y1, zb, u0, v0, 1f, 0f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x1, y1, zf, u1, v0, 1f, 0f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x1, y0, zf, u1, v1, 1f, 0f, 0f, r, g, b, a, overlay, sky, block);
            }
            if (!solid(p, px, py + 1)) {
                q(buf, m, x0, y0, zb, u0, v1, 0f, -1f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x1, y0, zb, u1, v1, 0f, -1f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x1, y0, zf, u1, v0, 0f, -1f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x0, y0, zf, u0, v0, 0f, -1f, 0f, r, g, b, a, overlay, sky, block);
            }
            if (!solid(p, px, py - 1)) {
                q(buf, m, x0, y1, zb, u0, v1, 0f, 1f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x0, y1, zf, u0, v0, 0f, 1f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x1, y1, zf, u1, v0, 0f, 1f, 0f, r, g, b, a, overlay, sky, block);
                q(buf, m, x1, y1, zb, u1, v1, 0f, 1f, 0f, r, g, b, a, overlay, sky, block);
            }
        }
    }

    private static void q(VertexConsumer buf, Matrix4f m, float x, float y, float z, float u, float vv,
                          float nx, float ny, float nz, int r, int g, int b, int a,
                          int packedOverlay, int sky, int block) {
        buf.addVertex(m, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, vv)
                .setOverlay(packedOverlay)
                .setUv2(block, sky)
                .setNormal(nx, ny, nz);
    }

}