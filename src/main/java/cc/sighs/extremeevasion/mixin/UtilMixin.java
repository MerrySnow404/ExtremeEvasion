package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.BulletTimeClock;
import cc.sighs.extremeevasion.BulletTimeController;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Util.class, priority = Integer.MAX_VALUE)
public abstract class UtilMixin {
    @Inject(method = "getNanos", at = @At("HEAD"), cancellable = true)
    private static void extremeevasion$getBulletTimeNanos(CallbackInfoReturnable<Long> cir) {
        if (!BulletTimeClock.isBypassBulletTime() && BulletTimeController.shouldOverrideMillis()) {
            cir.setReturnValue(BulletTimeController.getNanos());
        }
    }

    @Inject(method = "getMillis", at = @At("HEAD"), cancellable = true)
    private static void extremeevasion$getBulletTimeMillis(CallbackInfoReturnable<Long> cir) {
        if (!BulletTimeClock.isBypassBulletTime() && BulletTimeController.shouldOverrideMillis()) {
            cir.setReturnValue(BulletTimeController.getMillis());
        }
    }
}
