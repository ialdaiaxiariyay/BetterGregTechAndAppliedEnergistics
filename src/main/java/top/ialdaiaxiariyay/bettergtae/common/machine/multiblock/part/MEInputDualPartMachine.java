package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputDualPartMachine extends MEBusPartMachine implements IDataStickInteractable{

    protected static final int CONFIG_SIZE = 9;

    protected final ExportOnlyAEItemList aeItemHandler;
    protected final ExportOnlyAEFluidList aeFluidHandler;

    public MEInputDualPartMachine(BlockEntityCreationInfo info) {
        super(info, IO.IN,new NotifiableItemStackHandler(9, IO.IN, IO.NONE));
        this.aeItemHandler = new ExportOnlyAEItemList(CONFIG_SIZE);
        this.aeFluidHandler = new ExportOnlyAEFluidList(this, CONFIG_SIZE);
    }

    // 获取所有配置槽（用于刷新等）
    protected ExportOnlyAESlot[] getAllSlots() {
        ExportOnlyAESlot[] itemSlots = aeItemHandler.getInventory();
        ExportOnlyAESlot[] fluidSlots = aeFluidHandler.getInventory();
        ExportOnlyAESlot[] all = new ExportOnlyAESlot[itemSlots.length + fluidSlots.length];
        System.arraycopy(itemSlots, 0, all, 0, itemSlots.length);
        System.arraycopy(fluidSlots, 0, all, itemSlots.length, fluidSlots.length);
        return all;
    }

    // 自动同步
    @Override
    public void autoIO() {
        if (!isWorkingEnabled()) return;
        if (!shouldSyncME()) return;
        if (updateMEStatus()) {
            syncItems();
            syncFluids();
            updateInventorySubscription();
        }
    }

    protected void syncItems() {
        MEStorage networkInv = Objects.requireNonNull(getMainNode().getGrid()).getStorageService().getInventory();
        for (var aeSlot : aeItemHandler.getInventory()) {
            GenericStack exceed = aeSlot.exceedStack();
            if (exceed != null) {
                long total = exceed.amount();
                long inserted = networkInv.insert(exceed.what(), exceed.amount(), Actionable.MODULATE, actionSource);
                if (inserted > 0) {
                    aeSlot.extractItem(0, GTMath.saturatedCast(inserted), false);
                    continue;
                } else {
                    aeSlot.extractItem(0, GTMath.saturatedCast(total), false);
                }
            }
            GenericStack request = aeSlot.requestStack();
            if (request != null) {
                long extracted = networkInv.extract(request.what(), request.amount(), Actionable.MODULATE,
                        actionSource);
                if (extracted != 0) {
                    aeSlot.addStack(new GenericStack(request.what(), extracted));
                }
            }
        }
    }

    protected void syncFluids() {
        MEStorage networkInv = Objects.requireNonNull(getMainNode().getGrid()).getStorageService().getInventory();
        for (var aeSlot : aeFluidHandler.getInventory()) {
            GenericStack exceed = aeSlot.exceedStack();
            if (exceed != null) {
                int total = GTMath.saturatedCast(exceed.amount());
                int inserted = GTMath
                        .saturatedCast(networkInv.insert(exceed.what(), exceed.amount(), Actionable.MODULATE,
                                actionSource));
                if (inserted > 0) {
                    aeSlot.drain(inserted, IFluidHandler.FluidAction.EXECUTE);
                    continue;
                } else {
                    aeSlot.drain(total, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            GenericStack request = aeSlot.requestStack();
            if (request != null) {
                long extracted = networkInv.extract(request.what(), request.amount(), Actionable.MODULATE,
                        actionSource);
                if (extracted > 0) {
                    aeSlot.addStack(new GenericStack(request.what(), extracted));
                }
            }
        }
    }

    @Override
    public void onMachineDestroyed() {
        flushInventory();
    }

    protected void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            for (var slot : getAllSlots()) {
                GenericStack stock = slot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            actionSource);
                }
            }
        }
    }

    // UI
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        registerConfigActions(syncManager);

        var flow = Flow.col().coverChildren();

        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                        Component.translatable("gtceu.gui.me_network.online") :
                        Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        // 计算合适宽度：9个槽位 × 18px = 162px，留出边距
        int widgetWidth = CONFIG_SIZE * 18; // 162
        int widgetHeight = 2 * (18 * 2 + 2); // 保持原有高度

        var itemConfig = new AEConfigWidget(aeItemHandler, CONFIG_SIZE, false)
                .syncManager(syncManager)
                .size(widgetWidth, widgetHeight);
        var fluidConfig = new AEConfigWidget(aeFluidHandler, CONFIG_SIZE, false)
                .syncManager(syncManager)
                .size(widgetWidth, widgetHeight);

        // 垂直排列，上下留间隔
        flow.child(itemConfig.marginBottom(2));
        flow.child(fluidConfig);

        mainWidget.child(flow.center());
    }

    protected void registerConfigActions(PanelSyncManager syncManager) {
        // 通用清空和数量调整
        syncManager.registerServerSyncedAction("ae_config_clear", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE * 2) return;
            ExportOnlyAESlot slot = getAllSlots()[index];
            slot.setConfig(null);
        });

        syncManager.registerServerSyncedAction("ae_config_amount", packet -> {
            int index = packet.readVarInt();
            long amount = packet.readVarLong();
            if (index < 0 || index >= CONFIG_SIZE * 2) return;
            ExportOnlyAESlot slot = getAllSlots()[index];
            if (slot.getConfig() != null && amount > 0) {
                slot.setConfig(ExportOnlyAESlot.copy(slot.getConfig(), amount));
            }
        });

        // 设置配置（物品/流体）
        syncManager.registerServerSyncedAction("ae_config_set", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE * 2) return;
            if (index < CONFIG_SIZE) {
                // 物品
                var slot = aeItemHandler.getInventory()[index];
                var player = syncManager.getPlayer();
                ItemStack held = player.containerMenu.getCarried();
                if (!held.isEmpty()) {
                    slot.setConfig(GenericStack.fromItemStack(held));
                }
            } else {
                // 流体
                int fluidIdx = index - CONFIG_SIZE;
                var slot = aeFluidHandler.getInventory()[fluidIdx];
                var player = syncManager.getPlayer();
                ItemStack held = player.containerMenu.getCarried();
                FluidUtil.getFluidContained(held).ifPresent(fluid -> {
                    slot.setConfig(AEUtil.fromFluidStack(fluid));
                });
            }
        });

        // 设置幽灵配置（用于 JEI 拖拽）
        syncManager.registerServerSyncedAction("ae_config_set_ghost", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE * 2) return;
            boolean isFluid = packet.readBoolean();
            if (index < CONFIG_SIZE) {
                if (!isFluid) {
                    ItemStack item = packet.readItem();
                    if (!item.isEmpty()) {
                        aeItemHandler.getInventory()[index].setConfig(GenericStack.fromItemStack(item));
                    }
                }
            } else {
                int fluidIdx = index - CONFIG_SIZE;
                if (isFluid) {
                    FluidStack fluid = FluidStack.readFromPacket(packet);
                    if (!fluid.isEmpty()) {
                        aeFluidHandler.getInventory()[fluidIdx].setConfig(AEUtil.fromFluidStack(fluid));
                    }
                }
            }
        });
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = new CompoundTag();
            tag.put("MEInputDual", writeConfigToTag());
            dataStick.setTag(tag);
            dataStick.setHoverName(Component.translatable("bettergtae.machine.me.dual_stocking.data_stick.name"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getTag();
        if (tag == null || !tag.contains("MEInputDual")) {
            return InteractionResult.PASS;
        }
        if (!isRemote()) {
            readConfigFromTag(tag.getCompound("MEInputDual"));
            updateInventorySubscription();
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag itemConfig = new CompoundTag();
        CompoundTag fluidConfig = new CompoundTag();
        tag.put("ItemConfig", itemConfig);
        tag.put("FluidConfig", fluidConfig);

        for (int i = 0; i < CONFIG_SIZE; i++) {
            var itemSlot = aeItemHandler.getInventory()[i];
            if (itemSlot.getConfig() != null) {
                itemConfig.put(Integer.toString(i), GenericStack.writeTag(itemSlot.getConfig()));
            }
            var fluidSlot = aeFluidHandler.getInventory()[i];
            if (fluidSlot.getConfig() != null) {
                fluidConfig.put(Integer.toString(i), GenericStack.writeTag(fluidSlot.getConfig()));
            }
        }
        tag.putByte("GhostCircuit",
                (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitSlot.storage.getStackInSlot(0)));
        tag.putBoolean("DistinctBuses", isDistinct());
        return tag;
    }

    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.contains("ItemConfig")) {
            CompoundTag itemConfig = tag.getCompound("ItemConfig");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (itemConfig.contains(key)) {
                    aeItemHandler.getInventory()[i].setConfig(GenericStack.readTag(itemConfig.getCompound(key)));
                } else {
                    aeItemHandler.getInventory()[i].setConfig(null);
                }
            }
        }
        if (tag.contains("FluidConfig")) {
            CompoundTag fluidConfig = tag.getCompound("FluidConfig");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (fluidConfig.contains(key)) {
                    aeFluidHandler.getInventory()[i].setConfig(GenericStack.readTag(fluidConfig.getCompound(key)));
                } else {
                    aeFluidHandler.getInventory()[i].setConfig(null);
                }
            }
        }
        if (tag.contains("GhostCircuit")) {
            circuitSlot.storage.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
        }
        if (tag.contains("DistinctBuses")) {
            setDistinct(tag.getBoolean("DistinctBuses"));
        }
    }
}
