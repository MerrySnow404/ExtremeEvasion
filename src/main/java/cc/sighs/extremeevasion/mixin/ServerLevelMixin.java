package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.handler.EchoHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"))
    private void extremeEvasion$prepareActivePlayers(BooleanSupplier hasTimeLeft, CallbackInfo callbackInfo) {
        EchoHandler.prepareLevelTick((ServerLevel) (Object) this);
    }

    @Inject(method = "tickNonPassenger", at = @At("HEAD"))
    private void extremeEvasion$beginEntityTick(Entity entity, CallbackInfo callbackInfo) {
        EchoHandler.currentEntity = entity;
        EchoHandler.modifiesBoundingBoxAndPos.set(EchoHandler.shouldModifyFor(entity));
    }

    @Inject(method = "tickNonPassenger", at = @At("RETURN"))
    private void extremeEvasion$endEntityTick(Entity entity, CallbackInfo callbackInfo) {
        EchoHandler.modifiesBoundingBoxAndPos.remove();
        EchoHandler.currentEntity = null;
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"))
    private void extremeEvasion$beginPassengerTick(Entity vehicle, Entity passenger, CallbackInfo callbackInfo) {
        EchoHandler.currentEntity = passenger;
        EchoHandler.modifiesBoundingBoxAndPos.set(EchoHandler.shouldModifyFor(passenger));
    }

    @Inject(method = "tickPassenger", at = @At("RETURN"))
    private void extremeEvasion$endPassengerTick(Entity vehicle, Entity passenger, CallbackInfo callbackInfo) {
        EchoHandler.modifiesBoundingBoxAndPos.remove();
        EchoHandler.currentEntity = null;
    }
}
