package top.ialdaiaxiariyay.bettergtae.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import java.util.function.Supplier;

public class CustomRecipeLogic extends RecipeLogic {

    private final Supplier<GTRecipe> recipe;

    public CustomRecipeLogic(IRecipeLogicMachine machine, Supplier<GTRecipe> recipeSupplier) {
        super(machine);
        this.recipe = recipeSupplier;
    }

    @Override
    public void findAndHandleRecipe() {
        lastRecipe = null;
        if (machine.hasCapabilityProxies()) {
            GTRecipe match = recipe.get();
            if (match != null) {
                setupRecipe(match);
            }
        }
    }

    @Override
    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            handleRecipeIO(lastRecipe, IO.OUT);
        }
        if (suspendAfterFinish) {
            setStatus(Status.SUSPEND);
            suspendAfterFinish = false;
        } else {
            if (RecipeHelper.matchRecipe(machine, lastRecipe).isSuccess()) {
                setupRecipe(lastRecipe);
                return;
            } else {
                GTRecipe match = recipe.get();
                if (match != null) {
                    setupRecipe(match);
                    return;
                }
            }
            setStatus(Status.IDLE);
        }
        progress = 0;
        duration = 0;
        isActive = false;
    }
}
