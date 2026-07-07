package top.ialdaiaxiariyay.bettergtae.api.gui;

import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;

import java.util.function.Consumer;

/**
 * ModularUI 版本的多电路配置器。
 * 为每个 InternalSlot 提供独立的编程电路配置。
 * 支持左键递增、右键置空/置零、中键弹窗选择、滚轮调整。
 */
public class MultiCircuitConfigurator extends ParentWidget<MultiCircuitConfigurator> {

    private static final int NO_CONFIG = -1;

    private final ItemStackHandler[] circuitInventories;
    private final PanelSyncManager syncManager;
    private final int[] circuitValues;
    private int lastClickedSlotIndex = -1;
    private int lastClickedNewValue = -1;

    public MultiCircuitConfigurator(ItemStackHandler[] circuitInventories, PanelSyncManager syncManager) {
        this.circuitInventories = circuitInventories;
        this.syncManager = syncManager;
        this.circuitValues = new int[circuitInventories.length];

        // 初始化电路值
        for (int i = 0; i < circuitInventories.length; i++) {
            if (circuitInventories[i] != null) {
                ItemStack stack = circuitInventories[i].getStackInSlot(0);
                if (!stack.isEmpty() && IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                    circuitValues[i] = IntCircuitBehaviour.getCircuitConfiguration(stack);
                } else {
                    circuitValues[i] = NO_CONFIG;
                }
            }
        }

        // 注册服务端同步动作
        syncManager.registerServerSyncedAction("circuit_set_slot", packet -> {
            int slotIndex = packet.readVarInt();
            int value = packet.readVarInt();
            if (slotIndex >= 0 && slotIndex < circuitInventories.length && circuitInventories[slotIndex] != null) {
                if (value == NO_CONFIG) {
                    circuitInventories[slotIndex].setStackInSlot(0, ItemStack.EMPTY);
                } else {
                    circuitInventories[slotIndex].setStackInSlot(0, IntCircuitBehaviour.stack(value));
                }
                circuitValues[slotIndex] = value;
            }
        });

        buildUI();
    }

    private void buildUI() {
        int count = circuitInventories.length;
        int cols = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / cols);

