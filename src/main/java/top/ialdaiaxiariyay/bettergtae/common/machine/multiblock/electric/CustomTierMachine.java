package top.ialdaiaxiariyay.bettergtae.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.bettergtae.common.block.TierBlockRegistry;

import java.util.*;

public class CustomTierMachine extends WorkableElectricMultiblockMachine {

    public final String[] tierTypes;
    private final Map<String, Integer> tierValues = new HashMap<>();

    public CustomTierMachine(BlockEntityCreationInfo holder, String... tierTypes) {
        super(holder);
        this.tierTypes = tierTypes;
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;

        tierValues.clear();
        PatternState state = getDefaultPatternState();
        var cache = state.getCache();

        for (String type : tierTypes) {
            Map<Block, Integer> tierMap = TierBlockRegistry.getTierMap(type);
            int maxTier = 0;
            for (BlockInfo info : cache.values()) {
                Block block = info.getBlockState().getBlock();
                Integer tier = tierMap.get(block);
                if (tier != null && tier > maxTier) {
                    maxTier = tier;
                }
            }
            tierValues.put(type, maxTier);
        }
    }

    @Override
    public void invalidateStructure(@NotNull String name) {
        super.invalidateStructure(name);
        if (DEFAULT_STRUCTURE.equals(name)) {
            tierValues.clear();
        }
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe != null) {
            for (String type : tierTypes) {
                if (recipe.data.contains(type)) {
                    int required = recipe.data.getInt(type);
                    int actual = tierValues.getOrDefault(type, 0);
                    if (actual < required) {
                        return false;
                    }
                }
            }
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public @NotNull List<IWidget> getWidgetsForDisplay(@NotNull PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);
        if (!isFormed) return widgets;

        for (String type : tierTypes) {
            int value = tierValues.getOrDefault(type, 0);
            // 使用 Text.dynamic 创建动态文本控件
            widgets.add(Text.dynamic(() -> Component.translatable("bettergtae.block.tier", type, value))
                    .asWidget());
        }
        return widgets;
    }

    public int getTierValue(String tierType) {
        return tierValues.getOrDefault(tierType, 0);
    }

    public boolean meetsTierRequirement(String tierType, int required) {
        return getTierValue(tierType) >= required;
    }

    public boolean meetsAllTierRequirements(Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            if (!meetsTierRequirement(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
