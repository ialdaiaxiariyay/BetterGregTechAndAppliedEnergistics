package top.ialdaiaxiariyay.bettergtae.integration.jade;

import top.ialdaiaxiariyay.bettergtae.integration.jade.provider.ExtendMEPatternBufferProvider;
import top.ialdaiaxiariyay.bettergtae.integration.jade.provider.ExtendMEPatternBufferProxyProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class BGTAEJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ExtendMEPatternBufferProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ExtendMEPatternBufferProxyProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ExtendMEPatternBufferProvider(), Block.class);
        registration.registerBlockComponent(new ExtendMEPatternBufferProxyProvider(), Block.class);
    }
}
