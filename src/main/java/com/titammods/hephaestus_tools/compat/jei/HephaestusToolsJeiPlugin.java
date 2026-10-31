package com.titammods.hephaestus_tools.compat.jei;

import com.titammods.compat.jei.TitamModsJEIPlugin;
import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.materials.MaterialId;
import com.titammods.hephaestus_tools.registry.ModComponents;
import com.titammods.hephaestus_tools.registry.ModItems;
import com.titammods.hephaestus_tools.tools.part.ToolPartItem;
import com.titammods.setup.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JeiPlugin
public class HephaestusToolsJeiPlugin implements IModPlugin {

    private static final Identifier UID =
            Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, "jei_plugin");

    private static final String CAST_SUFFIX = "_cast";

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    private static Set<Item> partItems() {
        return Set.of(
                ModItems.PICK_HEAD.get(), ModItems.HAMMER_HEAD.get(), ModItems.SMALL_AXE_HEAD.get(),
                ModItems.BROAD_AXE_HEAD.get(), ModItems.ADZE_HEAD.get(), ModItems.LARGE_PLATE.get(),
                ModItems.SMALL_BLADE.get(), ModItems.LARGE_BLADE.get(), ModItems.TOOL_HANDLE.get(),
                ModItems.TOUGH_HANDLE.get(), ModItems.TOOL_BINDING.get(), ModItems.TOUGH_BINDING.get(),
                ModItems.MACE_HEAD.get());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        ISubtypeInterpreter<ItemStack> interp = (stack, ctx) -> {
            MaterialId id = stack.get(ModComponents.PART_MATERIAL.get());
            return id == null ? "none" : id.toString();
        };
        for (Item part : partItems()) reg.registerSubtypeInterpreter(part, interp);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HephaestusToolsJeiPlugin.class);

    @Override
    public void onRuntimeAvailable(IJeiRuntime jei) {
        if (!ModList.get().isLoaded(HephaestusTools.FORGE_MOD_ID)) return;
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        RecipeMap rm = server != null ? server.getRecipeManager().recipeMap() : SyncedCastingRecipes.get();
        if (rm == null) {
            LOGGER.warn("[HephaestusTools/JEI] Receitas de fundicao nao recebidas do servidor, variantes de peca nao registradas");
            return;
        }
        IRecipeManager jeiRm = jei.getRecipeManager();

        List<ItemStack> variants = new ArrayList<>();
        List<ModRecipes.CastingTableRecipe> castGeneric = new ArrayList<>();
        List<ModRecipes.CastingTableRecipe> castWood = new ArrayList<>();

        for (RecipeHolder<ModRecipes.CastingTableRecipe> holder :
                rm.byType(ModRecipes.CASTING_TABLE_TYPE.get())) {
            ModRecipes.CastingTableRecipe r = holder.value();
            ItemStack result = r.result();

            if (result.getItem() instanceof ToolPartItem && result.has(ModComponents.PART_MATERIAL.get())) {
                variants.add(result);
                continue;
            }

            Identifier rid = r.resultId();
            if (rid == null || !rid.getPath().endsWith(CAST_SUFFIX)) continue;

            Item moldPart = firstItemOf(r.cast());
            if (!(moldPart instanceof ToolPartItem)) continue;

            ItemStack woodPart = new ItemStack(moldPart);
            woodPart.set(ModComponents.PART_MATERIAL.get(), MaterialId.of(HephaestusTools.MOD_ID, "wood"));
            Ingredient woodIng = DataComponentIngredient.of(false, woodPart);

            castGeneric.add(r);
            castWood.add(new ModRecipes.CastingTableRecipe(
                    Optional.of(woodIng), r.castConsumed(),
                    r.fluidId(), r.fluidAmount(),
                    r.resultId(), r.resultCount(), r.coolingTime()));
        }

        int castingTotal = rm.byType(ModRecipes.CASTING_TABLE_TYPE.get()).size();
        int meltingTotal = rm.byType(ModRecipes.MELTING_TYPE.get()).size();
        LOGGER.info("[HephaestusTools/JEI] casting={} melting={} variantes={} moldes={}",
                castingTotal, meltingTotal, variants.size(), castGeneric.size());

        if (!variants.isEmpty()) {
            try {
                jei.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, variants);
            } catch (Exception e) {
                LOGGER.error("[HephaestusTools/JEI] Falha ao registrar variantes de peca", e);
            }
        }
        if (!castGeneric.isEmpty()) {
            jeiRm.hideRecipes(TitamModsJEIPlugin.CASTING_TABLE_TYPE, castGeneric);
            jeiRm.addRecipes(TitamModsJEIPlugin.CASTING_TABLE_TYPE, castWood);
        }
    }

    private static Item firstItemOf(Optional<Ingredient> ingredient) {
        if (ingredient.isEmpty()) return Items.AIR;
        try {
            return ingredient.get().items().findFirst().map(Holder::value).orElse(Items.AIR);
        } catch (UnsupportedOperationException e) {
            return Items.AIR;
        }
    }
}