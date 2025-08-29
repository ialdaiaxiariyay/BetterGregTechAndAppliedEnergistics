package top.ialdaiaxiariyay.bettergtae.mixin.ae;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.ialdaiaxiariyay.bettergtae.BetterGTAE;

import java.util.List;

@Mixin(CraftingCPUCluster.class)
public abstract class CraftingCPUClusterMixin {

    @Shadow(remap = false)
    private long storage;

    @Shadow(remap = false)
    private int accelerator;

    @Shadow(remap = false)
    @Final
    private List<CraftingMonitorBlockEntity> status;

    @Shadow(remap = false)
    @Final
    private List<CraftingBlockEntity> blockEntities;

    @Shadow(remap = false)
    private MachineSource machineSrc;

    /**
     * @author iald
     * @reason delete te.getAcceleratorThreads() > 16 game collapse
     */
    @Overwrite(remap = false)
    void addBlockEntity(CraftingBlockEntity te) {
        if (this.machineSrc == null || te.isCoreBlock()) {
            this.machineSrc = new MachineSource(te);
        }
        te.setCoreBlock(false);
        te.saveChanges();
        this.blockEntities.add(0, te);
        if (te instanceof CraftingMonitorBlockEntity) {
            this.status.add((CraftingMonitorBlockEntity) te);
        }
        if (te.getStorageBytes() > 0L) {
            this.storage += te.getStorageBytes();
        }
        if (te.getAcceleratorThreads() > 0) {
            this.accelerator += te.getAcceleratorThreads();
        }
    }
}
