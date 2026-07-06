package top.ialdaiaxiariyay.bettergtae.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import com.google.common.base.Joiner;
import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.utils.RegistriesUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StructureWriteBehavior implements IItemUIHolder {

    public static final StructureWriteBehavior INSTANCE = new StructureWriteBehavior();

    protected StructureWriteBehavior() {}

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        // 使用 ModularPanel 构建 UI
        var panel = new ModularPanel<>("structure_writer")
                .size(176, 120)
                .background(GTGuiTextures.BACKGROUND);

        // 主容器（类似 WidgetGroup）
        var container = new ParentWidget<>()
                .size(200, 200)
                .pos(8, 8)
                .background(GTGuiTextures.BACKGROUND_INVERSE);

        // 显示坐标信息
        container.addChild(Text.dynamic(() -> {
            var pos = getPos(data.getUsedItemStack());
            if (pos != null) {
                int x = 1 + pos[1].getX() - pos[0].getX();
                int y = 1 + pos[1].getY() - pos[0].getY();
                int z = 1 + pos[1].getZ() - pos[0].getZ();
                return Component.literal(String.format("Structural scale: X:%d Y:%d Z:%d", x, y, z));
            }
            return Component.literal("No structure selected");
        }).asWidget().pos(7, 7).color(0xFAF9F6), -1);

        // 显示方向信息
        container.addChild(Text.dynamic(() -> {
            var dir = getDir(data.getUsedItemStack());
            var dirs = DebugBlockPattern.getDir(dir);
            return Component.literal(
                    String.format("Export order: C:%s S:%s A:%s", dirs[0].name(), dirs[1].name(), dirs[2].name()));
        }).asWidget().pos(7, 20).color(0xFAF9F6), -1);

        // Export 按钮
        container.addChild(
                new ButtonWidget<>()
                        .size(158, 20)
                        .pos(9, 91)
                        .background(GTGuiTextures.BUTTON)
                        .overlay(Text.str("Export").asIcon())
                        .onMousePressed((ctx, btn) -> {
                            export(data);
                            return true;
                        }),
                -1);

        // Rotate X axis 按钮
        container.addChild(
                new ButtonWidget<>()
                        .size(77, 20)
                        .pos(9, 68)
                        .background(GTGuiTextures.BUTTON)
                        .overlay(Text.str("Rotate X axis").asIcon())
                        .onMousePressed((ctx, btn) -> {
                            changeDirX(data);
                            return true;
                        }),
                -1);

        // Rotate Y axis 按钮
        container.addChild(
                new ButtonWidget<>()
                        .size(77, 20)
                        .pos(90, 68)
                        .background(GTGuiTextures.BUTTON)
                        .overlay(Text.str("Rotate Y axis").asIcon())
                        .onMousePressed((ctx, btn) -> {
                            changeDirY(data);
                            return true;
                        }),
                -1);

        panel.addChild(container, -1);
        return panel;
    }

    private void export(PlayerInventoryGuiData<?> playerInventoryHolder) {
        if (getPos(playerInventoryHolder.getUsedItemStack()) != null &&
                playerInventoryHolder.getPlayer() instanceof ServerPlayer) {
            BlockPos[] blockPos = getPos(playerInventoryHolder.getUsedItemStack());
            Direction direction = getDir(playerInventoryHolder.getUsedItemStack());
            StringBuilder builder = new StringBuilder();
            DebugBlockPattern blockPattern;
            if (blockPos != null) {
                blockPattern = new DebugBlockPattern(
                        playerInventoryHolder.getPlayer().level(),
                        blockPos[0].getX(),
                        blockPos[0].getY(),
                        blockPos[0].getZ(),
                        blockPos[1].getX(),
                        blockPos[1].getY(),
                        blockPos[1].getZ());
                RelativeDirection[] dirs = DebugBlockPattern.getDir(direction);
                blockPattern.changeDir(dirs[0], dirs[1], dirs[2]);
                builder.append(".pattern(definition -> FactoryBlockPattern.start()\n");
                for (int i = 0; i < blockPattern.pattern.length; i++) {
                    String[] strings = blockPattern.pattern[i];
                    builder.append(".aisle(\"%s\")\n".formatted(Joiner.on("\", \"").join(strings)));
                }
                builder.append(".where(\"~\", Predicates.controller(Predicates.blocks(definition.get())))\n");
                blockPattern.legend.forEach((b, c) -> {
                    if (c.equals(' ')) return;
                    builder.append(".where(\"").append(c).append("\", Predicates.blocks(RegistriesUtil.getBlock(\"")
                            .append(RegistriesUtil.BlockId(b)).append("\")))\n");
                });
            }
            String target = ".where(\"~\", Predicates.blocks(Registries.getBlock(\"minecraft:oak_log\")))";
            int startIndex = builder.indexOf(target);
            if (startIndex != -1) {
                int endIndex = startIndex + target.length() + 1;
                builder.delete(startIndex, endIndex);
            }
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String fileName = now.format(formatter) + ".txt";
            File logDir = new File("logs/bp");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            File logFile = new File(logDir, fileName);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
                writer.write(builder.toString());
            } catch (IOException e) {
                BetterGTAE.LOGGER.error("Error writing to log file: " + e.getMessage());
            }
        }
    }

    private void changeDirX(PlayerInventoryGuiData<?> playerInventoryHolder) {
        if (getPos(playerInventoryHolder.getUsedItemStack()) != null &&
                playerInventoryHolder.getPlayer() instanceof ServerPlayer) {
            ItemStack itemStack = playerInventoryHolder.getUsedItemStack();
            Direction direction = getDir(itemStack);
            direction = direction.getClockWise(Direction.Axis.X);
            setDir(itemStack, direction);
        }
    }

    private void changeDirY(PlayerInventoryGuiData<?> playerInventoryHolder) {
        if (getPos(playerInventoryHolder.getUsedItemStack()) != null &&
                playerInventoryHolder.getPlayer() instanceof ServerPlayer) {
            ItemStack itemStack = playerInventoryHolder.getUsedItemStack();
            Direction direction = getDir(itemStack);
            direction = direction.getClockWise(Direction.Axis.Y);
            setDir(itemStack, direction);
        }
    }

    public static boolean isItemStructureWriter(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() instanceof ComponentItem item) {
            return item.getComponents().contains(INSTANCE);
        }
        return false;
    }

    public static Direction getDir(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement("structure_writer");
        if (!tag.contains("dir")) return Direction.WEST;
        return Direction.byName(tag.getString("dir"));
    }

    public static void setDir(ItemStack stack, Direction dir) {
        CompoundTag tag = stack.getOrCreateTagElement("structure_writer");
        tag.putString("dir", dir.getName());
    }

    public static BlockPos[] getPos(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement("structure_writer");
        if (!tag.contains("minX")) return null;
        return new BlockPos[] {
                new BlockPos(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ")),
                new BlockPos(tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ"))
        };
    }

    public static void addPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTagElement("structure_writer");
        if (!tag.contains("minX") || tag.getInt("minX") > pos.getX()) {
            tag.putInt("minX", pos.getX());
        }
        if (!tag.contains("maxX") || tag.getInt("maxX") < pos.getX()) {
            tag.putInt("maxX", pos.getX());
        }

        if (!tag.contains("minY") || tag.getInt("minY") > pos.getY()) {
            tag.putInt("minY", pos.getY());
        }
        if (!tag.contains("maxY") || tag.getInt("maxY") < pos.getY()) {
            tag.putInt("maxY", pos.getY());
        }

        if (!tag.contains("minZ") || tag.getInt("minZ") > pos.getZ()) {
            tag.putInt("minZ", pos.getZ());
        }
        if (!tag.contains("maxZ") || tag.getInt("maxZ") < pos.getZ()) {
            tag.putInt("maxZ", pos.getZ());
        }
    }

    public static void removePos(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement("structure_writer");
        tag.remove("minX");
        tag.remove("maxX");
        tag.remove("minY");
        tag.remove("maxY");
        tag.remove("minZ");
        tag.remove("maxZ");
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(context.getHand());
        if (!player.isShiftKeyDown()) {
            addPos(stack, context.getClickedPos());
        } else {
            removePos(stack);
        }
        return InteractionResult.SUCCESS;
    }
}
