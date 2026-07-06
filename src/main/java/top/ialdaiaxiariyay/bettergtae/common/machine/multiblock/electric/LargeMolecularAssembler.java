package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.AEItemKey;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.bettergtae.api.recipe.CustomRecipeLogic;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.CraftingPatternPartMachine;

import java.util.List;

public class LargeMolecularAssembler extends WorkableElectricMultiblockMachine {

    private CraftingPatternPartMachine craftingPatternPartMachine;

    public LargeMolecularAssembler(BlockEntityCreationInfo info) {
        super(info, new CustomRecipeLogic());
        if (getRecipeLogic() instanceof CustomRecipeLogic custom) {
            custom.setRecipeSupplier(this::getGTRecipe);
        }
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;

        for (var part : getParts()) {
            if (part instanceof CraftingPatternPartMachine hatch) {
                this.craftingPatternPartMachine = hatch;
                break;
            }
        }
    }

    @Override
    public void invalidateStructure(@NotNull String name) {
        super.invalidateStructure(name);
        if (DEFAULT_STRUCTURE.equals(name)) {
            this.craftingPatternPartMachine = null;
        }
    }

    private int getHatchParallel() {
        return getParallelHatch()
                .map(hatch -> Math.max(1, hatch.getCurrentParallel()))
                .orElse(1);
    }

    private GTRecipe getGTRecipe() {
        if (craftingPatternPartMachine == null) return null;

        GTRecipe output = GTRecipeBuilder.ofRaw().buildRawRecipe();
        List<Content> outputList = output.outputs.computeIfAbsent(ItemRecipeCapability.CAP,
                cap -> new ObjectArrayList<>());

        long remain = getHatchParallel();

        var iterator = Object2LongMaps.fastIterator(craftingPatternPartMachine.outputItems);
        while (iterator.hasNext() && remain > 0) {
            var entry = iterator.next();
            var key = entry.getKey();
            if (!(key.what() instanceof AEItemKey aeItemKey)) {
                iterator.remove();
                continue;
            }

            long multiply = entry.getLongValue();
            long extract = Math.min(multiply, remain);

            long totalItems = extract * key.amount();

            Content cont = new Content(
                    SizedIngredient.create(Ingredient.of(aeItemKey.getItem()), (int) totalItems),
                    ChanceLogic.getMaxChancedValue(),
                    ChanceLogic.getMaxChancedValue());
            outputList.add(cont);

            remain -= extract;
            multiply -= extract;
            if (multiply == 0) {
                iterator.remove();
            } else {
                entry.setValue(multiply);
            }
        }

        if (outputList.isEmpty()) {
            return null;
        } else {
            output.duration = this.getTier() * 10;
            return output;
        }
    }
}
