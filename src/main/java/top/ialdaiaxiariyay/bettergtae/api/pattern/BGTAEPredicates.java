package top.ialdaiaxiariyay.bettergtae.api.pattern;

import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib.utils.BlockInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;

public class BGTAEPredicates extends Predicates {

    /**
     * <p>
     * {@code tieredBlocks(MACHINE_CASINGS, "MachineCasing"); All Tier}
     * </p>
     * <P>
     * {@code tieredBlocks(MACHINE_CASINGS, "MachineCasing", 2); Tier is 2}
     * </p>
     */

    public static TraceabilityPredicate tieredBlocks(Map<Integer, Supplier<? extends Block>> map, String tierType) {
        return tieredBlocks(map, tierType, -1);
    }

    public static TraceabilityPredicate tieredBlocks(Map<Integer, Supplier<? extends Block>> map, String tierType, int specifiedTier) {
        BlockInfo[] blockInfos;

        if (specifiedTier >= 0) {
            var supplier = map.get(specifiedTier);
            if (supplier != null) {
                var block = supplier.get();
                blockInfos = new BlockInfo[] { BlockInfo.fromBlockState(block.defaultBlockState()) };
            } else {
                blockInfos = new BlockInfo[0];
            }
        } else {
            blockInfos = map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        var block = entry.getValue().get();
                        return BlockInfo.fromBlockState(block.defaultBlockState());
                    })
                    .toArray(BlockInfo[]::new);
        }

        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (var entry : map.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    var tier = entry.getKey();

                    if (specifiedTier >= 0 && tier != specifiedTier) {
                        return false;
                    }

                    Object currentTier = blockWorldState.getMatchContext().getOrPut(tierType, tier);
                    if (!currentTier.equals(tier)) {
                        blockWorldState.setError(new PatternStringError("bettergtae.multiblock.pattern.error.tier"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> blockInfos).addTooltips(Component.translatable("bettergtae.multiblock.pattern.error.tier"));
    }

    /**
     * <p>
     * {@code typedBlocks(COILS, "CoilType", null, Comparator.comparing(CoilType::getTier));}
     * </p>
     * <p>
     * {@code  typedBlocks(COILS, "CoilType", CoilType.KANTHAL, null);}
     * </p>
     */

    public static <T> TraceabilityPredicate typedBlocks(
                                                        Map<T, Supplier<? extends Block>> blockMap,
                                                        String typeKey,
                                                        @Nullable T specifiedType,
                                                        @Nullable Comparator<T> comparator) {
        BlockInfo[] blockInfos;
        if (specifiedType != null) {
            var supplier = blockMap.get(specifiedType);
            if (supplier != null) {
                var block = supplier.get();
                blockInfos = new BlockInfo[] { BlockInfo.fromBlockState(block.defaultBlockState()) };
            } else {
                blockInfos = new BlockInfo[0];
            }
        } else {
            var stream = blockMap.entrySet().stream();

            if (comparator != null) {
                stream = stream.sorted(Map.Entry.comparingByKey(comparator));
            }

            blockInfos = stream
                    .map(entry -> {
                        var block = entry.getValue().get();
                        return BlockInfo.fromBlockState(block.defaultBlockState());
                    })
                    .toArray(BlockInfo[]::new);
        }

        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            T blockType = null;
            for (var entry : blockMap.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    blockType = entry.getKey();
                    break;
                }
            }

            if (blockType == null) {
                return false;
            }

            if (specifiedType != null && !blockType.equals(specifiedType)) {
                return false;
            }

            Object currentType = blockWorldState.getMatchContext().getOrPut(typeKey, blockType);
            if (!currentType.equals(blockType)) {
                blockWorldState.setError(new PatternStringError("bettergtae.multiblock.pattern.error.tier"));
                return false;
            }

            return true;
        }, () -> blockInfos).addTooltips(Component.translatable("bettergtae.multiblock.pattern.error.tier"));
    }
}
