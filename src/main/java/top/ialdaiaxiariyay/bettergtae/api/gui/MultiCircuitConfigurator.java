package top.ialdaiaxiariyay.bettergtae.api.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyCustomMiddleClickAction;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Accessors(chain = true)
public class MultiCircuitConfigurator implements IFancyConfigurator, IFancyCustomMiddleClickAction {

    // 只保留实际使用的常量
    private static final int SET_SLOT_TO_N = 5;
    private static final int NO_CONFIG = -1;

    @Persisted
    @DescSynced
    private int[] circuitValues;

    private final ItemStackHandler[] circuitInventories;
    private final Component title;

    @Setter
    private List<Component> tooltips;

    @Getter
    private HoverTrackingGroup mainGroup;

    public MultiCircuitConfigurator(ItemStackHandler @NotNull [] circuitInventories, Component title) {
        this.circuitInventories = circuitInventories;
        this.title = title;
        this.tooltips = List.of(Component.translatable("gtceu.gui.configurator_slot.tooltip.0"));

        this.circuitValues = new int[circuitInventories.length];
        for (int i = 0; i < circuitInventories.length; i++) {
            if (circuitInventories[i] != null) {
                ItemStack stack = circuitInventories[i].getStackInSlot(0);
                if (!stack.isEmpty() && IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                    circuitValues[i] = IntCircuitBehaviour.getCircuitConfiguration(stack);
                } else {
                    circuitValues[i] = -1;
                }
            }
        }
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IGuiTexture getIcon() {
        for (ItemStackHandler handler : circuitInventories) {
            if (handler != null && !handler.getStackInSlot(0).isEmpty() &&
                    IntCircuitBehaviour.isIntegratedCircuit(handler.getStackInSlot(0))) {
                return new ItemStackTexture(handler.getStackInSlot(0));
            }
        }
        return new GuiTextureGroup(new ItemStackTexture(IntCircuitBehaviour.stack(0)),
                new ItemStackTexture(Items.BARRIER));
    }

    @Override
    public Widget createConfigurator() {
        int slotCount = circuitInventories.length;
        int rowSize = (int) Math.ceil(Math.sqrt(slotCount));

        HoverTrackingGroup group = new HoverTrackingGroup(0, 0, 18 * rowSize + 16, 18 * rowSize + 16);
        WidgetGroup container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * rowSize + 8);

        for (int i = 0; i < slotCount; i++) {
            ItemStackHandler circuitInventory = circuitInventories[i];
            if (circuitInventory != null) {
                int x = 4 + (i % rowSize) * 18;
                int y = 2 + (i / rowSize) * 18;
                SlotWidget circuitSlot = new SlotWidget(circuitInventory, 0, x, y, false, false);
                circuitSlot.setBackground(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY));

                // 使用自定义按钮，支持左键、右键、中键以及滚轮
                CircuitSlotButton slotButton = new CircuitSlotButton(x, y, 18, 18,
                        circuitInventory, i, group);
                slotButton.setHoverTooltips(Component.translatable("bettergtae.gui.circuit.middle_click_tooltip"));

                container.addWidget(circuitSlot);
                container.addWidget(slotButton);
            }
        }
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        this.mainGroup = group;
        return group;
    }

    /**
     * 处理递增动作（左键 / 滚轮向上）
     */
    private void incrementCircuitValue(ItemStackHandler handler, int slotIndex, HoverTrackingGroup group) {
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
        if (group.isRemote()) {
            int finalNewValue = newValue;
            group.getWriteClientAction(SET_SLOT_TO_N, buf -> {
                buf.writeVarInt(slotIndex);
                buf.writeVarInt(finalNewValue);
            });
        }
    }

    /**
     * 处理递减动作（滚轮向下）
     */
    private void decrementCircuitValue(ItemStackHandler handler, int slotIndex, HoverTrackingGroup group) {
        ItemStack current = handler.getStackInSlot(0);
        int newValue;
        if (current.isEmpty()) {
            // 空槽位递减：根据 ghostCircuit 决定是置0还是置最大值
            if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                newValue = IntCircuitBehaviour.CIRCUIT_MAX;
                handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
            } else {
                newValue = 0;
                handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
            }
        } else if (IntCircuitBehaviour.isIntegratedCircuit(current)) {
            int cur = IntCircuitBehaviour.getCircuitConfiguration(current);
            if (cur == 0) {
                if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                    newValue = IntCircuitBehaviour.CIRCUIT_MAX;
                    handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
                } else {
                    handler.setStackInSlot(0, ItemStack.EMPTY);
                    newValue = NO_CONFIG;
                }
            } else {
                newValue = cur - 1;
                handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
            }
        } else {
            // 非法物品，统一置0
            newValue = 0;
            handler.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
        }
        circuitValues[slotIndex] = newValue;
        if (group.isRemote()) {
            group.getWriteClientAction(SET_SLOT_TO_N, buf -> {
                buf.writeVarInt(slotIndex);
                buf.writeVarInt(newValue);
            });
        }
    }

    /**
     * 处理右键点击（清空或置0，保持原逻辑）
     */
    private void handleRightClick(ItemStackHandler handler, int slotIndex, HoverTrackingGroup group) {
        int newValue;
        if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            handler.setStackInSlot(0, ItemStack.EMPTY);
            newValue = NO_CONFIG;
        } else {
            handler.setStackInSlot(0, IntCircuitBehaviour.stack(0));
            newValue = 0;
        }
        circuitValues[slotIndex] = newValue;
        if (group.isRemote()) {
            group.getWriteClientAction(SET_SLOT_TO_N, buf -> {
                buf.writeVarInt(slotIndex);
                buf.writeVarInt(newValue);
            });
        }
    }

    private void openCircuitDialog(int slotIndex, HoverTrackingGroup group) {
        if (group == null || group.getGui() == null) return;
        ItemStackHandler handler = circuitInventories[slotIndex];
        if (handler == null) return;

        var ui = group.getGui();
        int guiWidth = ui.getWidth();
        int guiHeight = ui.getHeight();

        WidgetGroup dialogOverlay = new WidgetGroup(0, 0, guiWidth, guiHeight);
        dialogOverlay.setBackground(GuiTextures.BACKGROUND);

        ButtonWidget closeArea = new ButtonWidget(0, 0, guiWidth, guiHeight, IGuiTexture.EMPTY,
                clickData -> closeDialog(dialogOverlay, group));
        dialogOverlay.addWidget(closeArea);

        int dialogWidth = 174;
        int dialogHeight = 132;
        int posX = (guiWidth - dialogWidth) / 2;
        int posY = (guiHeight - dialogHeight) / 2;
        WidgetGroup dialogContent = new WidgetGroup(posX, posY, dialogWidth, dialogHeight);
        dialogContent.setBackground(GuiTextures.BACKGROUND);

        ButtonWidget closeButton = new ButtonWidget(dialogWidth - 18, 0, 18, 18, GuiTextures.CLOSE_ICON,
                clickData -> closeDialog(dialogOverlay, group));
        dialogContent.addWidget(closeButton);

        dialogContent.addWidget(new LabelWidget(9, 8, Component.translatable("gtceu.gui.circuit.title")));

        // 展示槽位（带 SlotWidget 和覆盖按钮，支持滚轮调整）
        SlotWidget displaySlot = new SlotWidget(handler, 0, (dialogWidth - 18) / 2, 20,
                !ConfigHolder.INSTANCE.machines.ghostCircuit, !ConfigHolder.INSTANCE.machines.ghostCircuit);
        displaySlot.setBackground(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY));
        dialogContent.addWidget(displaySlot);

        // 覆盖按钮，提供与一级 UI 相同的交互（左键递增、右键清空/置零、滚轮调整）
        CircuitSlotButton dialogSlotButton = new CircuitSlotButton((dialogWidth - 18) / 2, 20, 18, 18,
                handler, slotIndex, group);
        dialogContent.addWidget(dialogSlotButton);

        // 添加电路值按钮 (0-26)
        int idx = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 8; y++) {
                int finalIdx = idx;
                ButtonWidget btn = new ButtonWidget(5 + (18 * y), 48 + (18 * x), 18, 18,
                        new GuiTextureGroup(GuiTextures.SLOT,
                                new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                        clickData -> {
                            setCircuitValue(handler, finalIdx, slotIndex, group);
                            closeDialog(dialogOverlay, group);
                        });
                dialogContent.addWidget(btn);
                idx++;
            }
        }
        // 添加电路值按钮 (27-32)
        for (int x = 0; x <= 5; x++) {
            int finalIdx = x + 27;
            ButtonWidget btn = new ButtonWidget(5 + (18 * x), 102, 18, 18,
                    new GuiTextureGroup(GuiTextures.SLOT,
                            new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                    clickData -> {
                        setCircuitValue(handler, finalIdx, slotIndex, group);
                        closeDialog(dialogOverlay, group);
                    });
            dialogContent.addWidget(btn);
        }

        dialogOverlay.addWidget(dialogContent);
        group.addWidget(dialogOverlay);
    }

    private void setCircuitValue(ItemStackHandler handler, int value, int slotIndex, HoverTrackingGroup group) {
        ItemStack newStack = IntCircuitBehaviour.stack(value);
        handler.setStackInSlot(0, newStack);
        circuitValues[slotIndex] = value;
        group.getWriteClientAction(SET_SLOT_TO_N, buf -> {
            buf.writeVarInt(slotIndex);
            buf.writeVarInt(value);
        });
    }

    private void closeDialog(WidgetGroup dialog, HoverTrackingGroup group) {
        group.removeWidget(dialog);
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == SET_SLOT_TO_N) {
            int slotIndex = buffer.readVarInt();
            int value = buffer.readVarInt();
            if (slotIndex >= 0 && slotIndex < circuitInventories.length && circuitInventories[slotIndex] != null) {
                if (value == NO_CONFIG) {
                    circuitInventories[slotIndex].setStackInSlot(0, ItemStack.EMPTY);
                } else {
                    circuitInventories[slotIndex].setStackInSlot(0, IntCircuitBehaviour.stack(value));
                }
                circuitValues[slotIndex] = value;
            }
        }
        // 移除了 SET_TO_ZERO, SET_TO_EMPTY, SET_TO_N 的处理
    }

    @Override
    public List<Component> getTooltips() {
        var list = new ArrayList<>(IFancyConfigurator.super.getTooltips());
        list.addAll(tooltips);
        return list;
    }

    // 自定义按钮：支持左键递增、右键清空/置零、中键打开对话框、滚轮调整
    private class CircuitSlotButton extends ButtonWidget {

        private final ItemStackHandler handler;
        private final int slotIndex;
        private final HoverTrackingGroup group;

        public CircuitSlotButton(int x, int y, int width, int height,
                                 ItemStackHandler handler, int slotIndex, HoverTrackingGroup group) {
            super(x, y, width, height, IGuiTexture.EMPTY, null);
            this.handler = handler;
            this.slotIndex = slotIndex;
            this.group = group;
        }

        @Override
        public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
            if (!isMouseOverElement(mouseX, mouseY)) return false;
            if (wheelDelta > 0) {
                incrementCircuitValue(handler, slotIndex, group);
            } else {
                decrementCircuitValue(handler, slotIndex, group);
            }
            return true; // 事件已消费，阻止传递
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY)) return false;
            if (button == 0) {
                incrementCircuitValue(handler, slotIndex, group);
                return true;
            } else if (button == 1) {
                handleRightClick(handler, slotIndex, group);
                return true;
            } else if (button == 2) {
                if (isRemote()) {
                    openCircuitDialog(slotIndex, group);
                }
                return true;
            }
            return false;
        }
    }

    // 内部类：用于暴露 writeClientAction 方法
    private class HoverTrackingGroup extends WidgetGroup {

        public HoverTrackingGroup(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        public void getWriteClientAction(int id, Consumer<FriendlyByteBuf> dataWriter) {
            this.writeClientAction(id, dataWriter);
        }

        public boolean isRemote() {
            return this.gui != null && this.gui.holder.isRemote();
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            // 将动作转发给外部配置器实例
            MultiCircuitConfigurator.this.handleClientAction(id, buffer);
        }
    }
}
