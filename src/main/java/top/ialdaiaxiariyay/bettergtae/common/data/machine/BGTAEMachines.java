package top.ialdaiaxiariyay.bettergtae.common.data.machine;

import top.ialdaiaxiariyay.bettergtae.common.data.BGTAECreativeModeTabs;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.ExtendMEPatternBufferPartMachine;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.ExtendMEPatternBufferProxyPartMachine;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part.MEDualHatchPartMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.machine.multiblock.part.CleaningMaintenanceHatchPartMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate.REGISTRATE;

public class BGTAEMachines {

    public static void init() {
        BGTAEMultiblockA.init();
    }

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

    public static final MachineDefinition ME_STOCKING_DUAL_INPUT_HATCH = REGISTRATE
            .machine("me_stocking_dual_input_hatch", MEDualHatchPartMachine::new)
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS)
            .rotationState(RotationState.ALL)
            .tooltips(
                    Component.translatable("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.0"),
                    Component.translatable("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.1"),
                    Component.translatable("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.2"),
                    Component.translatable("gtceu.machine.me.copy_paste.tooltip"),
                    Component.translatable("bettergtae.machine.me_stocking_dual_input_hatch.tooltip.3"),
                    Component.translatable("gtceu.part_sharing.enabled"))
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_input_bus"))
            .register();

    public static final MachineDefinition STERILE_CLEANING_MAINTENANCE_HATCH = REGISTRATE
            .machine("cleanroom_sterile_maintenance_hatch",
                    holder -> new CleaningMaintenanceHatchPartMachine(holder, CleanroomType.STERILE_CLEANROOM))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.0"),
                    Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.1"))
            .tooltipBuilder((stack, tooltips) -> {
                tooltips.add(Component.literal("  ").append(Component
                        .translatable(CleanroomType.STERILE_CLEANROOM.getTranslationKey()).withStyle(ChatFormatting.GREEN)));
            })
            .overlayTieredHullModel(GTCEu.id("block/machine/part/cleaning_maintenance_hatch"))
            .tier(ZPM)
            .register();
}
