package top.ialdaiaxiariyay.bettergtae.common.data.machine;

import top.ialdaiaxiariyay.bettergtae.common.data.BGTAECreativeModeTabs;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.ExtendMEPatternBufferPartMachine;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.ExtendMEPatternBufferProxyPartMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.LuV;
import static top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate.REGISTRATE;

public class BGTAEMachines {

    public static void init() {}

    static {
        REGISTRATE.creativeModeTab(() -> BGTAECreativeModeTabs.MACHINE);
    }

    public static final MachineDefinition EXTEND_ME_PATTERN_BUFFER = REGISTRATE
            .machine("extend_me_pattern_buffer", ExtendMEPatternBufferPartMachine::new)
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                    PartAbility.EXPORT_ITEMS)
            .rotationState(RotationState.ALL)
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
            .tooltips(
                    Component.translatable("block.gtceu.pattern_buffer.desc.0"),
                    Component.translatable("block.gtceu.pattern_buffer.desc.1"),
                    Component.translatable("block.gtceu.pattern_buffer.desc.2"),
                    Component.translatable("gtceu.part_sharing.enabled"),
                    Component.translatable("bettergtae.extend_me_pattern_buffer.tips.0"))
            .register();

    public static final MachineDefinition EXTEND_ME_PATTERN_BUFFER_PROXY = REGISTRATE
            .machine("extend_me_pattern_buffer_proxy", ExtendMEPatternBufferProxyPartMachine::new)
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                    PartAbility.EXPORT_ITEMS)
            .rotationState(RotationState.ALL)
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch_proxy"))
            .tooltips(
                    Component.translatable("block.gtceu.pattern_buffer_proxy.desc.0"),
                    Component.translatable("block.gtceu.pattern_buffer_proxy.desc.1"),
                    Component.translatable("block.gtceu.pattern_buffer_proxy.desc.2"),
                    Component.translatable("gtceu.part_sharing.enabled"))
            .register();
}
