package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.crafting.pattern.CraftingPatternItem;
import appeng.helpers.patternprovider.PatternContainer;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.trait.CraftingPatternInternalSlotRecipeHandler;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CraftingPatternPartMachine extends MEBusPartMachine
                                        implements ICraftingProvider, PatternContainer, IDataStickInteractable {

    protected static final int MAX_PATTERN_COUNT = 81;

    private final InternalInventory internalPatternInventory = new InternalInventory() {

        @Override
        public int size() {
            return MAX_PATTERN_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return patternInventory.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            patternInventory.setStackInSlot(slotIndex, stack);
            patternInventory.onContentsChanged(slotIndex);
            onPatternChange(slotIndex);
        }
    };

    @Getter
    @SaveField
    @SyncToClient
    private final CustomItemStackHandler patternInventory = new CustomItemStackHandler(MAX_PATTERN_COUNT);

    @Getter
    @SaveField
    protected final NotifiableItemStackHandler shareInventory;

    @Getter
    @SaveField
    protected final NotifiableFluidTank shareTank;

    @Getter
    @SaveField
    protected final InternalSlot[] internalInventory = new InternalSlot[MAX_PATTERN_COUNT];

    private final BiMap<IPatternDetails, InternalSlot> detailsSlotMap = HashBiMap.create(MAX_PATTERN_COUNT);

    @SyncToClient
    @SaveField
    private String customName = "";

    private boolean needPatternSync;

    @Getter
    protected final CraftingPatternInternalSlotRecipeHandler internalRecipeHandler;

    @Nullable
    protected TickableSubscription updateSubs;

    // 新增字段
    public final Object2LongLinkedOpenHashMap<GenericStack> outputItems = new Object2LongLinkedOpenHashMap<>();
    @Getter
    private final BiMap<IPatternDetails, Integer> patternSlotMap = HashBiMap.create();

    public CraftingPatternPartMachine(BlockEntityCreationInfo info) {
        super(info, IO.IN, new NotifiableItemStackHandler(9, IO.IN, IO.NONE));
        patternInventory.setFilter(stack -> stack.getItem() instanceof CraftingPatternItem);
        patternInventory.setOnContentsChanged(() -> syncDataHolder.markClientSyncFieldDirty("patternInventory"));

        for (int i = 0; i < this.internalInventory.length; i++) {
            this.internalInventory[i] = new InternalSlot();
        }
        getMainNode().addService(ICraftingProvider.class, this);

        this.shareInventory = attachTrait(new NotifiableItemStackHandler(9, IO.IN, IO.NONE));
        this.shareTank = attachTrait(new NotifiableFluidTank(9, 8 * FluidType.BUCKET_VOLUME, IO.IN, IO.NONE));
        this.internalRecipeHandler = new CraftingPatternInternalSlotRecipeHandler(this, internalInventory);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            for (int i = 0; i < patternInventory.getSlots(); i++) {
                var pattern = patternInventory.getStackInSlot(i);
                var patternDetails = PatternDetailsHelper.decodePattern(pattern, getLevel());
                if (patternDetails != null) {
                    this.detailsSlotMap.put(patternDetails, this.internalInventory[i]);
                }
            }
            needPatternSync = true;
        }
    }

    @Override
    public List<RecipeHandlerList> getRecipeHandlers() {
        return internalRecipeHandler.getSlotHandlers();
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    public void setCustomName(String newName) {
        customName = newName;
        syncDataHolder.markClientSyncFieldDirty("customName");
        markAsChanged();
    }

    @Override
    public void setWorkingEnabled(boolean ignored) {}

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean ignored) {}

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.updateSubscription();
    }

    protected void updateSubscription() {
        if (getMainNode().isOnline()) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void update() {
        if (needPatternSync) {
            ICraftingProvider.requestUpdate(getMainNode());
            this.needPatternSync = false;
        }
    }

    private void refundAll() {
        for (InternalSlot internalSlot : internalInventory) {
            internalSlot.refund();
        }
    }

    @VisibleForTesting
    public void onPatternChange(int index) {
        if (isRemote()) return;
        var internalInv = internalInventory[index];
        var newPattern = patternInventory.getStackInSlot(index);
        var newPatternDetails = PatternDetailsHelper.decodePattern(newPattern, getLevel());
        var oldPatternDetails = detailsSlotMap.inverse().get(internalInv);
        detailsSlotMap.forcePut(newPatternDetails, internalInv);
        if (oldPatternDetails != null && !oldPatternDetails.equals(newPatternDetails)) {
            internalInv.refund();
        }
        needPatternSync = true;
    }

    // =========================== UI (ModularUI) ===========================
    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IPanelHandler renamingPanelHandler = syncManager.syncedPanel("renaming", true,
                (sm, handler) -> PopupPanel.createPopupPanel("renaming_panel", 110, 40)
                        .child(Flow.col()
                                .coverChildren()
                                .child(Text.lang("gtceu.gui.pattern_buffer.set_custom_name").asWidget())
                                .child(new TextFieldWidget()
                                        .size(90, 20)
                                        .value(SyncHandlers.string(() -> this.customName, this::setCustomName)))
                                .margin(5)));

        IPanelHandler sharedItemsPanelHandler = syncManager.syncedPanel("shared_items", true,
                (sm, handler) -> {
                    SlotGroup sharedItemSlotGroup = new SlotGroup("shared_item_slots", 3, false);
                    return PopupPanel.createPopupPanel("shared_items_panel", 80, 86)
                            .child(Text.lang("gui.gtceu.share_inventory.title").asWidget().margin(4))
                            .child(new Grid()
                                    .name("shared_item_grid")
                                    .top(26)
                                    .height(18 * 3)
                                    .minElementMargin(0, 0)
                                    .minColWidth(18).minRowHeight(18)
                                    .leftRel(0.5f)
                                    .gridOfSizeWidth(9, 3, (x, y, index) -> new ItemSlot()
                                            .slot(SyncHandlers.itemSlot(shareInventory, index)
                                                    .slotGroup(sharedItemSlotGroup)
                                                    .accessibility(true, true))));
                });

        IPanelHandler sharedFluidsPanelHandler = syncManager.syncedPanel("shared_fluids", true,
                (sm, handler) -> PopupPanel.createPopupPanel("shared_fluids_panel", 85, 86)
                        .child(Text.lang("gui.gtceu.share_tank.title").asWidget().margin(4))
                        .child(GTMuiMachineUtil.createSlotGroupFromInventory(sm, shareTank,
                                "shared_fluid_slots", 9, 'F',
                                GTMuiMachineUtil.createSquareMatrix(9, 'F'))
                                .top(26)
                                .leftRel(0.5f)));

        BooleanSyncValue canRefundValue = SyncHandlers.bool(this::canRefund, b -> {});
        syncManager.syncValue("can_refund", canRefundValue);
        syncManager.registerServerSyncedAction("refundButtonPressed", packet -> refundAll());

        return MachineUIPanelBuilder.panelBuilder(this)
                .leftConfigurators(f -> f.child(new ButtonWidget<>() // Shared items
                        .size(18)
                        .onMousePressed((context, b) -> {
                            if (b == 0) {
                                sharedItemsPanelHandler.openPanel();
                                return true;
                            }
                            return false;
                        })
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .tooltip(new RichTooltip()
                                .addLine(Text.lang("gui.gtceu.share_inventory.desc.0"))
                                .addLine(Text.lang("gui.gtceu.share_inventory.desc.1"))))
                        .child(new ButtonWidget<>() // Shared fluids
                                .size(18)
                                .onMousePressed((context, b) -> {
                                    if (b == 0) {
                                        sharedFluidsPanelHandler.openPanel();
                                        return true;
                                    }
                                    return false;
                                })
                                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                                .tooltip(new RichTooltip()
                                        .addLine(Text.lang("gui.gtceu.share_tank.desc.0"))
                                        .addLine(Text.lang("gui.gtceu.share_inventory.desc.1"))))
                        .child(new ButtonWidget<>() // Refund
                                .size(18)
                                .onMousePressed((context, b) -> {
                                    if (canRefundValue.getBoolValue() && b == 0) {
                                        syncManager.callSyncedAction("refundButtonPressed");
                                        return true;
                                    }
                                    return false;
                                })
                                .overlay(new DynamicDrawable(() -> GTGuiTextures.REFUND_OVERLAY.asIcon().size(16)))
                                .tooltip(new RichTooltip().addLine(Text.lang("gui.gtceu.refund_all.desc"))))
                        .child(new ButtonWidget<>() // Rename
                                .size(18)
                                .onMousePressed((context, b) -> {
                                    if (b == 0) {
                                        renamingPanelHandler.openPanel();
                                        return true;
                                    }
                                    return false;
                                })
                                .overlay(Text.str("✎").asIcon().size(16))
                                .tooltip(new RichTooltip().addLine(Text.lang("gui.gtceu.rename.desc")))));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        SlotGroup patternSlotGroup = new SlotGroup("pattern_slots", 9, 0, true);
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        var flow = Flow.col().coverChildren();
        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        flow.child(new Grid()
                .height(18 * (MAX_PATTERN_COUNT / 9))
                .minElementMargin(0, 0)
                .minColWidth(18).minRowHeight(18)
                .leftRel(0.5f)
                .gridOfSizeWidth(MAX_PATTERN_COUNT, 9, (x, y, index) -> new ItemSlot()
                        .slot(SyncHandlers.itemSlot(patternInventory, index)
                                .slotGroup(patternSlotGroup)
                                .accessibility(true, true)
                                .filter(stack -> stack.getItem() instanceof CraftingPatternItem)
                                .changeListener((i, o, c, init) -> onPatternChange(index)))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.PATTERN_OVERLAY)));

        mainWidget.child(flow.center());
    }

    public boolean canRefund() {
        return Arrays.stream(internalInventory).anyMatch(slot -> !slot.isEmpty());
    }

    // =========================== ICraftingProvider & PatternContainer ===========================
    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return detailsSlotMap.keySet().stream().toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        // 记录输出
        GenericStack output = patternDetails.getOutputs()[0];
        long multiplier = 1;
        // 简单计算multiplier：取第一个输入的数量 / 所需数量
        if (inputHolder.length > 0 && patternDetails.getInputs().length > 0) {
            var firstInput = patternDetails.getInputs()[0];
            var possible = firstInput.getPossibleInputs();
            if (possible.length > 0) {
                var required = possible[0];
                for (var input : inputHolder) {
                    var count = input.get(required.what());
                    if (count > 0) {
                        multiplier = count / required.amount();
                        break;
                    }
                }
            }
        }
        outputItems.addTo(output, Math.max(1, multiplier));

        if (!isFormed() || !getMainNode().isActive() || !detailsSlotMap.containsKey(patternDetails) ||
                !checkInput(inputHolder)) {
            return false;
        }
        var slot = detailsSlotMap.get(patternDetails);
        if (slot != null) {
            slot.pushPattern(patternDetails, inputHolder);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    private boolean checkInput(KeyCounter[] inputHolder) {
        for (KeyCounter input : inputHolder) {
            var illegal = input.keySet().stream()
                    .map(AEKey::getType)
                    .map(AEKeyType::getId)
                    .anyMatch(id -> !id.equals(AEKeyType.items().getId()) && !id.equals(AEKeyType.fluids().getId()));
            if (illegal) return false;
        }
        return true;
    }

    @Override
    public @Nullable IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return internalPatternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        if (isFormed()) {
            MultiblockControllerMachine controller = getControllers().first();
            var controllerDefinition = controller.getDefinition();
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                ItemStack circuitStack = circuitSlot.isEnabled() ? circuitSlot.storage.getStackInSlot(0) :
                        ItemStack.EMPTY;
                int circuitConfiguration = circuitStack.isEmpty() ? -1 :
                        IntCircuitBehaviour.getCircuitConfiguration(circuitStack);
                Component groupName = circuitConfiguration != -1 ?
                        Component.translatable(controllerDefinition.getDescriptionId())
                                .append(" - " + circuitConfiguration) :
                        Component.translatable(controllerDefinition.getDescriptionId());
                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()), groupName, Collections.emptyList());
            }
        } else {
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(GTAEMachines.ME_PATTERN_BUFFER.getItem()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                return new PatternContainerGroup(
                        AEItemKey.of(GTAEMachines.ME_PATTERN_BUFFER.getItem()),
                        GTAEMachines.ME_PATTERN_BUFFER.get().getDefinition().getItem().getDescription(),
                        Collections.emptyList());
            }
        }
    }

    @Override
    public void onMachineDestroyed() {
        patternInventory.dropInventoryInWorld(getLevel(), getBlockPos());
        shareInventory.dropInventoryInWorld();
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        dataStick.getOrCreateTag().putIntArray("pos",
                new int[] { getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ() });
        return InteractionResult.SUCCESS;
    }

    // =========================== InternalSlot (NBT serializable) ===========================
    public class InternalSlot implements INBTSerializable<CompoundTag> {

        @Getter
        @Setter
        private Runnable onContentsChanged = () -> {};
        private final Object2LongOpenCustomHashMap<ItemStack> itemInventory = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        private final Object2LongOpenHashMap<FluidStack> fluidInventory = new Object2LongOpenHashMap<>();
        private List<ItemStack> itemStacks = null;
        private List<FluidStack> fluidStacks = null;

        public boolean isItemEmpty() {
            return itemInventory.isEmpty();
        }

        public boolean isFluidEmpty() {
            return fluidInventory.isEmpty();
        }

        public boolean isEmpty() {
            return isItemEmpty() && isFluidEmpty();
        }

        public void onContentsChanged() {
            itemStacks = null;
            fluidStacks = null;
            onContentsChanged.run();
        }

        private void add(AEKey what, long amount) {
            if (amount <= 0) return;
            if (what instanceof AEItemKey itemKey) {
                var stack = itemKey.toStack();
                itemInventory.addTo(stack, amount);
            } else if (what instanceof AEFluidKey fluidKey) {
                var stack = fluidKey.toStack(1);
                fluidInventory.addTo(stack, amount);
            }
        }

        public List<ItemStack> getItems() {
            if (itemStacks == null) {
                itemStacks = new ArrayList<>();
                itemInventory.object2LongEntrySet().stream()
                        .map(e -> GTMath.splitStacks(e.getKey(), e.getLongValue()))
                        .forEach(itemStacks::addAll);
            }
            return itemStacks;
        }

        public List<FluidStack> getFluids() {
            if (fluidStacks == null) {
                fluidStacks = new ArrayList<>();
                fluidInventory.object2LongEntrySet().stream()
                        .map(e -> GTMath.splitFluidStacks(e.getKey(), e.getLongValue()))
                        .forEach(fluidStacks::addAll);
            }
            return fluidStacks;
        }

        public void refund() {
            var network = getMainNode().getGrid();
            if (network == null) return;
            MEStorage networkInv = network.getStorageService().getInventory();
            var energy = network.getEnergyService();
            var actionSource = IActionSource.ofMachine(getMainNode()::getNode);

            for (var it = itemInventory.object2LongEntrySet().iterator(); it.hasNext();) {
                var entry = it.next();
                var stack = entry.getKey();
                var count = entry.getLongValue();
                if (stack.isEmpty() || count == 0) {
                    it.remove();
                    continue;
                }
                var key = AEItemKey.of(stack);
                if (key == null) continue;
                long inserted = StorageHelper.poweredInsert(energy, networkInv, key, count, actionSource);
                if (inserted > 0) {
                    count -= inserted;
                    if (count == 0) it.remove();
                    else entry.setValue(count);
                }
            }
            for (var it = fluidInventory.object2LongEntrySet().iterator(); it.hasNext();) {
                var entry = it.next();
                var stack = entry.getKey();
                var amount = entry.getLongValue();
                if (stack.isEmpty() || amount == 0) {
                    it.remove();
                    continue;
                }
                var key = AEFluidKey.of(stack);
                if (key == null) continue;
                long inserted = StorageHelper.poweredInsert(energy, networkInv, key, amount, actionSource);
                if (inserted > 0) {
                    amount -= inserted;
                    if (amount == 0) it.remove();
                    else entry.setValue(amount);
                }
            }
            onContentsChanged();
        }

        public void pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            patternDetails.pushInputsToExternalInventory(inputHolder, this::add);
            onContentsChanged();
        }

        public @Nullable List<Ingredient> handleItemInternal(List<Ingredient> left, boolean simulate) {
            boolean changed = false;
            for (var it = left.listIterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                var items = ingredient.getItems();
                if (items.length == 0 || items[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                int amount = items[0].getCount();
                for (var it2 = itemInventory.object2LongEntrySet().iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    var stack = entry.getKey();
                    var count = entry.getLongValue();
                    if (stack.isEmpty() || count == 0) {
                        it2.remove();
                        continue;
                    }
                    if (!ingredient.test(stack)) continue;
                    int extracted = Math.min(GTMath.saturatedCast(count), amount);
                    if (!simulate && extracted > 0) {
                        changed = true;
                        count -= extracted;
                        if (count == 0) it2.remove();
                        else entry.setValue(count);
                    }
                    amount -= extracted;
                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }
                if (amount > 0) {
                    if (ingredient instanceof SizedIngredient si) si.setAmount(amount);
                    else items[0].setCount(amount);
                }
            }
            if (changed) onContentsChanged();
            return left.isEmpty() ? null : left;
        }

        public @Nullable List<FluidIngredient> handleFluidInternal(List<FluidIngredient> left, boolean simulate) {
            boolean changed = false;
            for (var it = left.listIterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                var fluids = ingredient.getStacks();
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                int amount = fluids[0].getAmount();
                for (var it2 = fluidInventory.object2LongEntrySet().iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    var stack = entry.getKey();
                    var count = entry.getLongValue();
                    if (stack.isEmpty() || count == 0) {
                        it2.remove();
                        continue;
                    }
                    if (!ingredient.test(stack)) continue;
                    int extracted = Math.min(GTMath.saturatedCast(count), amount);
                    if (!simulate && extracted > 0) {
                        changed = true;
                        count -= extracted;
                        if (count == 0) it2.remove();
                        else entry.setValue(count);
                    }
                    amount -= extracted;
                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }
                if (amount > 0) ingredient.setAmount(amount);
            }
            if (changed) onContentsChanged();
            return left.isEmpty() ? null : left;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            ListTag itemsTag = new ListTag();
            for (var entry : itemInventory.object2LongEntrySet()) {
                var ct = entry.getKey().serializeNBT();
                ct.putLong("real", entry.getLongValue());
                itemsTag.add(ct);
            }
            if (!itemsTag.isEmpty()) tag.put("inventory", itemsTag);
            ListTag fluidsTag = new ListTag();
            for (var entry : fluidInventory.object2LongEntrySet()) {
                var ct = entry.getKey().writeToNBT(new CompoundTag());
                ct.putLong("real", entry.getLongValue());
                fluidsTag.add(ct);
            }
            if (!fluidsTag.isEmpty()) tag.put("fluidInventory", fluidsTag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag items = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (Tag t : items) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = ItemStack.of(ct);
                var count = ct.getLong("real");
                if (!stack.isEmpty() && count > 0) itemInventory.put(stack, count);
            }
            ListTag fluids = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            for (Tag t : fluids) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = FluidStack.loadFluidStackFromNBT(ct);
                var amount = ct.getLong("real");
                if (!stack.isEmpty() && amount > 0) fluidInventory.put(stack, amount);
            }
        }
    }
}
