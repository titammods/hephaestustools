package com.titammods.hephaestus_tools.tables.layout;

import com.titammods.hephaestus_tools.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public final class ToolLayouts {

    private ToolLayouts() {}

    private static final String NS = "hephaestus_tools";


    private static ToolLayout make(String id, Item icon,
                                   ToolLayout.ToolSlotDef toolSlot,
                                   List<ToolLayout.InputSlotDef> inputs) {
        return new ToolLayout(id,
                Component.translatable("layout.hephaestus_tools." + id),
                Component.translatable("layout.hephaestus_tools." + id + ".description"),
                icon, toolSlot, inputs);
    }

    private static ToolLayout.InputSlotDef in(int x, int y, Item part) {
        return new ToolLayout.InputSlotDef(x, y, part);
    }

    private static ToolLayout.InputSlotDef in(int x, int y) {
        return new ToolLayout.InputSlotDef(x, y);
    }

    private static final ToolLayout.ToolSlotDef HIDDEN = new ToolLayout.ToolSlotDef(-1, -1, true);

    public static final ToolLayout REPAIR = make("repair", Items.EXPERIENCE_BOTTLE,
            new ToolLayout.ToolSlotDef(33, 41),
            List.of(
                    in(15, 62),
                    in(11, 37),
                    in(33, 19),
                    in(55, 37),
                    in(51, 62)
            ));

    public static final ToolLayout PICKAXE = make("pickaxe", ModItems.PICKAXE.get(), HIDDEN,
            List.of(
                    in(53, 22, ModItems.PICK_HEAD.get()),
                    in(15, 60, ModItems.TOOL_HANDLE.get()),
                    in(33, 42, ModItems.TOOL_BINDING.get())
            ));

    public static final ToolLayout SLEDGE_HAMMER = make("sledge_hammer", ModItems.SLEDGE_HAMMER.get(), HIDDEN,
            List.of(
                    in(44, 29, ModItems.HAMMER_HEAD.get()),
                    in(21, 52, ModItems.TOUGH_HANDLE.get()),
                    in(50, 48, ModItems.LARGE_PLATE.get()),
                    in(25, 20, ModItems.LARGE_PLATE.get())
            ));

    public static final ToolLayout VEIN_HAMMER = make("vein_hammer", ModItems.VEIN_HAMMER.get(), HIDDEN,
            List.of(
                    in(44, 29, ModItems.HAMMER_HEAD.get()),
                    in(21, 52, ModItems.TOUGH_HANDLE.get()),
                    in(41, 49, ModItems.TOUGH_BINDING.get()),
                    in(25, 20, ModItems.TOUGH_HANDLE.get())
            ));

    public static final ToolLayout MATTOCK = make("mattock", ModItems.MATTOCK.get(), HIDDEN,
            List.of(
                    in(31, 22, ModItems.SMALL_AXE_HEAD.get()),
                    in(22, 53, ModItems.TOOL_HANDLE.get()),
                    in(51, 34, ModItems.ADZE_HEAD.get())
            ));

    public static final ToolLayout PAXEL = make("paxel", ModItems.PAXEL.get(), HIDDEN,
            List.of(
                    in(51, 34, ModItems.PICK_HEAD.get()),
                    in(22, 53, ModItems.TOOL_HANDLE.get()),
                    in(41, 14, ModItems.ADZE_HEAD.get()),
                    in(21, 22, ModItems.SMALL_AXE_HEAD.get())
            ));

    public static final ToolLayout EXCAVATOR = make("excavator", ModItems.EXCAVATOR.get(), HIDDEN,
            List.of(
                    in(45, 26, ModItems.HAMMER_HEAD.get()),
                    in(25, 46, ModItems.TOUGH_HANDLE.get()),
                    in(25, 26, ModItems.TOUGH_BINDING.get()),
                    in(7, 62, ModItems.TOUGH_HANDLE.get())
            ));

    public static final ToolLayout HAND_AXE = make("hand_axe", ModItems.HAND_AXE.get(), HIDDEN,
            List.of(
                    in(31, 22, ModItems.SMALL_AXE_HEAD.get()),
                    in(22, 53, ModItems.TOOL_HANDLE.get()),
                    in(51, 34, ModItems.TOOL_BINDING.get())
            ));

    public static final ToolLayout BROAD_AXE = make("broad_axe", ModItems.BROAD_AXE.get(), HIDDEN,
            List.of(
                    in(25, 20, ModItems.BROAD_AXE_HEAD.get()),
                    in(21, 52, ModItems.TOUGH_HANDLE.get()),
                    in(50, 48, ModItems.PICK_HEAD.get()),
                    in(44, 29, ModItems.TOUGH_BINDING.get())
            ));

    public static final ToolLayout KAMA = make("kama", ModItems.KAMA.get(), HIDDEN,
            List.of(
                    in(31, 22, ModItems.SMALL_BLADE.get()),
                    in(22, 53, ModItems.TOOL_HANDLE.get()),
                    in(51, 34, ModItems.TOOL_BINDING.get())
            ));

    public static final ToolLayout SCYTHE = make("scythe", ModItems.SCYTHE.get(), HIDDEN,
            List.of(
                    in(35, 20, ModItems.LARGE_BLADE.get()),
                    in(12, 55, ModItems.TOUGH_HANDLE.get()),
                    in(50, 40, ModItems.TOUGH_BINDING.get()),
                    in(30, 40, ModItems.TOUGH_HANDLE.get())
            ));

    public static final ToolLayout DAGGER = make("dagger", ModItems.DAGGER.get(), HIDDEN,
            List.of(
                    in(39, 35, ModItems.SMALL_BLADE.get()),
                    in(21, 53, ModItems.TOOL_HANDLE.get())
            ));

    public static final ToolLayout SWORD = make("sword", ModItems.SWORD.get(), HIDDEN,
            List.of(
                    in(48, 26, ModItems.SMALL_BLADE.get()),
                    in(12, 62, ModItems.TOOL_HANDLE.get()),
                    in(30, 44, ModItems.TOOL_HANDLE.get())
            ));

    public static final ToolLayout CLEAVER = make("cleaver", ModItems.CLEAVER.get(), HIDDEN,
            List.of(
                    in(45, 26, ModItems.LARGE_BLADE.get()),
                    in(7, 62, ModItems.TOUGH_HANDLE.get()),
                    in(25, 46, ModItems.TOUGH_HANDLE.get()),
                    in(45, 46, ModItems.TOUGH_BINDING.get())
            ));

    public static final ToolLayout MACE = make("mace", ModItems.MACE.get(), HIDDEN,
            List.of(
                    in(44, 29, ModItems.MACE_HEAD.get()),
                    in(21, 52, ModItems.TOOL_HANDLE.get())
            ));

    public static final ToolLayout SPEAR = make("spear", ModItems.SPEAR.get(), HIDDEN,
            List.of(
                    in(53, 22, ModItems.SPEAR_HEAD.get()),
                    in(15, 60, ModItems.TOOL_HANDLE.get()),
                    in(33, 42, ModItems.TOOL_BINDING.get())
            ));

    public static final ToolLayout BOW = make("bow", ModItems.BOW.get(), HIDDEN,
            List.of(
                    in(53, 22, ModItems.BOW_LIMB.get()),
                    in(15, 60, ModItems.TOOL_HANDLE.get()),
                    in(33, 42, ModItems.BOWSTRING.get())
            ));

    public static final List<ToolLayout> ALL = List.of(
            REPAIR,
            PICKAXE, SLEDGE_HAMMER, VEIN_HAMMER,
            MATTOCK, PAXEL, EXCAVATOR,
            HAND_AXE, BROAD_AXE,
            KAMA, SCYTHE,
            DAGGER, SWORD, CLEAVER, MACE, SPEAR, BOW
    );

    public static final List<ToolLayout> CRAFTABLE = ALL.stream()
            .filter(layout -> layout != VEIN_HAMMER)
            .toList();
}