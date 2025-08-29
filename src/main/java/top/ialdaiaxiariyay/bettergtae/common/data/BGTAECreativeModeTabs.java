package top.ialdaiaxiariyay.bettergtae.common.data;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMachines;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate.REGISTRATE;

public class BGTAECreativeModeTabs {

    public static void init() {}

    public static RegistryEntry<CreativeModeTab> MACHINE = REGISTRATE.defaultCreativeTab("machine",
            builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("machine", REGISTRATE))
                    .icon(BGTAEMachines.EXTEND_ME_PATTERN_BUFFER::asStack)
                    .title(REGISTRATE.addLang("itemGroup", BetterGTAE.id("machine"),
                            BetterGTAE.NAME + " Machine Containers"))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> BLOCK = REGISTRATE.defaultCreativeTab("block",
            builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("block", REGISTRATE))
                    .icon(BGTAEBlocks.COMPUTING_CORE_MAX::asStack)
                    .title(REGISTRATE.addLang("itemGroup", BetterGTAE.id("block"),
                            BetterGTAE.NAME + " Block Containers"))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> ITEM = REGISTRATE.defaultCreativeTab("item",
            builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("item", REGISTRATE))
                    .icon(BGTAEItems.STRUCTURE_TOOLS::asStack)
                    .title(REGISTRATE.addLang("itemGroup", BetterGTAE.id("item"),
                            BetterGTAE.NAME + " Item Containers"))
                    .build())
            .register();
}
