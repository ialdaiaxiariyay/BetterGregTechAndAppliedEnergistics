package top.ialdaiaxiariyay.bettergtae.mixin.ae;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

import appeng.core.localization.Tooltips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;

@Mixin(Tooltips.class)
public final class TooltipsMixin {

    @Inject(method = "ofBytes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void ofBytes(long number, CallbackInfoReturnable<MutableComponent> cir) {
        cir.setReturnValue(NumberUtil.numberText(number).withStyle(ChatFormatting.BLUE));
    }
}
