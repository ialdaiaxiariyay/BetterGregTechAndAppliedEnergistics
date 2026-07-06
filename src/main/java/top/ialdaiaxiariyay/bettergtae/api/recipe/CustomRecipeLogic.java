package top.ialdaiaxiariyay.bettergtae.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import lombok.Setter;

import java.util.function.Supplier;

@Setter
public class CustomRecipeLogic extends RecipeLogic {

    private Supplier<GTRecipe> recipeSupplier;

    public CustomRecipeLogic() {
        super();
    }

    @Override
    public void findAndHandleRecipe() {
        lastRecipe = null;
        IRecipeLogicMachine machine = getRLMachine();
        if (machine.hasCapabilityProxies()) {
            GTRecipe match = recipeSupplier != null ? recipeSupplier.get() : null;
            if (match != null) {
                setupRecipe(match);
            }
        }
    }

    @Override
    public void onRecipeFinish() {
        IRecipeLogicMachine machine = getRLMachine();
        machine.afterWorking();
        if (lastRecipe != null) {
            handleRecipeIO(lastRecipe, IO.OUT);
        }

        if (suspendAfterFinish) {
            setStatus(Status.SUSPEND);
            suspendAfterFinish = false;
        } else {
            GTRecipe match = recipeSupplier != null ? recipeSupplier.get() : null;
            if (match != null) {
                setupRecipe(match);
                return;
            }
            setStatus(Status.IDLE);
        }
        progress = 0;
        duration = 0;
        isActive = false;
    }
}
