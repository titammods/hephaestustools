package com.titammods.hephaestus_tools.tables.screen;

import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.mixin.client.SlotAccessor;
import com.titammods.hephaestus_tools.tables.menu.ForgeStorageMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ForgeStorageScreen extends AbstractContainerScreen<ForgeStorageMenu> {

    private static final Identifier SPR_PAGE = spr("page"), SPR_DRAWER = spr("drawer_panel"), SPR_SLOT = spr("drawer_slot");
    private static final int PAD = 10, HEAD = 15, CELL = 18, GAP = 6, INV_PAD = 7, INV_LABEL = 11;
    private static final int WIDTH = ForgeStorageMenu.COLUMNS * CELL + PAD * 2;
    private static final int INV_H = INV_PAD + INV_LABEL + 3 * CELL + 4 + CELL + INV_PAD;
    private static final int MAX_ROWS = ForgeStorageMenu.SIZE / ForgeStorageMenu.COLUMNS;
    private static final int INK = 0xFFE3E3E3, INK2 = 0xFF9A8F8C, ACCENT = 0xFFE0921E;

    private float rows = -1f;
    private long lastMs = -1L;

    public ForgeStorageScreen(ForgeStorageMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, WIDTH, pageHeight(MAX_ROWS) + GAP + INV_H);
    }

    private static Identifier spr(String name) {
        return Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, "workbench/" + name);
    }

    private static int pageHeight(float rows) {
        return PAD + HEAD + Math.round(rows * CELL) + PAD;
    }

    private int currentHeight() {
        return pageHeight(rows) + GAP + INV_H;
    }

    private void animate() {
        long now = Util.getMillis();
        float dt = lastMs < 0 ? 1f : Mth.clamp((now - lastMs) / 1000f, 0f, 0.1f);
        lastMs = now;
        int target = menu.visibleRows();
        if (rows < 0f) rows = target;
        rows += (target - rows) * Math.min(1f, dt * 14f);
        if (Math.abs(target - rows) < 0.01f) rows = target;
        topPos = (height - currentHeight()) / 2;
        layoutSlots();
    }

    private void layoutSlots() {
        int invTop = pageHeight(rows) + GAP;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            int x, y;
            if (i < ForgeStorageMenu.SIZE) {
                x = PAD + 1 + (i % ForgeStorageMenu.COLUMNS) * CELL;
                y = PAD + HEAD + 1 + (i / ForgeStorageMenu.COLUMNS) * CELL;
            } else {
                int k = i - ForgeStorageMenu.SIZE;
                x = PAD + 1 + (k % 9) * CELL;
                y = k < 27 ? invTop + INV_PAD + INV_LABEL + 1 + (k / 9) * CELL : invTop + INV_PAD + INV_LABEL + 1 + 3 * CELL + 4;
            }
            ((SlotAccessor) slot).hephaestusTools$setX(x);
            ((SlotAccessor) slot).hephaestusTools$setY(y);
        }
    }

    @Override
    protected void init() {
        super.init();
        rows = -1f;
        animate();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float tick) {
        animate();
        super.extractRenderState(g, mx, my, tick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float tick) {
        super.extractBackground(g, mx, my, tick);
        int x = leftPos, y = topPos, pageH = pageHeight(rows);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, SPR_PAGE, x - 4, y - 4, WIDTH + 8, pageH + 8);
        g.fill(x + PAD, y + PAD + HEAD - 4, x + WIDTH - PAD, y + PAD + HEAD - 3, 0xFF3A3434);
        int shownRows = Mth.ceil(rows - 0.05f);
        int visible = menu.visibleSlots();
        for (int i = 0; i < Math.min(visible, shownRows * ForgeStorageMenu.COLUMNS); i++) {
            int sx = x + PAD + (i % ForgeStorageMenu.COLUMNS) * CELL, sy = y + PAD + HEAD + (i / ForgeStorageMenu.COLUMNS) * CELL;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SPR_SLOT, sx, sy, CELL, CELL);
            if (i == visible - 1 && menu.getSlot(i).getItem().isEmpty()) g.fill(sx + 1, sy + 1, sx + CELL - 1, sy + CELL - 1, 0x18E0921E);
        }
        int invTop = y + pageH + GAP;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, SPR_DRAWER, x, invTop, WIDTH, INV_H);
        for (int k = 0; k < 36; k++) {
            int sx = x + PAD + (k % 9) * CELL;
            int sy = k < 27 ? invTop + INV_PAD + INV_LABEL + (k / 9) * CELL : invTop + INV_PAD + INV_LABEL + 3 * CELL + 4;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SPR_SLOT, sx, sy, CELL, CELL);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
        return mx < xo - 4 || my < yo - 4 || mx >= xo + WIDTH + 4 || my >= yo + currentHeight();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.text(font, title, PAD, PAD, INK, false);
        Component count = Component.literal(menu.storedCount() + " / " + ForgeStorageMenu.SIZE);
        g.text(font, count, WIDTH - PAD - font.width(count), PAD, menu.storedCount() >= ForgeStorageMenu.SIZE ? ACCENT : INK2, false);
        g.text(font, playerInventoryTitle, PAD, pageHeight(rows) + GAP + INV_PAD, INK2, false);
    }
}