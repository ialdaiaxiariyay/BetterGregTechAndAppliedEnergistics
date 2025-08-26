package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.AutoStockingFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.*;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDualHatchPartMachine extends MEBusPartMachine
        implements IDataStickInteractable, IMachineLife, IHasCircuitSlot, IMEStockingPart {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEDualHatchPartMachine.class,
            MEBusPartMachine.MANAGED_FIELD_HOLDER);
    protected final static int CONFIG_SIZE = 16;

    @Persisted
    protected ExportOnlyAEItemList aeItemHandler;

    @Persisted
    protected ExportOnlyAEFluidList aeFluidHandler;

    @DescSynced
    @Persisted
    @Getter
    private boolean autoPull;

    @Getter
    @Setter
    @Persisted
    @DropSaved
    private int minStackSize = 1;

    @Getter
    @Setter
    @Persisted
    @DropSaved
    private int ticksPerCycle = 40;

    @Setter
    private Predicate<GenericStack> autoPullTest;

    public MEDualHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
        this.autoPullTest = $ -> false;
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void onMachineRemoved() {
        flushInventory();
    }

    @Override
    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.aeItemHandler = new ExportOnlyAEStockingItemList(this, CONFIG_SIZE);
        this.aeFluidHandler = new ExportOnlyAEStockingFluidList(this, CONFIG_SIZE);
        return this.aeItemHandler;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            if (autoPull) {
                refreshList();
            }
            this.syncME();
            this.updateInventorySubscription();
        }
    }

    protected void syncME() {
        MEStorage networkInv = Objects.requireNonNull(this.getMainNode().getGrid()).getStorageService().getInventory();

        // Sync items
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            updateSlotStock(networkInv, aeSlot);
        }

        // Sync fluids
        for (ExportOnlyAEFluidSlot aeSlot : this.aeFluidHandler.getInventory()) {
            updateSlotStock(networkInv, aeSlot);
        }
    }

    private void updateSlotStock(MEStorage networkInv, ExportOnlyAESlot slot) {
        var config = slot.getConfig();
        if (config != null) {
            var key = config.what();
            long extracted = networkInv.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
            if (extracted >= minStackSize) {
                slot.setStock(new GenericStack(key, extracted));
                return;
            }
        }
        slot.setStock(null);
    }

    protected void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            // Flush items
            for (var aeSlot : aeItemHandler.getInventory()) {
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            actionSource);
                }
            }

            // Flush fluids
            for (var aeSlot : aeFluidHandler.getInventory()) {
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            actionSource);
                }
            }
        }
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Item config slots
        group.addWidget(new AEItemConfigWidget(3, 10, this.aeItemHandler));

        // Fluid config slots
        group.addWidget(new AEFluidConfigWidget(3, 84, this.aeFluidHandler));

        return group;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this); // removes the cover configurator, it's pointless and clashes with layout.
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IMEStockingPart.super.attachConfigurators(configuratorPanel);
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new AutoStockingFancyConfigurator(this));
    }

    ////////////////////////////////
    // ******* Interaction *******//
    ////////////////////////////////

    @Override
    public final InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!isRemote()) {
            CompoundTag tag = new CompoundTag();
            tag.put("MEDualStocking", writeConfigToTag());
            dataStick.setTag(tag);
            dataStick.setHoverName(Component.translatable("bettergtae.machine.me.dual_stocking.data_stick.name"));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public final InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getTag();
        if (tag == null || !tag.contains("MEDualStocking")) {
            return InteractionResult.PASS;
        }

        if (!isRemote()) {
            readConfigFromTag(tag.getCompound("MEDualStocking"));
            this.updateInventorySubscription();
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!isRemote()) {
            setAutoPull(!autoPull);
            if (autoPull) {
                playerIn.sendSystemMessage(
                        Component.translatable("gtceu.machine.me.stocking_auto_pull_enabled"));
            } else {
                playerIn.sendSystemMessage(
                        Component.translatable("gtceu.machine.me.stocking_auto_pull_disabled"));
            }
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    ////////////////////////////////
    // ****** Configuration ******//
    ////////////////////////////////

    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = new CompoundTag();

        if (!autoPull) {
            // Write item configs
            CompoundTag itemConfigStacks = new CompoundTag();
            tag.put("ItemConfigStacks", itemConfigStacks);
            for (int i = 0; i < CONFIG_SIZE; i++) {
                var slot = this.aeItemHandler.getInventory()[i];
                GenericStack config = slot.getConfig();
                if (config == null) {
                    continue;
                }
                CompoundTag stackTag = GenericStack.writeTag(config);
                itemConfigStacks.put(Integer.toString(i), stackTag);
            }

            // Write fluid configs
            CompoundTag fluidConfigStacks = new CompoundTag();
            tag.put("FluidConfigStacks", fluidConfigStacks);
            for (int i = 0; i < CONFIG_SIZE; i++) {
                var slot = this.aeFluidHandler.getInventory()[i];
                GenericStack config = slot.getConfig();
                if (config == null) {
                    continue;
                }
                CompoundTag stackTag = GenericStack.writeTag(config);
                fluidConfigStacks.put(Integer.toString(i), stackTag);
            }

            tag.putBoolean("AutoPull", false);
        } else {
            tag.putBoolean("AutoPull", true);
        }

        tag.putByte("GhostCircuit",
                (byte) IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0)));
        tag.putBoolean("DistinctBuses", isDistinct());
        return tag;
    }

    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.contains("AutoPull") && tag.getBoolean("AutoPull")) {
            this.setAutoPull(true);
        } else {
            this.setAutoPull(false);

            // Read item configs
            if (tag.contains("ItemConfigStacks")) {
                CompoundTag configStacks = tag.getCompound("ItemConfigStacks");
                for (int i = 0; i < CONFIG_SIZE; i++) {
                    String key = Integer.toString(i);
                    if (configStacks.contains(key)) {
                        CompoundTag configTag = configStacks.getCompound(key);
                        this.aeItemHandler.getInventory()[i].setConfig(GenericStack.readTag(configTag));
                    } else {
                        this.aeItemHandler.getInventory()[i].setConfig(null);
                    }
                }
            }

            // Read fluid configs
            if (tag.contains("FluidConfigStacks")) {
                CompoundTag configStacks = tag.getCompound("FluidConfigStacks");
                for (int i = 0; i < CONFIG_SIZE; i++) {
                    String key = Integer.toString(i);
                    if (configStacks.contains(key)) {
                        CompoundTag configTag = configStacks.getCompound(key);
                        this.aeFluidHandler.getInventory()[i].setConfig(GenericStack.readTag(configTag));
                    } else {
                        this.aeFluidHandler.getInventory()[i].setConfig(null);
                    }
                }
            }
        }

        if (tag.contains("GhostCircuit")) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
        }
        if (tag.contains("DistinctBuses")) {
            setDistinct(tag.getBoolean("DistinctBuses"));
        }
    }

    /////////////////////////////////
    // ***** IMEStockingPart *****//
    /////////////////////////////////

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        IMEStockingPart.super.addedToController(controller);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        IMEStockingPart.super.removedFromController(controller);
        super.removedFromController(controller);
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        super.setDistinct(isDistinct);
        if (!isRemote() && !isDistinct) {
            validateConfig();
        }
    }

    @Override
    public IConfigurableSlotList getSlotList() {
        // Return the primary slot list (items)
        return aeItemHandler;
    }

    public IConfigurableSlotList getFluidSlotList() {
        return aeFluidHandler;
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        if (!isFormed()) return false;

        // Check both items and fluids in other parts
        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof MEDualHatchPartMachine dualPart) {
                    if (dualPart == this) continue;

                    // Check if the same type of resource is configured in another part
                    if (config.what() instanceof AEItemKey) {
                        if (dualPart.aeItemHandler.hasStackInConfig(config, false)) {
                            return true;
                        }
                    } else if (config.what() instanceof AEFluidKey) {
                        if (dualPart.aeFluidHandler.hasStackInConfig(config, false)) {
                            return true;
                        }
                    }
                } else if (part instanceof MEStockingBusPartMachine busPart && config.what() instanceof AEItemKey) {
                    if (busPart.getSlotList().hasStackInConfig(config, false)) {
                        return true;
                    }
                } else if (part instanceof MEStockingHatchPartMachine hatchPart && config.what() instanceof AEFluidKey) {
                    if (hatchPart.getSlotList().hasStackInConfig(config, false)) {
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
            if (!this.autoPull) {
                this.aeItemHandler.clearInventory(0);
                this.aeFluidHandler.clearInventory(0);
            } else if (updateMEStatus()) {
                this.refreshList();
                updateInventorySubscription();
            }
        }
    }

    /**
     * Refresh the configuration list in auto-pull mode.
     * Sets the config to the CONFIG_SIZE items/fluids with the highest amount in the ME system.
     */
    private void refreshList() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            aeItemHandler.clearInventory(0);
            aeFluidHandler.clearInventory(0);
            return;
        }

        MEStorage networkStorage = grid.getStorageService().getInventory();
        var counter = networkStorage.getAvailableStacks();

        // Use PriorityQueues to sort the stacks by size
        PriorityQueue<Object2LongMap.Entry<AEKey>> topItems = new PriorityQueue<>(
                Comparator.comparingLong(Object2LongMap.Entry<AEKey>::getLongValue));
        PriorityQueue<Object2LongMap.Entry<AEKey>> topFluids = new PriorityQueue<>(
                Comparator.comparingLong(Object2LongMap.Entry<AEKey>::getLongValue));

        for (Object2LongMap.Entry<AEKey> entry : counter) {
            long amount = entry.getLongValue();
            AEKey what = entry.getKey();

            if (amount <= 0) continue;

            long request = networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource);
            if (request == 0) continue;

            // Ensure that it is valid to configure with this stack
            if (autoPullTest != null && !autoPullTest.test(new GenericStack(what, amount))) continue;

            if (amount >= minStackSize) {
                if (what instanceof AEItemKey) {
                    if (topItems.size() < CONFIG_SIZE) {
                        topItems.offer(entry);
                    } else if (amount > topItems.peek().getLongValue()) {
                        topItems.poll();
                        topItems.offer(entry);
                    }
                } else if (what instanceof AEFluidKey) {
                    if (topFluids.size() < CONFIG_SIZE) {
                        topFluids.offer(entry);
                    } else if (amount > topFluids.peek().getLongValue()) {
                        topFluids.poll();
                        topFluids.offer(entry);
                    }
                }
            }
        }

        // Process items
        int index;
        int itemAmount = topItems.size();
        for (index = 0; index < CONFIG_SIZE; index++) {
            if (topItems.isEmpty()) break;
            Object2LongMap.Entry<AEKey> entry = topItems.poll();
            AEKey what = entry.getKey();
            long amount = entry.getLongValue();

            long request = networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource);

            // Since we want our items to be displayed from highest to lowest, but poll() returns
            // the lowest first, we fill in the slots starting at itemAmount-1
            var slot = this.aeItemHandler.getInventory()[itemAmount - index - 1];
            slot.setConfig(new GenericStack(what, 1));
            slot.setStock(new GenericStack(what, request));
        }
        aeItemHandler.clearInventory(index);

        // Process fluids
        index = 0;
        int fluidAmount = topFluids.size();
        for (index = 0; index < CONFIG_SIZE; index++) {
            if (topFluids.isEmpty()) break;
            Object2LongMap.Entry<AEKey> entry = topFluids.poll();
            AEKey what = entry.getKey();
            long amount = entry.getLongValue();

            long request = networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource);

            var slot = this.aeFluidHandler.getInventory()[fluidAmount - index - 1];
            slot.setConfig(new GenericStack(what, 1));
            slot.setStock(new GenericStack(what, request));
        }
        aeFluidHandler.clearInventory(index);
    }

    // Item-specific handler and slot
    private class ExportOnlyAEStockingItemList extends ExportOnlyAEItemList {

        public ExportOnlyAEStockingItemList(MEDualHatchPartMachine holder, int slots) {
            super(holder, slots, ExportOnlyAEStockingItemSlot::new);
        }

        @Override
        public boolean isAutoPull() {
            return autoPull;
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            boolean inThisBus = super.hasStackInConfig(stack, false);
            if (inThisBus) return true;
            if (checkExternal) {
                return testConfiguredInOtherPart(stack);
            }
            return false;
        }
    }

    private class ExportOnlyAEStockingItemSlot extends ExportOnlyAEItemSlot {

        public ExportOnlyAEStockingItemSlot() {
            super();
        }

        public ExportOnlyAEStockingItemSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 && this.stock != null) {
                if (this.config != null) {
                    if (!isOnline()) return ItemStack.EMPTY;
                    MEStorage aeNetwork = Objects.requireNonNull(getMainNode().getGrid()).getStorageService().getInventory();

                    Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                    var key = config.what();
                    long extracted = aeNetwork.extract(key, amount, action, actionSource);

                    if (extracted > 0) {
                        ItemStack resultStack = key instanceof AEItemKey itemKey ?
                                itemKey.toStack((int) extracted) : ItemStack.EMPTY;
                        if (!simulate) {
                            this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                            if (this.stock.amount() == 0) {
                                this.stock = null;
                            }
                            if (this.onContentsChanged != null) {
                                this.onContentsChanged.run();
                            }
                        }
                        return resultStack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ExportOnlyAEStockingItemSlot copy() {
            return new ExportOnlyAEStockingItemSlot(
                    this.config == null ? null : copy(this.config),
                    this.stock == null ? null : copy(this.stock));
        }
    }

    // Fluid-specific handler and slot
    private class ExportOnlyAEStockingFluidList extends ExportOnlyAEFluidList {

        public ExportOnlyAEStockingFluidList(MEDualHatchPartMachine holder, int slots) {
            super(holder, slots, ExportOnlyAEStockingFluidSlot::new);
        }

        @Override
        public boolean isAutoPull() {
            return autoPull;
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            boolean inThisHatch = super.hasStackInConfig(stack, false);
            if (inThisHatch) return true;
            if (checkExternal) {
                return testConfiguredInOtherPart(stack);
            }
            return false;
        }
    }

    private class ExportOnlyAEStockingFluidSlot extends ExportOnlyAEFluidSlot {

        public ExportOnlyAEStockingFluidSlot() {
            super();
        }

        public ExportOnlyAEStockingFluidSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public ExportOnlyAEFluidSlot copy() {
            return new ExportOnlyAEStockingFluidSlot(
                    this.config == null ? null : copy(this.config),
                    this.stock == null ? null : copy(this.stock));
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (this.stock != null && this.config != null) {
                if (!isOnline()) return FluidStack.EMPTY;
                MEStorage aeNetwork = Objects.requireNonNull(getMainNode().getGrid()).getStorageService().getInventory();

                Actionable actionable = action.simulate() ? Actionable.SIMULATE : Actionable.MODULATE;
                var key = config.what();
                long extracted = aeNetwork.extract(key, maxDrain, actionable, actionSource);

                if (extracted > 0) {
                    FluidStack resultStack = key instanceof AEFluidKey fluidKey ?
                            AEUtil.toFluidStack(fluidKey, extracted) : FluidStack.EMPTY;
                    if (action.execute()) {
                        this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                        if (this.stock.amount() == 0) {
                            this.stock = null;
                        }
                        if (this.onContentsChanged != null) {
                            this.onContentsChanged.run();
                        }
                    }
                    return resultStack;
                }
            }
            return FluidStack.EMPTY;
        }
    }
}