package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.handler.EchoHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"))
    private void extremeevasion$begin(Entity entity, CallbackInfo callback) {
        EchoHandler.currentEntity = entity;
        EchoHandler.modifiesBoundingBoxAndPos.set(EchoHandler.shouldModifyFor(entity));
    }

    @Inject(method = "tickNonPassenger", at = @At("RETURN"))
    private void extremeevasion$end(Entity entity, CallbackInfo callback) {
        EchoHandler.modifiesBoundingBoxAndPos.remove();
        EchoHandler.currentEntity = null;
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"))
    private void extremeevasion$beginPassenger(Entity vehicle, Entity passenger, CallbackInfo callback) {
        EchoHandler.currentEntity = passenger;
        EchoHandler.modifiesBoundingBoxAndPos.set(EchoHandler.shouldModifyFor(passenger));
    }

    @Inject(method = "tickPassenger", at = @At("RETURN"))
    private void extremeevasion$endPassenger(Entity vehicle, Entity passenger, CallbackInfo callback) {
        EchoHandler.modifiesBoundingBoxAndPos.remove();
        EchoHandler.currentEntity = null;
    }
}
