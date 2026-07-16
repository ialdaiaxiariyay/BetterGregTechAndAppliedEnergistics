package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.trait;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.CraftingPatternPartMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CraftingPatternInternalSlotRecipeHandler {

    @Getter
    private final List<RecipeHandlerList> slotHandlers;

    public CraftingPatternInternalSlotRecipeHandler(CraftingPatternPartMachine buffer,
                                                    CraftingPatternPartMachine.InternalSlot[] slots) {
        this.slotHandlers = new ArrayList<>(slots.length);
        for (int i = 0; i < slots.length; i++) {
            var rhl = new SlotRHL(buffer, slots[i], i);
            slotHandlers.add(rhl);
        }
    }

    @Getter
    protected static class SlotRHL extends RecipeHandlerList {

        private final SlotItemRecipeHandler itemRecipeHandler;

        public SlotRHL(CraftingPatternPartMachine buffer, CraftingPatternPartMachine.InternalSlot slot, int idx) {
            super(IO.IN);
            itemRecipeHandler = buffer.attachTrait(new SlotItemRecipeHandler(slot, idx));
            addHandlers(buffer.getCircuitSlot(),
                    itemRecipeHandler);
            this.setGroup(RecipeHandlerGroupDistinctness.BUS_DISTINCT);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public void setDistinct(boolean ignored, boolean notify) {}
    }

    @Getter
    private static class SlotItemRecipeHandler extends NotifiableRecipeHandlerTrait<Ingredient> {

        public static final MachineTraitType<SlotItemRecipeHandler> TYPE = new MachineTraitType<>(
                SlotItemRecipeHandler.class);

        @Override
        public @NotNull MachineTraitType<SlotItemRecipeHandler> getTraitType() {
            return TYPE;
        }

        private final CraftingPatternPartMachine.InternalSlot slot;
        private final int priority;

        private final int size = 81;
        private final RecipeCapability<Ingredient> capability = ItemRecipeCapability.CAP;
        private final IO handlerIO = IO.IN;
        private final boolean isDistinct = true;

        private SlotItemRecipeHandler(CraftingPatternPartMachine.InternalSlot slot, int index) {
            super();
            this.slot = slot;
            this.priority = IFilteredHandler.HIGH + index + 1;
            slot.setOnContentsChanged(this::notifyListeners);
        }

        @Override
        public @NotNull List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                           boolean simulate) {
            if (io != IO.IN || slot.isItemEmpty()) return left;
            return Objects.requireNonNull(slot.handleItemInternal(left, simulate));
        }

        @Override
        public @NotNull List<Object> getContents() {
            return new ArrayList<>(slot.getItems());
        }

        @Override
        public double getTotalContentAmount() {
            return slot.getItems().stream().mapToLong(ItemStack::getCount).sum();
        }
    }
}
