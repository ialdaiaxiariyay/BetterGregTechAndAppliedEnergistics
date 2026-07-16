package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.integration.ae2.slot.*;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.api.gui.MEInputDualConfigWidget;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 同时支持物品和流体的 ME Stocking 输入部件。
 * 合并了 MEStockingBusPartMachine 和 MEStockingHatchPartMachine 的所有功能。
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputDualPartMachine extends TieredIOPartMachine
                                    implements IGridConnectedMachine, IMEStockingPart, IDataStickInteractable,
                                    IMuiMachine, IDistinctPart {

    private static final int CONFIG_SIZE = 16;

    // 公共 Stocking 属性
    @SyncToClient
    @SaveField
    @Getter
    private boolean autoPull;

    @Getter
    @Setter
    @SaveField
    private int minStackSize = 1;

    @Getter
    @SaveField
    private int ticksPerCycle = 40;

    @Setter
    private Predicate<GenericStack> autoPullTest;

    // Distinct（仅物品）
    @Getter
    @SaveField
    private boolean distinct;

    // 电路槽
    @Getter
    @SaveField
    private final ProgrammableCircuitSlotTrait circuitSlot;

    // 物品处理器
    @Getter
    private final StockingItemList aeItemHandler;

    // 流体处理器
    @Getter
    private final StockingFluidList aeFluidHandler;

    // AE 网络
    @SaveField
    private final GridNodeHolder nodeHolder;

    @SyncToClient
    @Getter
    private boolean isOnline;

    protected final IActionSource actionSource;

    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;
    @Nullable
    protected ISubscription tankSubs;

    public MEInputDualPartMachine(BlockEntityCreationInfo info) {
        super(info, GTValues.UHV, IO.IN);
        this.autoPullTest = $ -> false;
        setOffsetBound(ticksPerCycle);

        this.aeItemHandler = new StockingItemList(CONFIG_SIZE);
        this.aeItemHandler.setMachine(this);
        attachTrait(aeItemHandler);

        this.aeFluidHandler = new StockingFluidList(this, CONFIG_SIZE);
        this.aeFluidHandler.setMachine(this);
        attachTrait(aeFluidHandler);

        this.circuitSlot = attachTrait(new ProgrammableCircuitSlotTrait());
        circuitSlot.setEnabled(io == IO.IN);

        this.nodeHolder = attachTrait(new GridNodeHolder(this));
        this.actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
    }

    // ------------------------- IGridConnectedMachine -------------------------
    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode(); // 返回 IManagedGridNode
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        updateSubscriptions();
    }

    public void setOnline(boolean online) {
        isOnline = online;
        syncDataHolder.markClientSyncFieldDirty("isOnline");
    }

    // ------------------------- Lifecycle -------------------------
    @Override
    public void onLoad() {
        super.onLoad();
        scheduleForNextServerTick(this::updateSubscriptions);
        getMainNode().setExposedOnSides(Collections.singleton(getFrontFacing()));
        inventorySubs = aeItemHandler.addChangedListener(this::updateSubscriptions);
        tankSubs = aeFluidHandler.addChangedListener(this::updateSubscriptions);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
        if (tankSubs != null) {
            tankSubs.unsubscribe();
            tankSubs = null;
        }
        flushInventory();
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        flushInventory();
    }

    // ------------------------- 多方块生命周期 -------------------------
    @Override
    public void addedToController(MultiblockControllerMachine controller, String name) {
        super.addedToController(controller, name);
        IMEStockingPart.super.addedToController(controller, name);
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        IMEStockingPart.super.removedFromController(controller);
        super.removedFromController(controller);
    }

    // ------------------------- Auto IO & Sync -------------------------
    public void autoIO() {
        if (!isWorkingEnabled()) return;
        if (!shouldSyncME()) return;

        if (updateMEStatus()) {
            if (ticksPerCycle == 0) ticksPerCycle = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;
            if (getOffsetTimer() % ticksPerCycle == 0) {
                if (autoPull) {
                    refreshItemList();
                    refreshFluidList();
                }
                syncItems();
                syncFluids();
            }
            updateSubscriptions();
        }
    }

    private void syncItems() {
        MEStorage networkInv = Objects.requireNonNull(getMainNode().getGrid()).getStorageService().getInventory();
        for (ExportOnlyAEItemSlot slot : aeItemHandler.getInventory()) {
            var config = slot.getConfig();
            if (config != null) {
                var key = config.what();
                long extracted = networkInv.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                if (extracted >= minStackSize) {
                    slot.setStock(new GenericStack(key, extracted));
                    continue;
                }
            }
            slot.setStock(null);
        }
    }

    private void syncFluids() {
        MEStorage networkInv = Objects.requireNonNull(getMainNode().getGrid()).getStorageService().getInventory();
        for (ExportOnlyAEFluidSlot slot : aeFluidHandler.getInventory()) {
            var config = slot.getConfig();
            if (config != null) {
                var key = config.what();
                long extracted = networkInv.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                if (extracted >= minStackSize) {
                    slot.setStock(new GenericStack(key, extracted));
                    continue;
                }
            }
            slot.setStock(null);
        }
    }

    private void refreshItemList() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            aeItemHandler.clearInventory(0);
            return;
        }

        MEStorage networkStorage = grid.getStorageService().getInventory();
        var counter = networkStorage.getAvailableStacks();

        PriorityQueue<Object2LongMap.Entry<AEKey>> top = new PriorityQueue<>(
                Comparator.comparingLong(Object2LongMap.Entry::getLongValue));

        for (Object2LongMap.Entry<AEKey> entry : counter) {
            long amount = entry.getLongValue();
            AEKey what = entry.getKey();
            if (amount <= 0) continue;
            if (!(what instanceof AEItemKey)) continue;

            long request = networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource);
            if (request == 0) continue;

            if (autoPullTest != null && !autoPullTest.test(new GenericStack(what, amount))) continue;
            if (amount >= minStackSize) {
                if (top.size() < CONFIG_SIZE) {
                    top.offer(entry);
                } else if (amount > top.peek().getLongValue()) {
                    top.poll();
                    top.offer(entry);
                }
            }
        }

        int count = top.size();
        int index;
        for (index = 0; index < CONFIG_SIZE; index++) {
            if (top.isEmpty()) break;
            Object2LongMap.Entry<AEKey> entry = top.poll();
            AEKey key = entry.getKey();
            long amount = entry.getLongValue();

            long extracted = networkStorage.extract(key, amount, Actionable.SIMULATE, actionSource);
            var slot = aeItemHandler.getInventory()[count - index - 1];
            slot.setConfig(new GenericStack(key, 1));
            slot.setStock(new GenericStack(key, extracted));
        }
        aeItemHandler.clearInventory(index);
    }

    private void refreshFluidList() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            aeFluidHandler.clearInventory(0);
            return;
        }

        MEStorage networkStorage = grid.getStorageService().getInventory();
        var counter = networkStorage.getAvailableStacks();

        PriorityQueue<Object2LongMap.Entry<AEKey>> top = new PriorityQueue<>(
                Comparator.comparingLong(Object2LongMap.Entry::getLongValue));

        for (Object2LongMap.Entry<AEKey> entry : counter) {
            long amount = entry.getLongValue();
            AEKey what = entry.getKey();
            if (amount <= 0) continue;
            if (!(what instanceof AEFluidKey)) continue;

            long request = networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource);
            if (request == 0) continue;

            if (autoPullTest != null && !autoPullTest.test(new GenericStack(what, amount))) continue;
            if (amount >= minStackSize) {
                if (top.size() < CONFIG_SIZE) {
                    top.offer(entry);
                } else if (amount > top.peek().getLongValue()) {
                    top.poll();
                    top.offer(entry);
                }
            }
        }

        int count = top.size();
        int index;
        for (index = 0; index < CONFIG_SIZE; index++) {
            if (top.isEmpty()) break;
            Object2LongMap.Entry<AEKey> entry = top.poll();
            AEKey key = entry.getKey();
            long amount = entry.getLongValue();

            long extracted = networkStorage.extract(key, amount, Actionable.SIMULATE, actionSource);
            var slot = aeFluidHandler.getInventory()[count - index - 1];
            slot.setConfig(new GenericStack(key, 1));
            slot.setStock(new GenericStack(key, extracted));
        }
        aeFluidHandler.clearInventory(index);
    }

    protected void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            for (var slot : aeItemHandler.getInventory()) {
                GenericStack stock = slot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            actionSource);
                }
            }
            for (var slot : aeFluidHandler.getInventory()) {
                GenericStack stock = slot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            actionSource);
                }
            }
        }
    }

    protected void updateSubscriptions() {
        boolean shouldSubscribe = isWorkingEnabled() && isOnline();
        if (shouldSubscribe) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateSubscriptions();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateSubscriptions();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(Collections.singleton(newFacing));
        updateSubscriptions();
    }

    // ------------------------- IMEStockingPart -------------------------
    @Override
    public IConfigurableSlotList getSlotList() {
        return aeItemHandler; // 默认使用物品列表
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null || !isFormed()) return false;
        if (distinct) return false;

        for (MultiblockControllerMachine controller : getControllers()) {
            for (MultiblockPartMachine part : controller.getParts()) {
                if (part instanceof MEInputDualPartMachine dual && dual != this) {
                    if (dual.aeItemHandler.hasStackInConfig(config, false) ||
                            dual.aeFluidHandler.hasStackInConfig(config, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("autoPull");
            if (!this.autoPull) {
                aeItemHandler.clearInventory(0);
                aeFluidHandler.clearInventory(0);
            } else if (updateMEStatus()) {
                refreshItemList();
                refreshFluidList();
                updateSubscriptions();
            }
        }
    }

    public void setTicksPerCycle(int ticksPerCycle) {
        this.ticksPerCycle = ticksPerCycle;
        setOffsetBound(ticksPerCycle);
    }

    // ------------------------- IDistinctPart -------------------------
    @Override
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("distinct");
            if (!distinct) {
                validateItemConfig();
            }
        }
    }

    @Override
    public boolean supportsDistinct() {
        return true;
    }

    private void validateItemConfig() {
        for (var slot : aeItemHandler.getInventory()) {
            var config = slot.getConfig();
            if (config != null && testConfiguredInOtherPart(config)) {
                slot.setConfig(null);
                slot.setStock(null);
            }
        }
    }

    // ------------------------- 交互 -------------------------
    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            setAutoPull(!autoPull);
            context.getPlayer().sendSystemMessage(
                    Component.translatable(autoPull ?
                            "gtceu.machine.me.stocking_auto_pull_enabled" :
                            "gtceu.machine.me.stocking_auto_pull_disabled"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    // ------------------------- 数据棒 -------------------------
    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = new CompoundTag();
            tag.put("ItemConfig", writeConfigToTag(aeItemHandler));
            tag.put("FluidConfig", writeConfigToTag(aeFluidHandler));
            tag.putBoolean("AutoPull", autoPull);
            tag.putInt("MinStackSize", minStackSize);
            tag.putInt("TicksPerCycle", ticksPerCycle);
            tag.putBoolean("Distinct", distinct);
            tag.putByte("GhostCircuit", (byte) circuitSlot.getCurrentCircuit());
            dataStick.setTag(tag);
            dataStick.setHoverName(Component.translatable("gtceu.machine.me.dual_stocking.data_stick.name"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getTag();
        if (tag == null || !(tag.contains("ItemConfig") && tag.contains("FluidConfig"))) {
            return InteractionResult.PASS;
        }
        if (!isRemote()) {
            readConfigFromTag(aeItemHandler, tag.getCompound("ItemConfig"));
            readConfigFromTag(aeFluidHandler, tag.getCompound("FluidConfig"));
            if (tag.contains("AutoPull")) setAutoPull(tag.getBoolean("AutoPull"));
            if (tag.contains("MinStackSize")) minStackSize = tag.getInt("MinStackSize");
            if (tag.contains("TicksPerCycle")) {
                ticksPerCycle = tag.getInt("TicksPerCycle");
                setOffsetBound(ticksPerCycle);
            }
            if (tag.contains("Distinct")) setDistinct(tag.getBoolean("Distinct"));
            if (tag.contains("GhostCircuit")) {
                circuitSlot.setCurrentCircuit(tag.getByte("GhostCircuit"));
            }
            updateSubscriptions();
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    // 配置序列化（使用具体类型）
    private CompoundTag writeConfigToTag(ExportOnlyAEItemList list) {
        CompoundTag tag = new CompoundTag();
        CompoundTag configStacks = new CompoundTag();
        tag.put("ConfigStacks", configStacks);
        var slots = list.getInventory();
        for (int i = 0; i < slots.length; i++) {
            GenericStack config = slots[i].getConfig();
            if (config != null) {
                configStacks.put(Integer.toString(i), GenericStack.writeTag(config));
            }
        }
        return tag;
    }

    private CompoundTag writeConfigToTag(ExportOnlyAEFluidList list) {
        CompoundTag tag = new CompoundTag();
        CompoundTag configStacks = new CompoundTag();
        tag.put("ConfigStacks", configStacks);
        var slots = list.getInventory();
        for (int i = 0; i < slots.length; i++) {
            GenericStack config = slots[i].getConfig();
            if (config != null) {
                configStacks.put(Integer.toString(i), GenericStack.writeTag(config));
            }
        }
        return tag;
    }

    private void readConfigFromTag(ExportOnlyAEItemList list, CompoundTag tag) {
        if (tag.contains("ConfigStacks")) {
            CompoundTag configStacks = tag.getCompound("ConfigStacks");
            var slots = list.getInventory();
            for (int i = 0; i < slots.length; i++) {
                String key = Integer.toString(i);
                if (configStacks.contains(key)) {
                    slots[i].setConfig(GenericStack.readTag(configStacks.getCompound(key)));
                } else {
                    slots[i].setConfig(null);
                }
            }
        }
    }

    private void readConfigFromTag(ExportOnlyAEFluidList list, CompoundTag tag) {
        if (tag.contains("ConfigStacks")) {
            CompoundTag configStacks = tag.getCompound("ConfigStacks");
            var slots = list.getInventory();
            for (int i = 0; i < slots.length; i++) {
                String key = Integer.toString(i);
                if (configStacks.contains(key)) {
                    slots[i].setConfig(GenericStack.readTag(configStacks.getCompound(key)));
                } else {
                    slots[i].setConfig(null);
                }
            }
        }
    }

    // ------------------------- GUI -------------------------
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        registerConfigActionsForPrefix(syncManager, "item");
        registerConfigActionsForPrefix(syncManager, "fluid");

        // 计算行数和尺寸
        int columns = 8;
        int rows = (int) Math.ceil((double) CONFIG_SIZE / columns);
        int width = columns * 18;                     // 每个槽 18px
        int height = rows * (18 * 2 + 2);             // 每个配对行高 = 36+2

        Flow flow = Flow.col().coverChildren().crossAxisAlignment(Alignment.CrossAxis.CENTER);

        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        // 物品区域
        flow.child(Text.dynamic(() -> Component.literal("Items")).asWidget());
        flow.child(new MEInputDualConfigWidget(aeItemHandler, CONFIG_SIZE, false, "item", this::isAutoPull)
                .withPrefix(syncManager)
                .size(width, height));   // 明确尺寸

        // 流体区域
        flow.child(Text.dynamic(() -> Component.literal("Fluids")).asWidget());
        flow.child(new MEInputDualConfigWidget(aeFluidHandler, CONFIG_SIZE, true, "fluid", this::isAutoPull)
                .withPrefix(syncManager)
                .size(width, height));

        mainWidget.child(flow);
    }

    private void registerConfigActionsForPrefix(PanelSyncManager syncManager, String prefix) {
        IConfigurableSlotList targetList = prefix.equals("item") ? aeItemHandler : aeFluidHandler;
        ExportOnlyAEItemList itemList = prefix.equals("item") ? aeItemHandler : null;
        syncManager.registerServerSyncedAction(prefix + "_set", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            var slot = targetList.getConfigurableSlot(index);
            var player = syncManager.getPlayer();
            ItemStack held = player.containerMenu.getCarried();
            if (prefix.equals("item")) {
                if (!held.isEmpty()) {
                    slot.setConfig(Objects.requireNonNull(GenericStack.fromItemStack(held)));
                    if (!distinct) validateItemConfig();
                }
            } else { // fluid
                FluidUtil.getFluidContained(held).ifPresent(fluid -> {
                    if (!fluid.isEmpty()) {
                        slot.setConfig(Objects.requireNonNull(AEUtil.fromFluidStack(fluid)));
                    }
                });
            }
        });

        syncManager.registerServerSyncedAction(prefix + "_clear", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            BetterGTAE.LOGGER.info("Received clear action for {} at index {}", prefix, index);
            boolean isFluid = prefix.equals("fluid");
            clearConfig(index, isFluid);
        });

        syncManager.registerServerSyncedAction(prefix + "_amount", packet -> {
            int index = packet.readVarInt();
            long amount = packet.readVarLong();
            if (index < 0 || index >= CONFIG_SIZE || amount <= 0) return;
            var slot = targetList.getConfigurableSlot(index);
            slot.getConfig();
            slot.setConfig(new GenericStack(slot.getConfig().what(), amount));
        });

        syncManager.registerServerSyncedAction(prefix + "_stock_pickup", packet -> {
            if (prefix.equals("fluid")) return;
            if (itemList == null) return;
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            ExportOnlyAEItemSlot slot = itemList.getInventory()[index];
            if (slot.getStock() != null && slot.getStock().what() instanceof AEItemKey key) {
                var player = syncManager.getPlayer();
                if (!player.containerMenu.getCarried().isEmpty()) return;
                ItemStack stack = key.toStack((int) Math.min(slot.getStock().amount(), Integer.MAX_VALUE));
                player.containerMenu.setCarried(stack);
                long remaining = slot.getStock().amount() - stack.getCount();
                slot.setStock(remaining > 0 ? new GenericStack(key, remaining) : null);
            }
        });

        syncManager.registerServerSyncedAction(prefix + "_set_ghost", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            boolean isFluidPacket = packet.readBoolean();

            if (prefix.equals("item")) {
                if (!isFluidPacket) {
                    ItemStack item = packet.readItem();
                    if (!item.isEmpty()) {
                        var slot = aeItemHandler.getInventory()[index];
                        slot.setConfig(GenericStack.fromItemStack(item));
                        if (!distinct) validateItemConfig();
                    }
                }
            } else { // fluid
                if (isFluidPacket) {
                    FluidStack fluid = FluidStack.readFromPacket(packet);
                    if (!fluid.isEmpty()) {
                        aeFluidHandler.getInventory()[index].setConfig(AEUtil.fromFluidStack(fluid));
                    }
                }
            }
        });
    }

    public void clearConfig(int index, boolean isFluid) {
        if (index < 0 || index >= CONFIG_SIZE) return;
        if (isFluid) {
            aeFluidHandler.getInventory()[index].setConfig(null);
            aeFluidHandler.onContentsChanged();
        } else {
            aeItemHandler.getInventory()[index].setConfig(null);
            aeItemHandler.onContentsChanged();
            if (!distinct) validateItemConfig();
        }
        markAsChanged();
    }

    // ------------------------- 内部 Handler 类 -------------------------

    private static class StockingItemList extends ExportOnlyAEItemList {

        @Getter
        @Setter
        @Nullable
        private MEInputDualPartMachine machine;

        public StockingItemList(int slots) {
            super(slots, StockingItemSlot::new);
        }

        @Override
        public boolean isAutoPull() {
            return machine != null && machine.isAutoPull();
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            if (super.hasStackInConfig(stack, false)) return true;
            if (checkExternal && machine != null) {
                return machine.testConfiguredInOtherPart(stack);
            }
            return false;
        }
    }

    private static class StockingFluidList extends ExportOnlyAEFluidList {

        @Getter
        @Setter
        @Nullable
        private MEInputDualPartMachine machine;

        public StockingFluidList(MEInputDualPartMachine holder, int slots) {
            super(holder, slots, StockingFluidSlot::new);
        }

        @Override
        public boolean isAutoPull() {
            return machine != null && machine.isAutoPull();
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            if (super.hasStackInConfig(stack, false)) return true;
            if (checkExternal && machine != null) {
                return machine.testConfiguredInOtherPart(stack);
            }
            return false;
        }
    }

    private static class StockingItemSlot extends ExportOnlyAEItemSlot {

        @Getter
        @Setter
        @Nullable
        private MEInputDualPartMachine machine;

        public StockingItemSlot() {
            super();
        }

        public StockingItemSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 && stock != null && config != null && machine != null && machine.isOnline()) {
                MEStorage aeNetwork = Objects.requireNonNull(machine.getMainNode().getGrid()).getStorageService()
                        .getInventory();
                Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                var key = config.what();
                long extracted = aeNetwork.extract(key, amount, action, machine.actionSource);
                if (extracted > 0) {
                    ItemStack result = key instanceof AEItemKey itemKey ? itemKey.toStack((int) extracted) :
                            ItemStack.EMPTY;
                    if (!simulate) {
                        this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                        if (this.stock.amount() == 0) this.stock = null;
                        onContentsChanged.run();
                    }
                    return result;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public StockingItemSlot copy() {
            return new StockingItemSlot(
                    config == null ? null : copy(config),
                    stock == null ? null : copy(stock));
        }
    }

    private static class StockingFluidSlot extends ExportOnlyAEFluidSlot {

        @Getter
        @Setter
        @Nullable
        private MEInputDualPartMachine machine;

        public StockingFluidSlot() {
            super();
        }

        public StockingFluidSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (stock != null && config != null && machine != null && machine.isOnline()) {
                MEStorage aeNetwork = Objects.requireNonNull(machine.getMainNode().getGrid()).getStorageService()
                        .getInventory();
                Actionable actionable = action.simulate() ? Actionable.SIMULATE : Actionable.MODULATE;
                var key = config.what();
                long extracted = aeNetwork.extract(key, maxDrain, actionable, machine.actionSource);
                if (extracted > 0) {
                    FluidStack result = key instanceof AEFluidKey fluidKey ?
                            AEUtil.toFluidStack(fluidKey, extracted) : FluidStack.EMPTY;
                    if (action.execute()) {
                        this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                        if (this.stock.amount() == 0) this.stock = null;
                        onContentsChanged.run();
                    }
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public StockingFluidSlot copy() {
            return new StockingFluidSlot(
                    config == null ? null : copy(config),
                    stock == null ? null : copy(stock));
        }
    }
}
