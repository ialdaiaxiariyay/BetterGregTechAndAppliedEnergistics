package top.ialdaiaxiariyay.bettergtae.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class AddAllRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        AssemblyLineRecipes.init(provider);
        FormingPressRecipes.init(provider);
        MaceratorRecipes.init(provider);
        ElectrolyzerRecipe.init(provider);
        MixerRecipes.init(provider);
        VanillaRecipe.init(provider);
    }
}
