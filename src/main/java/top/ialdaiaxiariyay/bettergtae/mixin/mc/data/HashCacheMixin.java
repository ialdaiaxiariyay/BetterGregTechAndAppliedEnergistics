package top.ialdaiaxiariyay.bettergtae.mixin.mc.data;

import net.minecraft.data.HashCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HashCache.class)
public abstract class HashCacheMixin {

    @Inject(
            method = "purgeStaleAndWrite",
            at = @At("RETURN"))
    private void onPurgeComplete(CallbackInfo ci) {
        System.exit(0);
    }
}
