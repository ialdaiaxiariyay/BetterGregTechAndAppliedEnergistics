package top.ialdaiaxiariyay.bettergtae;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.ialdaiaxiariyay.bettergtae.api.registrate.BGTAERegistrate;
import top.ialdaiaxiariyay.bettergtae.common.data.BGTAECreativeModeTabs;
import top.ialdaiaxiariyay.bettergtae.common.data.machine.BGTAEMachines;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import static net.minecraft.resources.ResourceLocation.tryBuild;

@Mod(BetterGTAE.MOD_ID)
public class BetterGTAE {

    public static final String MOD_ID = "bettergtae";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String NAME = "BetterGTAE";

    public static ResourceLocation id(String name) {
        return tryBuild(MOD_ID, name);
    }

    public BetterGTAE(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        BGTAERegistrate.REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        BGTAECreativeModeTabs.init();
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("gtocore") && ModList.get().isLoaded("gtolib")) {
            throw new RuntimeException("Unreachable operation and not compatible with mod GTOCORE, GTOLIB");
        }
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        BGTAEMachines.init();
    }
}
