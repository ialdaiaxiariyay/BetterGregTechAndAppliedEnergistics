package top.ialdaiaxiariyay.bettergtae.data.recipe;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import java.util.function.Consumer;

public class MaceratorRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(BetterGTAE.id("ender_dust"))
                .inputItems(Items.ENDER_PEARL)
                .outputItems(AEItems.ENDER_DUST.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(BetterGTAE.id("fluix_dust"))
                .inputItems(AEItems.FLUIX_CRYSTAL.asItem())
                .outputItems(AEItems.FLUIX_DUST.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(BetterGTAE.id("sky_dust"))
                .inputItems(AEBlocks.SKY_STONE_BLOCK.asItem())
                .outputItems(AEItems.SKY_DUST.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);
    }
}
