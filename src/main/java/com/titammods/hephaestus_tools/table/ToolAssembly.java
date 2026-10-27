package com.titammods.hephaestus_tools.table;

import com.titammods.hephaestus_tools.materials.MaterialId;
import com.titammods.hephaestus_tools.tables.layout.ToolLayouts;
import com.titammods.hephaestus_tools.tools.helper.ToolBuildHandler;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import com.titammods.hephaestus_tools.tools.part.ToolPartItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public record ToolAssembly(Item result, List<Item> parts) {

    public static final List<ToolAssembly> REGISTRY = new ArrayList<>();

    public static void init() {
        if (!REGISTRY.isEmpty()) return;
        for (var layout : ToolLayouts.CRAFTABLE) {
            List<Item> parts = ToolBuildHandler.getToolParts(layout.iconItem());
            if (!parts.isEmpty()) REGISTRY.add(new ToolAssembly(layout.iconItem(), parts));
        }
    }

    private Item primary() { return parts.get(0); }

    public List<MaterialId> primaryVariants(Player p) {
        TreeMap<String, MaterialId> distinct = new TreeMap<>();
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == primary() && primary() instanceof ToolPartItem tp) {
                MaterialId m = tp.getMaterial(s);
                if (m != null && !m.isEmpty()) distinct.put(m.id().toString(), m);
            }
        }
        return new ArrayList<>(distinct.values());
    }

    public boolean variantCraftable(Player p, MaterialId primaryMat) {
        return primaryMat != null && variantMissing(p, primaryMat).isEmpty();
    }

    public List<Item> variantMissing(Player p, MaterialId primaryMat) {
        boolean[] used = new boolean[p.getInventory().getContainerSize()];
        List<Item> missing = new ArrayList<>();
        int head = primaryMat == null ? -1 : findPartOfMaterial(p, primary(), primaryMat, used);
        if (head < 0) missing.add(primary()); else used[head] = true;
        for (int i = 1; i < parts.size(); i++) {
            int slot = findPart(p, parts.get(i), used);
            if (slot < 0) missing.add(parts.get(i)); else used[slot] = true;
        }
        return missing;
    }

    public boolean craftVariant(Player p, MaterialId primaryMat) {
        if (primaryMat == null) return false;
        boolean[] used = new boolean[p.getInventory().getContainerSize()];
        int[] slots = new int[parts.size()];
        List<MaterialId> mats = new ArrayList<>();
        int head = findPartOfMaterial(p, primary(), primaryMat, used);
        if (head < 0) return false;
        used[head] = true; slots[0] = head; mats.add(primaryMat);
        for (int i = 1; i < parts.size(); i++) {
            int slot = findPart(p, parts.get(i), used);
            if (slot < 0) return false;
            used[slot] = true; slots[i] = slot;
            mats.add(((ToolPartItem) parts.get(i)).getMaterial(p.getInventory().getItem(slot)));
        }
        for (int slot : slots) p.getInventory().getItem(slot).shrink(1);
        ItemStack tool = ToolStack.createTool(new ItemStack(result), mats);
        if (!p.getInventory().add(tool)) p.drop(tool, false);
        return true;
    }

    public ItemStack previewVariant(Player p, MaterialId primaryMat) {
        if (!variantCraftable(p, primaryMat)) return new ItemStack(result);
        boolean[] used = new boolean[p.getInventory().getContainerSize()];
        List<MaterialId> mats = new ArrayList<>();
        int head = findPartOfMaterial(p, primary(), primaryMat, used); used[head] = true; mats.add(primaryMat);
        for (int i = 1; i < parts.size(); i++) {
            int slot = findPart(p, parts.get(i), used); used[slot] = true;
            mats.add(((ToolPartItem) parts.get(i)).getMaterial(p.getInventory().getItem(slot)));
        }
        return ToolStack.createTool(new ItemStack(result), mats);
    }

    private static int findPart(Player p, Item part, boolean[] used) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (used[i]) continue;
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == part) return i;
        }
        return -1;
    }

    private static int findPartOfMaterial(Player p, Item part, MaterialId mat, boolean[] used) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (used[i]) continue;
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == part && part instanceof ToolPartItem tp) {
                MaterialId m = tp.getMaterial(s);
                if (m != null && m.equals(mat)) return i;
            }
        }
        return -1;
    }

    public record Variant(ToolAssembly assembly, MaterialId primary) {
        public boolean isBase() { return primary == null; }
        public boolean craftable(Player p) { return assembly.variantCraftable(p, primary); }
        public boolean craft(Player p) { return assembly.craftVariant(p, primary); }
        public ItemStack preview(Player p) { return assembly.previewVariant(p, primary); }
        public List<Item> missing(Player p) { return assembly.variantMissing(p, primary); }
        public ItemStack resultItem() { return new ItemStack(assembly.result); }
    }

    public static List<Variant> variants(Player p) {
        init();
        List<Variant> out = new ArrayList<>();
        for (ToolAssembly a : REGISTRY) {
            List<MaterialId> prims = a.primaryVariants(p);
            if (prims.isEmpty()) out.add(new Variant(a, null));
            else for (MaterialId m : prims) out.add(new Variant(a, m));
        }
        return out;
    }
}