        Grid grid = new Grid()
                .minElementMargin(2, 2)
                .minColWidth(18).minRowHeight(18)
                .coverChildren()
                .center()
                .gridOfWidthHeight(rows, cols, (x, y, index) -> {
                    if (index >= count) return new Widget<>().size(18, 18);
                    return createSlotWidget(index);
                });
        addChild(grid, -1);
    }

    private Widget<?> createSlotWidget(int index) {
        ItemStackHandler handler = circuitInventories[index];
        if (handler == null) return new Widget<>().size(18, 18);

        // 只读槽位
        ItemSlot slot = new ItemSlot()
                .slot(SyncHandlers.itemSlot(handler, 0)
                        .accessibility(false, false))
                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY);

        // 交互按钮
        ButtonWidget<?> button = new ButtonWidget<>()
                .size(18, 18)
                .background(IDrawable.EMPTY)
                .onMousePressed((context, buttonId) -> {
                    if (buttonId == 0) {
                        handleLeftClick(handler, index);
                    } else if (buttonId == 1) {
                        handleRightClick(handler, index);
                    } else if (buttonId == 2) {
                        openCircuitDialog(handler, index);
                    }
                    return true;
                })
                .onMouseScrolled((context, delta) -> {
                    handleMouseWheel(handler, index, delta > 0);
                    return true;
                })
                .tooltip(new RichTooltip()
                        .add(Component.translatable("bettergtae.gui.circuit.middle_click_tooltip")));

        ParentWidget<?> overlay = new ParentWidget<>().size(18, 18);
        overlay.addChild(slot.pos(0, 0), -1);
        overlay.addChild(button.pos(0, 0), -1);
        return overlay;
    }

    // ==================== 交互逻辑（完全保留原功能） ====================

    private void handleLeftClick(ItemStackHandler handler, int slotIndex) {
        ItemStack current = handler.getStackInSlot(0);
        int newValue;

        if (current.isEmpty()) {
            newValue = 0;
            handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
        } else if (IntCircuitBehaviour.isIntegratedCircuit(current)) {
            int cur = IntCircuitBehaviour.getCircuitConfiguration(current);
            newValue = (cur + 1) % (IntCircuitBehaviour.CIRCUIT_MAX + 1);
            if (newValue == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit) {
                handler.setStackInSlot(0, ItemStack.EMPTY);
                newValue = NO_CONFIG;
            } else {
                handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
            }
        } else {
            newValue = 0;
            handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
        }

        circuitValues[slotIndex] = newValue;
        lastClickedSlotIndex = slotIndex;
        lastClickedNewValue = newValue;
        sendSyncAction(slotIndex, newValue);
    }

    private void handleRightClick(ItemStackHandler handler, int slotIndex) {
        int newValue;
        if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            handler.setStackInSlot(0, ItemStack.EMPTY);
            newValue = NO_CONFIG;
        } else {
            handler.setStackInSlot(0, IntCircuitBehaviour.stack(0));
            newValue = 0;
        }
        circuitValues[slotIndex] = newValue;
        lastClickedSlotIndex = slotIndex;
        lastClickedNewValue = newValue;
        sendSyncAction(slotIndex, newValue);
    }

    private void handleMouseWheel(ItemStackHandler handler, int slotIndex, boolean increment) {
        ItemStack current = handler.getStackInSlot(0);
        int newValue;

        if (current.isEmpty()) {
            if (increment) {
                if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                    newValue = 0;
                } else {
                    newValue = 1;
                }
                handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
            } else {
                if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                    handler.setStackInSlot(0, ItemStack.EMPTY);
                    newValue = NO_CONFIG;
                } else {
                    newValue = IntCircuitBehaviour.CIRCUIT_MAX;
                    handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
                }
            }
        } else if (IntCircuitBehaviour.isIntegratedCircuit(current)) {
            int cur = IntCircuitBehaviour.getCircuitConfiguration(current);
            if (increment) {
                newValue = (cur + 1) % (IntCircuitBehaviour.CIRCUIT_MAX + 1);
                if (newValue == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit) {
                    handler.setStackInSlot(0, ItemStack.EMPTY);
                    newValue = NO_CONFIG;
                } else {
                    handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
                }
            } else {
                if (cur == 0) {
                    if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                        handler.setStackInSlot(0, ItemStack.EMPTY);
                        newValue = NO_CONFIG;
                    } else {
                        newValue = IntCircuitBehaviour.CIRCUIT_MAX;
                        handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
                    }
                } else {
                    newValue = cur - 1;
                    handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
                }
            }
        } else {
            newValue = 0;
            handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
        }

        circuitValues[slotIndex] = newValue;
        sendSyncAction(slotIndex, newValue);
    }

    private void setCircuitValue(ItemStackHandler handler, int value, int slotIndex) {
        if (value == NO_CONFIG) {
            handler.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            handler.setStackInSlot(0, IntCircuitBehaviour.stack(value));
        }
        circuitValues[slotIndex] = value;
        sendSyncAction(slotIndex, value);
    }

    private void sendSyncAction(int slotIndex, int value) {
        syncManager.callSyncedAction("circuit_set_slot", buf -> {
            buf.writeVarInt(slotIndex);
            buf.writeVarInt(value);
        });
    }

    // ==================== 中键对话框 ====================

    private void openCircuitDialog(ItemStackHandler handler, int slotIndex) {
        PopupPanel popup = PopupPanel.createPopupPanel("circuit_dialog_" + slotIndex, 174, 160);
        popup.closeOnOutOfBoundsClick(true);

        // 标题
        popup.addChild(Text.lang("gtceu.gui.circuit.title")
                .asWidget()
                .posRel(0.5f, 0.05f)
                .size(160, 16), -1);

        // 显示当前槽
        ItemSlot slot = new ItemSlot()
                .slot(SyncHandlers.itemSlot(handler, 0).accessibility(false, false))
                .background(GTGuiTextures.SLOT, GTGuiTextures.INT_CIRCUIT_OVERLAY);
        popup.addChild(slot.posRel(0.5f, 0.15f).size(18, 18), -1);

        // 数字按钮 0~32
        Flow rows = Flow.column().center().childPadding(2);

        Consumer<Integer> addRow = (start) -> {
            Flow row = Flow.row().center().childPadding(2);
            for (int i = 0; i < 9 && (start + i) <= 32; i++) {
                int value = start + i;
                ButtonWidget<?> btn = new ButtonWidget<>()
                        .size(18, 18)
                        .background(new ItemDrawable(IntCircuitBehaviour.stack(value)).asIcon())
                        .onMousePressed((c, b) -> {
                            setCircuitValue(handler, value, slotIndex);
                            popup.closeIfOpen();
                            return true;
                        })
                        .tooltip(new RichTooltip().add(Component.literal(String.valueOf(value))));
                row.addChild(btn, -1);
            }
            rows.addChild(row, -1);
        };

        addRow.accept(0);
        addRow.accept(9);
        addRow.accept(18);
        // 最后一行 27-32
        Flow lastRow = Flow.row().center().childPadding(2);
        for (int i = 27; i <= 32; i++) {
            int value = i;
            ButtonWidget<?> btn = new ButtonWidget<>()
                    .size(18, 18)
                    .background(new ItemDrawable(IntCircuitBehaviour.stack(value)).asIcon())
                    .onMousePressed((c, b) -> {
                        setCircuitValue(handler, value, slotIndex);
                        popup.closeIfOpen();
                        return true;
                    })
                    .tooltip(new RichTooltip().add(Component.literal(String.valueOf(value))));
            lastRow.addChild(btn, -1);
        }
        rows.addChild(lastRow, -1);

        popup.addChild(rows.posRel(0.5f, 0.35f), -1);

        // 清除按钮
        ButtonWidget<?> clearBtn = new ButtonWidget<>()
                .size(40, 16)
                .posRel(0.5f, 0.8f)
                .background(GTGuiTextures.BUTTON)
                .overlay(Text.lang("bettergtae.gui.circuit.clear").asIcon())
                .onMousePressed((c, b) -> {
                    setCircuitValue(handler, NO_CONFIG, slotIndex);
                    popup.closeIfOpen();
                    return true;
                });
        popup.addChild(clearBtn, -1);

        this.addChild(popup, -1);
    }

    public int[] getCircuitValues() {
        return circuitValues.clone();
    }
}
