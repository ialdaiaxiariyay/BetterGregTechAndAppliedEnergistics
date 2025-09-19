package top.ialdaiaxiariyay.bettergtae.utils;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RegistriesUtil {

    public static @NotNull String BlockId(Block block) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString();
    }

    public static Block getBlock(String string) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(string));
        if (block == null) {
            BetterGTAE.LOGGER.error("Block {} is null", string);
            return Blocks.AIR;
        }
        return block;
    }

    public static @NotNull String ItemId(Item item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString();
    }

    public static Item getItem(String string) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(string));
        if (item == null) {
            BetterGTAE.LOGGER.error("Item {} is null", string);
            return Items.AIR;
        }
        return item;
    }

    public static Fluid getFluid(String string) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(string));
        if (fluid == null) {
            BetterGTAE.LOGGER.error("Fluid {} is null", string);
            return Fluids.WATER;
        }
        return fluid;
    }

    public static @NotNull String FluidId(Fluid fluids) {
        return Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluids)).toString();
    }
}
