package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import cc.sighs.extremeevasion.client.CounterAttackClientState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttackStrengthMixin {
    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void extremeevasion$getFullCounterAttackStrength(float partialTick, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (ExtremeEvasionEvents.hasCounterAttackBoost(player)
                || player.level().isClientSide() && CounterAttackClientState.isActive()) {
            cir.setReturnValue(1.0F);
        }
    }
}
