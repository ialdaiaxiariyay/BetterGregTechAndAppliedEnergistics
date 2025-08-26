package top.ialdaiaxiariyay.bettergtae.api.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyCustomMiddleClickAction;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyCustomMouseWheelAction;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Accessors(chain = true)
public class MultiCircuitConfigurator implements IFancyConfigurator, IFancyCustomMouseWheelAction, IFancyCustomMiddleClickAction {

    private static final int SET_TO_ZERO = 2;
    private static final int SET_TO_EMPTY = 3;
    private static final int SET_TO_N = 4;
    private static final int SET_SLOT_TO_N = 5;

    private static final int NO_CONFIG = -1;

    @Persisted
    @DescSynced
    private int[] circuitValues;

    private final ItemStackHandler[] circuitInventories;
    private final Component title;

    @Setter
    private List<Component> tooltips;

    @Setter
    private int hoveredSlotIndex = -1;
    private int lastClickedSlotIndex = -1;
    private int lastClickedNewValue = -1;

    public MultiCircuitConfigurator(ItemStackHandler[] circuitInventories, Component title) {
        this.circuitInventories = circuitInventories;
        this.title = title;
        this.tooltips = List.of(
                Component.translatable("gtceu.gui.configurator_slot.tooltip.0"));

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
    public boolean mouseWheelMove(BiConsumer<Integer, Consumer<FriendlyByteBuf>> writeClientAction,
                                  double mouseX, double mouseY, double wheelDelta) {
        if (wheelDelta == 0 || hoveredSlotIndex == -1) return false;

        ItemStackHandler circuitInventory = circuitInventories[hoveredSlotIndex];
        if (!ConfigHolder.INSTANCE.machines.ghostCircuit && circuitInventory.getStackInSlot(0).isEmpty())
            return false;

        int nextValue = getNextValue(hoveredSlotIndex, wheelDelta > 0);
        if (nextValue == NO_CONFIG) {
            if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
                writeClientAction.accept(SET_SLOT_TO_N, buf -> {
                    buf.writeVarInt(hoveredSlotIndex);
                    buf.writeVarInt(NO_CONFIG);
                });
            }
        } else {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(nextValue));
            writeClientAction.accept(SET_SLOT_TO_N, buf -> {
                buf.writeVarInt(hoveredSlotIndex);
                buf.writeVarInt(nextValue);
            });
        }
        return true;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        switch (id) {
            case SET_TO_ZERO -> {
                for (ItemStackHandler circuitInventory : circuitInventories) {
                    if (circuitInventory != null &&
                            (ConfigHolder.INSTANCE.machines.ghostCircuit || !circuitInventory.getStackInSlot(0).isEmpty())) {
                        circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(0));
                    }
                }
            }
            case SET_TO_EMPTY -> {
                for (ItemStackHandler circuitInventory : circuitInventories) {
                    if (circuitInventory != null &&
                            (ConfigHolder.INSTANCE.machines.ghostCircuit || circuitInventory.getStackInSlot(0).isEmpty())) {
                        circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
                    } else if (circuitInventory != null) {
                        circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(0));
                    }
                }
            }
            case SET_TO_N -> {
                int value = buffer.readVarInt();
                for (ItemStackHandler circuitInventory : circuitInventories) {
                    if (circuitInventory != null &&
                            (ConfigHolder.INSTANCE.machines.ghostCircuit || !circuitInventory.getStackInSlot(0).isEmpty())) {
                        circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(value));
                    }
                }
            }
            case SET_SLOT_TO_N -> {
                int slotIndex = buffer.readVarInt();
                int value = buffer.readVarInt();
                if (slotIndex >= 0 && slotIndex < circuitInventories.length && circuitInventories[slotIndex] != null) {
                    if (value == NO_CONFIG) {
                        circuitInventories[slotIndex].setStackInSlot(0, ItemStack.EMPTY);
                    } else {
                        circuitInventories[slotIndex].setStackInSlot(0, IntCircuitBehaviour.stack(value));
                    }
                    // 更新保存的电路值
                    circuitValues[slotIndex] = value;
                }
            }
        }
    }

    @Override
    public Widget createConfigurator() {
        int slotCount = circuitInventories.length;
        int rowSize = (int) Math.ceil(Math.sqrt(slotCount));

        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * rowSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * rowSize + 8);

        for (int i = 0; i < slotCount; i++) {
            final int slotIndex = i;
            ItemStackHandler circuitInventory = circuitInventories[i];
            if (circuitInventory != null) {
                int x = 4 + (i % rowSize) * 18;
                int y = 2 + (i / rowSize) * 18;
                SlotWidget circuitSlot = new SlotWidget(circuitInventory, 0, x, y, false, false);
                circuitSlot.setBackground(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY));
                ButtonWidget middleClickButton = new ButtonWidget(x, y, 18, 18, IGuiTexture.EMPTY,
                        clickData -> {
                            if (clickData.button == 0) {
                                handleLeftClick(circuitInventory, slotIndex, clickData.isRemote);
                            } else if ((clickData.button == 1)) {
                                handleRightClick(circuitInventory, slotIndex, clickData.isRemote);
                            }
                        });
                // 悬停提示
                middleClickButton.setHoverTooltips(
                        Component.translatable("bettergtae.gui.circuit.middle_click_tooltip"));

                container.addWidget(circuitSlot);
                container.addWidget(middleClickButton);
            }
        }
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }


    private void handleLeftClick(ItemStackHandler circuitInventory, int slotIndex, boolean isRemote) {
        ItemStack currentStack = circuitInventory.getStackInSlot(0);
        int newValue;

        if (currentStack.isEmpty()) {
            // 如果槽位为空，设置为电路值0
            newValue = 0;
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
        } else if (IntCircuitBehaviour.isIntegratedCircuit(currentStack)) {
            // 如果已经是电路，增加电路值
            int currentValue = IntCircuitBehaviour.getCircuitConfiguration(currentStack);
            newValue = (currentValue + 1) % (IntCircuitBehaviour.CIRCUIT_MAX + 1);

            if (newValue == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit) {
                // 如果不允许幽灵电路且下一个值是0，则清空槽位
                circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
                newValue = -1; // 表示空槽位
            } else {
                // 否则设置为下一个值
                circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
            }
        } else {
            // 如果不是电路，设置为电路值0
            newValue = 0;
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(newValue));
        }

        // 更新保存的电路值
        circuitValues[slotIndex] = newValue;

        // 如果是客户端，发送动作到服务器
        if (isRemote) {
            lastClickedSlotIndex = slotIndex;
            lastClickedNewValue = newValue;
        }
    }

    private void handleRightClick(ItemStackHandler circuitInventory, int slotIndex, boolean isRemote) {
        int newValue;
        if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            // 如果允许幽灵电路，则清空槽位
            circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
            newValue = -1; // 表示空槽位
        } else {
            // 如果不允许幽灵电路，则设置为0
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(0));
            newValue = 0;
        }
        circuitValues[slotIndex] = newValue;
        if (isRemote) {
            lastClickedSlotIndex = slotIndex;
            lastClickedNewValue = newValue;
        }
    }

    @Override
    public void onMiddleClick(BiConsumer<Integer, Consumer<FriendlyByteBuf>> writeClientAction) {
        // 发送最后一次点击的信息
        if (lastClickedSlotIndex != -1) {
            writeClientAction.accept(SET_SLOT_TO_N, buf -> {
                buf.writeVarInt(lastClickedSlotIndex);
                buf.writeVarInt(lastClickedNewValue);
            });
            // 重置
            lastClickedSlotIndex = -1;
            lastClickedNewValue = -1;
        }
    }

    @Override
    public List<Component> getTooltips() {
        var list = new ArrayList<>(IFancyConfigurator.super.getTooltips());
        list.addAll(tooltips);
        return list;
    }

    private int getNextValue(int slotIndex, boolean increment) {
        ItemStackHandler circuitInventory = circuitInventories[slotIndex];
        int currentValue = IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0));

        if (increment) {
            if (currentValue == IntCircuitBehaviour.CIRCUIT_MAX) {
                return 0;
            }
            if (circuitInventory.getStackInSlot(0).isEmpty()) {
                return 1;
            }
            return currentValue + 1;
        } else {
            if (circuitInventory.getStackInSlot(0).isEmpty() ||
                    (currentValue == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit)) {
                return IntCircuitBehaviour.CIRCUIT_MAX;
            }
            if (currentValue == 1 && ConfigHolder.INSTANCE.machines.ghostCircuit) {
                return -1;
            }
            return currentValue - 1;
        }
    }
}
