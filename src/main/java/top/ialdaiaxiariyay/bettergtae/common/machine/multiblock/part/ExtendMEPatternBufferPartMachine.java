package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.part;

import brachy.modularui.widget.Widget;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
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
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.patternprovider.PatternContainer;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.ItemDrawable;
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
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jetbrains.annotations.VisibleForTesting;
import top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.trait.ExtendInternalSlotRecipeHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendMEPatternBufferPartMachine extends MEBusPartMachine
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
    // Maybe an Expansion Option in the future? a bit redundant for rn. Maybe Packdevs want to add their own
    // version.
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

    @SaveField
    private final Set<BlockPos> proxies = new ObjectOpenHashSet<>();
    private final Set<ExtendMEPatternBufferProxyPartMachine> proxyMachines = new ReferenceOpenHashSet<>();

    @Getter
    protected final ExtendInternalSlotRecipeHandler internalRecipeHandler;

    @Nullable
    protected TickableSubscription updateSubs;

    @SaveField
    @SyncToClient
    private int[] circuitConfigurations = new int[MAX_PATTERN_COUNT];

    public ExtendMEPatternBufferPartMachine(BlockEntityCreationInfo info) {
        super(info, IO.IN);
        patternInventory.setOnContentsChanged(() -> getSyncDataHolder().markClientSyncFieldDirty("patternInventory"));
        this.patternInventory.setFilter(stack -> stack.getItem() instanceof ProcessingPatternItem);

        for (int i = 0; i < this.internalInventory.length; i++) {
            NotifiableItemStackHandler circuitInv = attachTrait(
                    new NotifiableItemStackHandler(1, IO.IN, IO.NONE) {
                        @Override
                        public void onContentsChanged() {
                            super.onContentsChanged();
                        }
                    }
            );
            circuitInv.setFilter(IntCircuitBehaviour::isIntegratedCircuit);
            InternalSlot slot = new InternalSlot(circuitInv);
            circuitInv.storage.setOnContentsChanged(slot::onContentsChanged);
            this.internalInventory[i] = slot;
        }

        getMainNode().addService(ICraftingProvider.class, this);
        this.shareInventory = attachTrait(new NotifiableItemStackHandler(16, IO.IN, IO.NONE));
        this.shareTank = attachTrait(new NotifiableFluidTank(16, 8 * FluidType.BUCKET_VOLUME, IO.IN, IO.NONE));
        this.internalRecipeHandler = new ExtendInternalSlotRecipeHandler(this, internalInventory);
        Arrays.fill(circuitConfigurations, -1);
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
            // 将电路配置同步到 InternalSlot 的 circuitInventory
            for (int i = 0; i < circuitConfigurations.length; i++) {
                setCircuitConfiguration(i, circuitConfigurations[i]);
            }
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

    public void addProxy(ExtendMEPatternBufferProxyPartMachine proxy) {
        proxies.add(proxy.getBlockPos());
        proxyMachines.add(proxy);
    }

    public void removeProxy(ExtendMEPatternBufferProxyPartMachine proxy) {
        proxies.remove(proxy.getBlockPos());
        proxyMachines.remove(proxy);
    }

    @UnmodifiableView
    public Set<ExtendMEPatternBufferProxyPartMachine> getProxies() {
        if (proxyMachines.size() != proxies.size()) {
            proxyMachines.clear();
            for (var pos : proxies) {
                if (MetaMachine.getMachine(getLevel(), pos) instanceof ExtendMEPatternBufferProxyPartMachine proxy) {
                    proxyMachines.add(proxy);
                }
            }
        }
        return Collections.unmodifiableSet(proxyMachines);
    }

    private void refundAll() {
        for (InternalSlot internalSlot : internalInventory) {
            internalSlot.refund();
        }
    }

    @VisibleForTesting
    public void onPatternChange(int index) {
        if (isRemote()) return;

        // remove old if applicable
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

    public int getCircuitConfiguration(int slot) {
        if (slot < 0 || slot >= circuitConfigurations.length) return -1;
        return circuitConfigurations[slot];
    }

    public void setCircuitConfiguration(int slot, int value) {
        if (slot < 0 || slot >= circuitConfigurations.length) return;
        if (value < -1 || value > 32) return;
        circuitConfigurations[slot] = value;
        InternalSlot internalSlot = internalInventory[slot];
        if (value >= 0) {
            internalSlot.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(value));
        } else {
            internalSlot.getCircuitInventory().setStackInSlot(0, ItemStack.EMPTY);
        }
        syncDataHolder.markClientSyncFieldDirty("circuitConfigurations");
        markAsChanged();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        // 保留主面板同步管理器引用，供后续闭包使用
        final PanelSyncManager mainSyncManager = syncManager;

        // ---- 重命名面板 ----
        IPanelHandler renamingPanelHandler = syncManager.syncedPanel("renaming", true,
                ((syncManager1, syncHandler) -> PopupPanel.createPopupPanel("renaming_panel", 110, 40)
                        .child(Flow.col()
                                .coverChildren()
                                .child(Text.lang("gtceu.gui.pattern_buffer.set_custom_name").asWidget())
                                .child(new TextFieldWidget()
                                        .size(90, 20)
                                        .value(SyncHandlers.string(() -> this.customName, this::setCustomName)))
                                .margin(5))));

        // ---- 共享物品面板 ----
        IPanelHandler sharedItemsPanelHandler = syncManager.syncedPanel("shared_items", true,
                (syncManager1, panelHandler) -> {
                    SlotGroup sharedItemSlotGroup = new SlotGroup("shared_item_slots", 4, false);
                    // 窗口宽度略大于 4 个 18px 格子（72）+ 边距，80 足够
                    return PopupPanel.createPopupPanel("shared_items_panel", 80, 86)
                            .child(Text.lang("gui.gtceu.share_inventory.title").asWidget().margin(4))
                            .child(new Grid()
                                    .name("shared_item_grid")
                                    .top(26)
                                    .height(18 * 4)                // 4 行
                                    .minElementMargin(0, 0)
                                    .minColWidth(18).minRowHeight(18)
                                    .leftRel(0.5f)
                                    .gridOfSizeWidth(16, 4, (x, y, index) -> new ItemSlot()   // 总共 16 格，4 列
                                            .slot(SyncHandlers.itemSlot(shareInventory, index)
                                                    .slotGroup(sharedItemSlotGroup)
                                                    .accessibility(true, true))));
                });
        // ---- 共享流体面板 ----
        IPanelHandler sharedFluidsPanelHandler = syncManager.syncedPanel("shared_fluids", true,
                (syncManager1, panelHandler) -> PopupPanel.createPopupPanel("shared_fluids_panel", 85, 86)
                        .child(Text.lang("gui.gtceu.share_tank.title").asWidget().margin(4))
                        .child(GTMuiMachineUtil.createSlotGroupFromInventory(syncManager1, shareTank,
                                        "shared_fluid_slots", 16, 'F',
                                        GTMuiMachineUtil.createSquareMatrix(16, 'F'))   // 4x4 矩阵，共 16 格
                                .top(26)
                                .leftRel(0.5f)));

        // ---- 电路配置数值选择器面板（右键弹出） ----
        // 用 IntSyncValue 存储当前正在编辑的槽位号
        final AtomicInteger selectedCircuitSlot = new AtomicInteger(-1);

        final IPanelHandler circuitPickerHandler = mainSyncManager.syncedPanel("circuit_picker", true,
                (syncManager1, panelHandler) -> {
                    int slotIndex = selectedCircuitSlot.get();
                    if (slotIndex < 0 || slotIndex >= MAX_PATTERN_COUNT) {
                        return PopupPanel.createPopupPanel("circuit_picker_empty", 0, 0);
                    }

                    PopupPanel popup = PopupPanel.createPopupPanel("circuit_picker_panel", 170, 130);
                    popup.closeOnOutOfBoundsClick(true);

                    popup.child(Text.lang("bettergtae.gui.circuit.picker.title", slotIndex + 1)
                            .asWidget().posRel(0.5f, 0.05f).size(160, 16));
                    popup.child(Text.dynamic(() -> Component.translatable("bettergtae.gui.circuit.current_value",
                                    getCircuitConfiguration(slotIndex)))
                            .asWidget().posRel(0.5f, 0.12f).size(160, 12));

                    // 数字网格（0~32）
                    Grid grid = new Grid()
                            .minElementMargin(0, 0)
                            .minColWidth(18).minRowHeight(18)
                            .coverChildren()
                            .leftRel(0.5f)
                            .topRel(0.25f)
                            .gridOfWidthHeight(4, 9, (row, col, index) -> {
                                int value = row * 9 + col;
                                if (value > 32) return new Widget<>().size(18, 18);
                                return (ButtonWidget<?>) new ButtonWidget<>()
                                        .size(18, 18)
                                        .background(new ItemDrawable(IntCircuitBehaviour.stack(value)).asIcon())
                                        .onMousePressed((c, b) -> {
                                            setCircuitConfiguration(slotIndex, value);
                                            mainSyncManager.callSyncedAction("setCircuitConfig", buf -> {
                                                buf.writeVarInt(slotIndex);
                                                buf.writeVarInt(value);
                                            });
                                            panelHandler.closePanel();
                                            return true;
                                        })
                                        .tooltip(new RichTooltip().add(Component.literal(String.valueOf(value))));
                            });

                    popup.child(grid);

                    // 清除按钮
                    ButtonWidget<?> clearBtn = new ButtonWidget<>()
                            .size(40, 16)
                            .posRel(0.5f, 0.85f)
                            .background(GTGuiTextures.BUTTON)
                            .overlay(Text.lang("bettergtae.gui.circuit.clear").asIcon())
                            .onMousePressed((c, b) -> {
                                setCircuitConfiguration(slotIndex, -1);
                                mainSyncManager.callSyncedAction("setCircuitConfig", buf -> {
                                    buf.writeVarInt(slotIndex);
                                    buf.writeVarInt(-1);
                                });
                                panelHandler.closePanel();
                                return true;
                            });
                    popup.child(clearBtn);

                    return popup;
                });

        // ---- 电路配置总览面板（左侧按钮打开） ----
        IPanelHandler circuitPanelHandler = syncManager.syncedPanel("circuit_config", true,
                (syncManager1, panelHandler) -> {
                    PopupPanel popup = PopupPanel.createPopupPanel("circuit_config_panel", 174, 185);
                    popup.closeOnOutOfBoundsClick(true);
                    popup.child(Text.lang("bettergtae.gui.circuit.configurator.title")
                            .asWidget().posRel(0.5f, 0.04f).size(160, 16));

                    Grid grid = new Grid()
                            .height(18 * ((int) Math.ceil((double) MAX_PATTERN_COUNT / 9)))
                            .minElementMargin(0, 0)
                            .minColWidth(18).minRowHeight(18)
                            .leftRel(0.5f)
                            .gridOfSizeWidth(MAX_PATTERN_COUNT, 9, (x, y, index) -> {
                                if (index >= MAX_PATTERN_COUNT) return new Widget<>().size(18, 18);
                                return createCircuitButton(index, mainSyncManager, selectedCircuitSlot, circuitPickerHandler);
                            });

                    popup.child(grid.topRel(0.2f));
                    return popup;
                });

        // ---- 退款 / 状态等同步值 ----
        BooleanSyncValue canRefundValue = SyncHandlers.bool(this::canRefund, b -> {});
        syncManager.syncValue("can_refund", canRefundValue);

        // ---- 注册动作 ----
        syncManager.registerServerSyncedAction("refundButtonPressed", packet -> refundAll());
        syncManager.registerServerSyncedAction("setCircuitConfig", packet -> {
            int slot = packet.readVarInt();
            int value = packet.readVarInt();
            setCircuitConfiguration(slot, value); // 内部会同步更新 circuitInventory
        });

        // ---- 左侧配置按钮区域 ----
        return MachineUIPanelBuilder.panelBuilder(this).leftConfigurators(f -> f
                .child(new ButtonWidget<>()
                        .size(18)
                        .onMousePressed((context, b) -> {
                            if (b == InputConstants.MOUSE_BUTTON_LEFT) {
                                sharedItemsPanelHandler.openPanel();
                                return true;
                            }
                            return false;
                        })
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .tooltip(new RichTooltip()
                                .addLine(Text.lang("gui.gtceu.share_inventory.desc.0"))
                                .addLine(Text.lang("gui.gtceu.share_inventory.desc.1"))))
                .child(new ButtonWidget<>()
                        .size(18)
                        .onMousePressed((context, b) -> {
                            if (b == InputConstants.MOUSE_BUTTON_LEFT) {
                                sharedFluidsPanelHandler.openPanel();
                                return true;
                            }
                            return false;
                        })
                        .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                        .tooltip(new RichTooltip()
                                .addLine(Text.lang("gui.gtceu.share_tank.desc.0"))
                                .addLine(Text.lang("gui.gtceu.share_inventory.desc.1"))))
                .child(new ButtonWidget<>()
                        .size(18)
                        .onMousePressed((context, b) -> {
                            if (canRefundValue.getBoolValue() && b == InputConstants.MOUSE_BUTTON_LEFT) {
                                syncManager.callSyncedAction("refundButtonPressed");
                                return true;
                            }
                            return false;
                        })
                        .overlay(new DynamicDrawable(() -> canRefundValue.getBoolValue()
                                ? GTGuiTextures.REFUND_OVERLAY.asIcon().size(16)
                                : new DrawableStack(GTGuiTextures.REFUND_OVERLAY, new ItemDrawable(Items.BARRIER))
                                .asIcon().size(16)))
                        .tooltip(new RichTooltip().addLine(Text.lang("gui.gtceu.refund_all.desc"))))
                .child(new ButtonWidget<>()
                        .size(18)
                        .onMousePressed((context, b) -> {
                            if (b == InputConstants.MOUSE_BUTTON_LEFT) {
                                renamingPanelHandler.openPanel();
                                return true;
                            }
                            return false;
                        })
                        .overlay(Text.str("✎").asIcon().size(16))
                        .tooltip(new RichTooltip().addLine(Text.lang("gui.gtceu.rename.desc"))))
                .child(new ButtonWidget<>()
                        .size(18)
                        .onMousePressed((context, b) -> {
                            if (b == InputConstants.MOUSE_BUTTON_LEFT) {
                                circuitPanelHandler.openPanel();
                                return true;
                            }
                            return false;
                        })
                        .overlay(GTGuiTextures.INT_CIRCUIT_OVERLAY.asIcon().size(16))
                        .tooltip(new RichTooltip().add(Component.translatable("bettergtae.gui.circuit.configurator.tooltip")))));
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
                                .filter(stack -> stack.getItem() instanceof EncodedPatternItem)
                                .changeListener((i, o, c, init) -> onPatternChange(index)))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.PATTERN_OVERLAY)));

        mainWidget.child(flow.center());
    }

    private Widget<?> createCircuitButton(int slotIndex, PanelSyncManager mainSyncManager,
                                          AtomicInteger selectedCircuitSlot, IPanelHandler circuitPickerHandler) {
        DynamicDrawable display = new DynamicDrawable(() -> {
            int val = getCircuitConfiguration(slotIndex);
            String text = val == -1 ? "-" : String.valueOf(val);
            return Text.str(text).asIcon().size(16);
        });

        // 左键
        // 中键
        // 右键：打开数值选择器
        return new ButtonWidget<>()
                .size(18, 18)
                .background(GTGuiTextures.SLOT)
                .overlay(display)
                .onMousePressed((context, btn) -> {
                    int cur = getCircuitConfiguration(slotIndex);
                    int newVal;
                    if (btn == 0) { // 左键
                        if (cur == -1) newVal = 0;
                        else {
                            newVal = (cur + 1) % 33;
                            if (newVal == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit) newVal = -1;
                        }
                        sendCircuitSync(slotIndex, newVal, mainSyncManager);
                    } else if (btn == 1) { // 中键
                        newVal = ConfigHolder.INSTANCE.machines.ghostCircuit ? -1 : 0;
                        sendCircuitSync(slotIndex, newVal, mainSyncManager);
                    } else if (btn == 2) { // 右键：打开数值选择器
                        selectedCircuitSlot.set(slotIndex);
                        circuitPickerHandler.openPanel();
                    }
                    return true;
                })
                .onMouseScrolled((context, delta) -> {
                    int cur = getCircuitConfiguration(slotIndex);
                    int newVal;
                    if (delta > 0) {
                        if (cur == -1) newVal = 0;
                        else {
                            newVal = (cur + 1) % 33;
                            if (newVal == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit) newVal = -1;
                        }
                    } else {
                        if (cur == -1) {
                            newVal = ConfigHolder.INSTANCE.machines.ghostCircuit ? -1 : 32;
                        } else {
                            newVal = cur - 1;
                            if (newVal < 0) newVal = ConfigHolder.INSTANCE.machines.ghostCircuit ? -1 : 32;
                        }
                    }
                    sendCircuitSync(slotIndex, newVal, mainSyncManager);
                    return true;
                })
                .tooltip(new RichTooltip()
                        .add(Component.translatable("bettergtae.gui.circuit.slot_tooltip", slotIndex + 1)));
    }

    private void sendCircuitSync(int slot, int value, PanelSyncManager syncManager) {
        syncManager.callSyncedAction("setCircuitConfig", buf -> {
            buf.writeVarInt(slot);
            buf.writeVarInt(value);
        });
    }

    public boolean canRefund() {
        return Arrays.stream(internalInventory).anyMatch(slot -> !slot.isEmpty());
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return detailsSlotMap.keySet().stream().toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
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
        // Has controller
        if (isFormed()) {
            MultiblockControllerMachine controller = getControllers().first();
            MultiblockMachineDefinition controllerDefinition = controller.getDefinition();
            // has customName
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                ItemStack circuitStack = isHasCircuitSlot() ? circuitInventory.storage.getStackInSlot(0) :
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

    public record BufferData(Object2LongMap<ItemStack> items, Object2LongMap<FluidStack> fluids) {}

    public BufferData mergeInternalSlots() {
        var items = new Object2LongOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
        var fluids = new Object2LongOpenHashMap<FluidStack>();
        for (InternalSlot slot : internalInventory) {
            slot.itemInventory.object2LongEntrySet().fastForEach(e -> items.addTo(e.getKey(), e.getLongValue()));
            slot.fluidInventory.object2LongEntrySet().fastForEach(e -> fluids.addTo(e.getKey(), e.getLongValue()));
        }
        return new BufferData(items, fluids);
    }

    public class InternalSlot implements INBTSerializable<CompoundTag> {

        @Getter
        @Setter
        private Runnable onContentsChanged = () -> {};

        private final Object2LongOpenCustomHashMap<ItemStack> itemInventory = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        private final Object2LongOpenHashMap<FluidStack> fluidInventory = new Object2LongOpenHashMap<>();
        private @Nullable List<ItemStack> itemStacks = null;
        private @Nullable List<FluidStack> fluidStacks = null;
        @Getter
        private final NotifiableItemStackHandler circuitInventory;

        public InternalSlot(NotifiableItemStackHandler circuitInventory) {
            this.circuitInventory = circuitInventory;
            this.circuitInventory.setFilter(IntCircuitBehaviour::isIntegratedCircuit);
            this.circuitInventory.storage.setOnContentsChanged(() -> {
                InternalSlot.this.onContentsChanged();
            });
        }

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
            if (amount <= 0L) return;
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
            if (network != null) {
                MEStorage networkInv = network.getStorageService().getInventory();
                var energy = network.getEnergyService();

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
        }

        public void pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            patternDetails.pushInputsToExternalInventory(inputHolder, this::add);
            onContentsChanged();
        }

        public List<Ingredient> handleItemInternal(List<Ingredient> left, boolean simulate) {
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
                    if (ingredient instanceof SizedIngredient si) {
                        si.setAmount(amount);
                    } else {
                        items[0].setCount(amount);
                    }
                }
            }
            if (changed) onContentsChanged();
            return left;
        }

        public List<FluidIngredient> handleFluidInternal(List<FluidIngredient> left, boolean simulate) {
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

                if (amount > 0) {
                    ingredient.setAmount(amount);
                }
            }

            if (changed) onContentsChanged();
            return left;
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

            if (circuitInventory != null) {
                tag.put("circuitInventory", circuitInventory.storage.serializeNBT());
            }
            
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag items = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (Tag t : items) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = ItemStack.of(ct);
                var count = ct.getLong("real");
                if (!stack.isEmpty() && count > 0) {
                    itemInventory.put(stack, count);
                }
            }

            ListTag fluids = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            for (Tag t : fluids) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = FluidStack.loadFluidStackFromNBT(ct);
                var amount = ct.getLong("real");
                if (!stack.isEmpty() && amount > 0) {
                    fluidInventory.put(stack, amount);
                }
            }

            if (tag.contains("circuitInventory") && circuitInventory != null) {
                circuitInventory.storage.deserializeNBT(tag.getCompound("circuitInventory"));
            }
        }
    }
}
