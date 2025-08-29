package top.ialdaiaxiariyay.bettergtae;

import top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate;
import top.ialdaiaxiariyay.bettergtae.common.data.BGTAEBlocks;
import top.ialdaiaxiariyay.bettergtae.common.data.BGTAEItems;
import top.ialdaiaxiariyay.bettergtae.data.recipe.AddAllRecipes;
import top.ialdaiaxiariyay.bettergtae.data.recipe.AssemblyLineRecipes;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

@GTAddon
public class BetterGTAEAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return BGTAERegistrate.REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        BGTAEBlocks.init();
        BGTAEItems.init();
    }

    @Override
    public String addonModId() {
        return BetterGTAE.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        AddAllRecipes.init(provider);
    }
}
