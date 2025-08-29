package top.ialdaiaxiariyay.bettergtae.data.recipe;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import appeng.core.definitions.AEItems;

import java.util.function.Consumer;

public class FormingPressRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("calculation_processor_press"))
                .notConsumable(AEItems.CALCULATION_PROCESSOR_PRESS.asItem())
                .inputItems(Blocks.IRON_BLOCK.asItem())
                .outputItems(AEItems.CALCULATION_PROCESSOR_PRESS.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("engineering_processor_press"))
                .notConsumable(AEItems.ENGINEERING_PROCESSOR_PRESS.asItem())
                .inputItems(Blocks.IRON_BLOCK.asItem())
                .outputItems(AEItems.ENGINEERING_PROCESSOR_PRESS.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("logic_processor_press"))
                .notConsumable(AEItems.LOGIC_PROCESSOR_PRESS.asItem())
                .inputItems(Blocks.IRON_BLOCK.asItem())
                .outputItems(AEItems.LOGIC_PROCESSOR_PRESS.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("silicon_processor_press"))
                .notConsumable(AEItems.SILICON_PRESS.asItem())
                .inputItems(Blocks.IRON_BLOCK.asItem())
                .outputItems(AEItems.SILICON_PRESS.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("printed_calculation_processor"))
                .notConsumable(AEItems.CALCULATION_PROCESSOR_PRESS.asItem())
                .inputItems(AEItems.CERTUS_QUARTZ_CRYSTAL.asItem())
                .outputItems(AEItems.CALCULATION_PROCESSOR_PRINT.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("printed_logic_processor"))
                .notConsumable(AEItems.ENGINEERING_PROCESSOR_PRESS.asItem())
                .inputItems(Items.GOLD_INGOT.asItem())
                .outputItems(AEItems.LOGIC_PROCESSOR_PRINT.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("printed_engineering_processor"))
                .notConsumable(AEItems.ENGINEERING_PROCESSOR_PRESS.asItem())
                .inputItems(Items.DIAMOND.asItem())
                .outputItems(AEItems.ENGINEERING_PROCESSOR_PRINT.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("printed_silicon"))
                .notConsumable(AEItems.SILICON_PRESS.asItem())
                .inputItems(AEItems.SILICON.asItem())
                .outputItems(AEItems.SILICON_PRINT.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("engineering_processor"))
                .inputItems(AEItems.ENGINEERING_PROCESSOR_PRINT.asItem())
                .inputItems(Items.REDSTONE.asItem())
                .inputItems(AEItems.SILICON_PRINT.asItem())
                .outputItems(AEItems.ENGINEERING_PROCESSOR.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("calculation_processor"))
                .inputItems(AEItems.CALCULATION_PROCESSOR_PRINT.asItem())
                .inputItems(Items.REDSTONE.asItem())
                .inputItems(AEItems.SILICON_PRINT.asItem())
                .outputItems(AEItems.CALCULATION_PROCESSOR.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(BetterGTAE.id("logic_processor"))
                .inputItems(AEItems.LOGIC_PROCESSOR_PRINT.asItem())
                .inputItems(Items.REDSTONE.asItem())
                .inputItems(AEItems.SILICON_PRINT.asItem())
                .outputItems(AEItems.LOGIC_PROCESSOR.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);
    }
}
