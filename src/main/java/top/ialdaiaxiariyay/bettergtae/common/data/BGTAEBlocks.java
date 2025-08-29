package top.ialdaiaxiariyay.bettergtae.common.data;

import top.ialdaiaxiariyay.bettergtae.common.block.BGTAECraftingUnitBlock;
import top.ialdaiaxiariyay.bettergtae.common.block.BGTAECraftingUnitType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import static top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate.REGISTRATE;

public class BGTAEBlocks {

    public static void init() {}

    static {
        REGISTRATE.creativeModeTab(() -> BGTAECreativeModeTabs.BLOCK);
    }

    private static BlockEntry<BGTAECraftingUnitBlock> registerCraftingUnitBlock(int tier, BGTAECraftingUnitType Type) {
        BlockEntry<BGTAECraftingUnitBlock> Block = REGISTRATE
                .block(tier == -1 ? "max_computing_core" : tier + "m_computing_core",
                        p -> new BGTAECraftingUnitBlock(Type))
                .blockstate((ctx, provider) -> {
                    String baseName = ctx.getName();
                    String formed = "block/crafting/" + baseName + "_formed";
                    String unformed = "block/crafting/" + baseName;
                    provider.models().cubeAll(unformed, provider.modLoc("block/crafting/" + baseName));
                    provider.models().getBuilder(formed)
                            .parent(provider.models().getExistingFile(new ResourceLocation("block/block")))
                            .texture("particle", provider.modLoc("block/crafting/" + baseName))
                            .texture("light", provider.modLoc("block/crafting/" + baseName + "_light"))
                            .element()
                            .from(0, 0, 0)
                            .to(16, 16, 16)
                            .allFaces((dir, face) -> face.texture("#light"))
                            .shade(true)
                            .end();
                    provider.getVariantBuilder(ctx.get())
                            .forAllStatesExcept(state -> {
                                boolean isFormed = state.getValue(AbstractCraftingUnitBlock.FORMED);
                                return ConfiguredModel.builder()
                                        .modelFile(provider.models()
                                                .getExistingFile(provider.modLoc(isFormed ? formed : unformed)))
                                        .build();
                            }, AbstractCraftingUnitBlock.POWERED);
                })
                .defaultLoot()
                .item(BlockItem::new)
                .model((ctx, provider) -> provider.withExistingParent(ctx.getName(),
                        provider.modLoc("block/crafting/" + ctx.getName())))
                .build()
                .register();
        REGISTRATE.setCreativeTab(Block, BGTAECreativeModeTabs.BLOCK);
        return Block;
    }

    public static final BlockEntry<BGTAECraftingUnitBlock> COMPUTING_CORE_1M = registerCraftingUnitBlock(1,
            BGTAECraftingUnitType.STORAGE_1M);
    public static final BlockEntry<BGTAECraftingUnitBlock> COMPUTING_CORE_4M = registerCraftingUnitBlock(4,
            BGTAECraftingUnitType.STORAGE_4M);
    public static final BlockEntry<BGTAECraftingUnitBlock> COMPUTING_CORE_16M = registerCraftingUnitBlock(16,
            BGTAECraftingUnitType.STORAGE_16M);
    public static final BlockEntry<BGTAECraftingUnitBlock> COMPUTING_CORE_64M = registerCraftingUnitBlock(64,
            BGTAECraftingUnitType.STORAGE_64M);
    public static final BlockEntry<BGTAECraftingUnitBlock> COMPUTING_CORE_256M = registerCraftingUnitBlock(256,
            BGTAECraftingUnitType.STORAGE_256M);
    public static final BlockEntry<BGTAECraftingUnitBlock> COMPUTING_CORE_MAX = registerCraftingUnitBlock(-1,
            BGTAECraftingUnitType.STORAGE_MAX);

    public static BlockEntityEntry<CraftingBlockEntity> COMPUTING_CORE = REGISTRATE
            .blockEntity("crafting_storage", CraftingBlockEntity::new)
            .validBlocks(
                    COMPUTING_CORE_1M,
                    COMPUTING_CORE_4M,
                    COMPUTING_CORE_16M,
                    COMPUTING_CORE_64M,
                    COMPUTING_CORE_256M,
                    COMPUTING_CORE_MAX)
            .onRegister(type -> {
                for (BGTAECraftingUnitType BGTAECraftingUnitType : BGTAECraftingUnitType.values()) {
                    AEBaseBlockEntity.registerBlockEntityItem(type, BGTAECraftingUnitType.getItemFromType());
                    BGTAECraftingUnitType.getDefinition().get().setBlockEntity(CraftingBlockEntity.class, type, null, null);
                }
            })
            .register();
}
