package top.ialdaiaxiariyay.bettergtae.common.block;

import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class TierBlockRegistry {

    // 类型 -> (方块 -> 等级)
    private static final Map<String, Map<Block, Integer>> TYPE_BLOCK_TIER = new HashMap<>();

    /**
     * 注册一个方块属于某个等级类型，并指定其等级值。
     */
    public static void registerBlock(String tierType, Block block, int tier) {
        TYPE_BLOCK_TIER.computeIfAbsent(tierType, k -> new HashMap<>())
                .put(block, tier);
    }

    /**
     * 获取某个等级类型下的所有方块 -> 等级映射（不可变）。
     */
    public static Map<Block, Integer> getTierMap(String tierType) {
        return TYPE_BLOCK_TIER.getOrDefault(tierType, Map.of());
    }
}
