package top.ialdaiaxiariyay.bettergtae.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.integration.jade.provider.MachineInfoProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.ExtendMEPatternBufferProxyPartMachine;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendMEPatternBufferProxyProvider extends
                                                MachineInfoProvider<ExtendMEPatternBufferProxyPartMachine, CompoundTag> {

    public ExtendMEPatternBufferProxyProvider() {
        super(BetterGTAE.id("extend_me_pattern_buffer_proxy"), ExtendMEPatternBufferProxyPartMachine.class);
    }

    @Override
    protected CompoundTag write(ExtendMEPatternBufferProxyPartMachine proxy) {
        var compoundTag = new CompoundTag();
        if (!proxy.isFormed()) {
            compoundTag.putBoolean("formed", false);
            return compoundTag;
        }
        compoundTag.putBoolean("formed", true);
        var buffer = proxy.getBuffer();
        if (buffer == null) {
            compoundTag.putBoolean("bound", false);
            return compoundTag;
        }
        compoundTag.putBoolean("bound", true);

        var pos = buffer.getBlockPos();
        compoundTag.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        ExtendMEPatternBufferProvider.writeBufferTag(compoundTag, buffer);
        return compoundTag;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!data.getBoolean("formed")) return;
        if (!data.getBoolean("bound")) {
            tooltip.add(Component.translatable("gtceu.top.buffer_not_bound").withStyle(ChatFormatting.RED));
            return;
        }

        int[] pos = data.getIntArray("pos");
        tooltip.add(Component.translatable("gtceu.top.buffer_bound_pos", pos[0], pos[1], pos[2])
                .withStyle(TooltipHelper.RAINBOW_HSL_SLOW));

        ExtendMEPatternBufferProvider.readBufferTag(tooltip, data);
    }
}
