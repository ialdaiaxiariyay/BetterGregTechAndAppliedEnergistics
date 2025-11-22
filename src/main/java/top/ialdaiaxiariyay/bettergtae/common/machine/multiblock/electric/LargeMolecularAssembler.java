package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.electric;

import top.ialdaiaxiariyay.bettergtae.api.recipe.CustomRecipeLogic;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.CraftingPatternPartMachine;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.AEItemKey;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class LargeMolecularAssembler extends WorkableElectricMultiblockMachine {

    public LargeMolecularAssembler(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private CraftingPatternPartMachine craftingPatternPartMachine;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (var part : getParts()) {
            if (part instanceof CraftingPatternPartMachine hatch) {
                this.craftingPatternPartMachine = hatch;
                break;
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.craftingPatternPartMachine = null;
    }

    private static int getHatchParallel(MetaMachine machine) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            Optional<IParallelHatch> parallelHatch = controller.getParallelHatch();
            if (parallelHatch.isPresent()) {
                return Math.max(1, parallelHatch.get().getCurrentParallel());
            }
        }
        return 1;
    }

    private GTRecipe getGTRecipe() {
        if (craftingPatternPartMachine == null) return null;
        GTRecipe output = GTRecipeBuilder.ofRaw().buildRawRecipe();
        List<Content> outputList = output.outputs.computeIfAbsent(ItemRecipeCapability.CAP, cap -> new ObjectArrayList<>());
        long remain = getHatchParallel(this);
        for (var it = Object2LongMaps.fastIterator(craftingPatternPartMachine.getOutputItems()); it.hasNext() && remain > 0;) {
            var entry = it.next();
            var key = entry.getKey();
            if (!(key.what() instanceof AEItemKey aeItemKey)) {
                it.remove();
                continue;
            }
            Item item = aeItemKey.getItem();
            long multiply = entry.getLongValue();

            int extract = Math.toIntExact(Math.min(multiply, remain));

            var cont = new Content(SizedIngredient.create(Ingredient.of(item), (int) (extract * key.amount())), ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0);
            outputList.add(cont);

            remain -= extract;
            multiply -= extract;
            if (multiply == 0) it.remove();
            else entry.setValue(multiply);
        }
        if (outputList.isEmpty()) return null;
        else {
            output.duration = this.tier * 20;
            return output;
        }
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new CustomRecipeLogic(this, this::getGTRecipe);
    }
}
