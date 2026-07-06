package top.ialdaiaxiariyay.bettergtae.api.pattern;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BGTAEPredicates extends Predicates {

    /**
     * 匹配指定等级（tier）的方块，所有等级的块都允许（不检查一致性）。
     *
     * @param map      等级 -> 方块供应器
     * @param tierType 用于工具提示的等级类型名称
     * @return PatternPredicate
     */
    public static PatternPredicate tieredBlocks(Map<Integer, Supplier<? extends Block>> map, String tierType) {
        return tieredBlocks(map, tierType, -1);
    }

    /**
     * 匹配特定等级的方块。
     *
     * @param map           等级 -> 方块供应器
     * @param tierType      用于工具提示的等级类型名称
     * @param specifiedTier 指定等级，若为 -1 则匹配所有等级
     * @return PatternPredicate
     */
    public static PatternPredicate tieredBlocks(Map<Integer, Supplier<? extends Block>> map, String tierType,
                                                int specifiedTier) {
        // 构建候选 BlockInfo
        List<BlockInfo> candidates = map.entrySet().stream()
                .filter(entry -> specifiedTier < 0 || entry.getKey() == specifiedTier)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> BlockInfo.fromBlock(entry.getValue().get()))
                .collect(Collectors.toList());

        // 测试函数
        return new PatternPredicate(
                "TieredBlocks_" + tierType,
                worldState -> {
                    BlockState blockState = worldState.getBlockState();
                    for (Map.Entry<Integer, Supplier<? extends Block>> entry : map.entrySet()) {
                        if (blockState != null && blockState.is(entry.getValue().get())) {
                            int tier = entry.getKey();
                            if (specifiedTier >= 0 && tier != specifiedTier) {
                                // 若指定等级且不匹配，返回错误
                                return new BlockMatchingError(worldState.getBlockPos(),
                                        candidates.stream().map(BlockInfo::getBlockState).map(BlockState::getBlock)
                                                .collect(Collectors.toList()));
                            }
                            // 匹配成功
                            return null;
                        }
                    }
                    // 无一匹配
                    return new BlockMatchingError(worldState.getBlockPos(),
                            candidates.stream().map(BlockInfo::getBlockState).map(BlockState::getBlock)
                                    .collect(Collectors.toList()));
                },
                candidates).addTooltips(Component.translatable("bettergtae.multiblock.pattern.error.tier"));
    }

    /**
     * 匹配特定类型的方块（泛型）。
     *
     * @param blockMap      类型 -> 方块供应器
     * @param typeKey       类型键名（用于工具提示）
     * @param specifiedType 指定类型，若为 null 则匹配所有类型
     * @param comparator    排序比较器（仅用于候选列表显示，可为 null）
     * @param <T>           类型参数
     * @return PatternPredicate
     */
    public static <T> PatternPredicate typedBlocks(
                                                   Map<T, Supplier<? extends Block>> blockMap,
                                                   String typeKey,
                                                   @Nullable T specifiedType,
                                                   @Nullable Comparator<T> comparator) {
        // 构建候选 BlockInfo
        List<BlockInfo> candidates = blockMap.entrySet().stream()
                .filter(entry -> specifiedType == null || entry.getKey().equals(specifiedType))
                .sorted(comparator == null ?
                        Map.Entry.comparingByKey(Comparator.comparing(Object::toString)) :
                        Map.Entry.comparingByKey(comparator))
                .map(entry -> BlockInfo.fromBlock(entry.getValue().get()))
                .collect(Collectors.toList());

        return new PatternPredicate(
                "TypedBlocks_" + typeKey,
                worldState -> {
                    BlockState blockState = worldState.getBlockState();
                    T matchedType = null;
                    for (Map.Entry<T, Supplier<? extends Block>> entry : blockMap.entrySet()) {
                        if (blockState.is(entry.getValue().get())) {
                            matchedType = entry.getKey();
                            break;
                        }
                    }
                    if (matchedType == null) {
                        // 无匹配
                        return new BlockMatchingError(worldState.getBlockPos(),
                                candidates.stream().map(BlockInfo::getBlockState).map(BlockState::getBlock)
                                        .collect(Collectors.toList()));
                    }
                    if (specifiedType != null && !matchedType.equals(specifiedType)) {
                        // 指定类型不匹配
                        return new BlockMatchingError(worldState.getBlockPos(),
                                candidates.stream().map(BlockInfo::getBlockState).map(BlockState::getBlock)
                                        .collect(Collectors.toList()));
                    }
                    // 匹配成功，不再检查全局一致性（旧API中的MatchContext已移除）
                    return null;
                },
                candidates).addTooltips(Component.translatable("bettergtae.multiblock.pattern.error.tier"));
    }
}
