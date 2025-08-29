package top.ialdaiaxiariyay.bettergtae.common.block;

import net.minecraft.world.item.Item;

import appeng.block.crafting.ICraftingUnitType;
import com.tterrag.registrate.util.entry.BlockEntry;
import lombok.Getter;
import top.ialdaiaxiariyay.bettergtae.common.data.BGTAEBlocks;

public enum BGTAECraftingUnitType implements ICraftingUnitType {

    STORAGE_1M(1, "1m_storage"),
    STORAGE_4M(4, "4m_storage"),
    STORAGE_16M(16, "16m_storage"),
    STORAGE_64M(64, "64m_storage"),
    STORAGE_256M(256, "256m_storage"),
    STORAGE_MAX(-1, "max_storage");

    private final int storageMb;

    @Getter
    private final String affix;

    BGTAECraftingUnitType(int storageMb, String affix) {
        this.storageMb = storageMb;
        this.affix = affix;
    }

    @Override
    public long getStorageBytes() {
        return storageMb == -1 ? Long.MAX_VALUE : 1024L * 1024 * storageMb;
    }

    @Override
    public int getAcceleratorThreads() {
        return storageMb == -1 ? Integer.MAX_VALUE - 1 : 1024 * 1024 * storageMb;
    }

    public BlockEntry<BGTAECraftingUnitBlock> getDefinition() {
        return switch (this) {
            case STORAGE_1M -> BGTAEBlocks.COMPUTING_CORE_1M;
            case STORAGE_4M -> BGTAEBlocks.COMPUTING_CORE_4M;
            case STORAGE_16M -> BGTAEBlocks.COMPUTING_CORE_16M;
            case STORAGE_64M -> BGTAEBlocks.COMPUTING_CORE_64M;
            case STORAGE_256M -> BGTAEBlocks.COMPUTING_CORE_256M;
            case STORAGE_MAX -> BGTAEBlocks.COMPUTING_CORE_MAX;
        };
    }

    @Override
    public Item getItemFromType() {
        return getDefinition().asItem();
    }
}
