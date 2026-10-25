package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import cc.sighs.extremeevasion.handler.EchoHandler;
import cc.sighs.extremeevasion.handler.TrajectoryAABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private Vec3 position;

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void extremeevasion$trajectoryBox(CallbackInfoReturnable<AABB> callback) {
        Entity self = (Entity) (Object) this;
        if (EchoHandler.shouldModifyBoundingBox(self)) {
            Player player = (Player) self;
            callback.setReturnValue(new TrajectoryAABB(ExtremeEvasionEvents.getExtremeEvasionStartAABB(player), callback.getReturnValue()));
        }
    }

    @Inject(method = "position", at = @At("RETURN"), cancellable = true)
    private void extremeevasion$position(CallbackInfoReturnable<Vec3> callback) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player && EchoHandler.shouldModifyPosition(player))
            callback.setReturnValue(EchoHandler.getEvasionPosition(player, callback.getReturnValue()));
    }

    @Inject(method = "getX()D", at = @At("RETURN"), cancellable = true)
    private void extremeevasion$x(CallbackInfoReturnable<Double> callback) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player && EchoHandler.shouldModifyPosition(player))
            callback.setReturnValue(EchoHandler.getEvasionPosition(player, new Vec3(callback.getReturnValue(), position.y, position.z)).x);
    }

    @Inject(method = "getY()D", at = @At("RETURN"), cancellable = true)
    private void extremeevasion$y(CallbackInfoReturnable<Double> callback) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player && EchoHandler.shouldModifyPosition(player))
            callback.setReturnValue(EchoHandler.getEvasionPosition(player, new Vec3(position.x, callback.getReturnValue(), position.z)).y);
    }

    @Inject(method = "getZ()D", at = @At("RETURN"), cancellable = true)
    private void extremeevasion$z(CallbackInfoReturnable<Double> callback) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player && EchoHandler.shouldModifyPosition(player))
            callback.setReturnValue(EchoHandler.getEvasionPosition(player, new Vec3(position.x, position.y, callback.getReturnValue())).z);
    }

    @Inject(method = "getEyeY", at = @At("RETURN"), cancellable = true)
    private void extremeevasion$eyeY(CallbackInfoReturnable<Double> callback) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player && EchoHandler.shouldModifyPosition(player))
            callback.setReturnValue(EchoHandler.getEvasionPosition(player, position).y + self.getEyeHeight());
    }
}
