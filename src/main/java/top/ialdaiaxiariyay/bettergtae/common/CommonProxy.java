package top.ialdaiaxiariyay.bettergtae.common;

import top.ialdaiaxiariyay.bettergtae.BetterGTAE;
import top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate;
import top.ialdaiaxiariyay.bettergtae.common.data.BGTAECreativeModeTabs;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMachines;
import top.ialdaiaxiariyay.bettergtae.common.item.InfinityCellGuiHandler;
import top.ialdaiaxiariyay.bettergtae.common.item.InfinityCellHandler;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.api.storage.StorageCells;

public class CommonProxy {

    public CommonProxy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BGTAERegistrate.REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        BGTAECreativeModeTabs.init();
        BetterGTAE.LOGGER.info("CommonProxy is Load");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("gtocore") && ModList.get().isLoaded("gtolib")) {
            throw new RuntimeException("Unreachable operation and not compatible with mod GTOCORE, GTOLIB");
        }
        StorageCells.addCellHandler(InfinityCellHandler.INSTANCE);
        StorageCells.addCellGuiHandler(new InfinityCellGuiHandler());
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        BGTAEMachines.init();
    }
}
