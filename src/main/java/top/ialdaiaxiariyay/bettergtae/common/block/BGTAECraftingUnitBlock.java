package top.ialdaiaxiariyay.bettergtae.common.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import appeng.block.crafting.CraftingUnitBlock;
import appeng.block.crafting.ICraftingUnitType;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.bettergtae.BetterGTAE;

import java.util.List;

public class BGTAECraftingUnitBlock extends CraftingUnitBlock {

    public BGTAECraftingUnitBlock(ICraftingUnitType type) {
        super(type);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (BetterGTAE.IsLoadMixin()) return;
        tooltip.add(Component.translatable("bettergtae.crafting_unit_tips.0"));
    }
}
