package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * If you want to create a recipe:
 * {@code GTRecipeTypes.DUMMY_RECIPES.recipeBuilder("raw").addData("TierType", 1).save(provider);}
 */
public class CustomTierMachine extends WorkableElectricMultiblockMachine {

    public final String[] TierTypes;

    private final Map<String, Integer> tierValues = new HashMap<>();

    public CustomTierMachine(IMachineBlockEntity holder, String... tierTypes) {
        super(holder);
        this.TierTypes = tierTypes;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        tierValues.clear();

        for (String tierType : TierTypes) {
            Integer value = getMultiblockState().getMatchContext().get(tierType);
            if (value != null) {
                tierValues.put(tierType, value);
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        tierValues.clear();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe != null) {
            for (String tierType : TierTypes) {
                if (recipe.data.contains(tierType)) {
                    int requiredTier = recipe.data.getInt(tierType);
                    Integer actualTier = tierValues.get(tierType);

                    if (actualTier == null || requiredTier > actualTier) {
                        return false;
                    }
                }
            }
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;

        for (String tierType : TierTypes) {
            Integer tierValue = tierValues.get(tierType);
            if (tierValue != null) {
                textList.add(Component.translatable("bettergtae.block.tier", tierValue));
            }
        }
    }

    public int getTierValue(String tierType) {
        return tierValues.getOrDefault(tierType, 0);
    }

    public boolean meetsTierRequirement(String tierType, int requiredTier) {
        Integer actualTier = tierValues.get(tierType);
        return actualTier != null && actualTier >= requiredTier;
    }

    public boolean meetsAllTierRequirements(Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            if (!meetsTierRequirement(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
