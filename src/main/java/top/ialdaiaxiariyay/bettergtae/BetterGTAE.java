package top.ialdaiaxiariyay.bettergtae;

import top.ialdaiaxiariyay.bettergtae.client.ClientProxy;
import top.ialdaiaxiariyay.bettergtae.common.CommonProxy;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.resources.ResourceLocation.tryBuild;

@Mod(BetterGTAE.MOD_ID)
public class BetterGTAE {

    public static final String MOD_ID = "bettergtae";
    public static final String NAME = "BetterGTAE";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static ResourceLocation id(String name) {
        return tryBuild(MOD_ID, name);
    }

    public BetterGTAE() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
