package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import cc.sighs.extremeevasion.handler.EchoHandler;
import cc.sighs.extremeevasion.handler.TrajectoryAABB;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private Vec3 position;

    @ModifyReturnValue(method = "getBoundingBox", at = @At("RETURN"))
    private AABB modifyBoundingBox(AABB original) {
        if (!EchoHandler.shouldModifyBoundingBox(this)) return original;

        var player = (Player) (Object) this;
        return new TrajectoryAABB(ExtremeEvasionEvents.getExtremeEvasionStartAABB(player), original);
    }

    @ModifyReturnValue(method = "position", at = @At("RETURN"))
    private Vec3 modifyPosition(Vec3 original) {
        if (!((Object) this instanceof Player player) || !EchoHandler.shouldModifyPosition(player)) return original;

        return EchoHandler.getEvasionPosition(player, original);
    }

    @ModifyReturnValue(method = "getX()D", at = @At("RETURN"))
    private double modifyX(double original) {
        if (!((Object) this instanceof Player player) || !EchoHandler.shouldModifyPosition(player)) return original;

        return EchoHandler.getEvasionPosition((Player) (Object) this, new Vec3(original, position.y, position.z)).x;
    }

    @ModifyReturnValue(method = "getY()D", at = @At("RETURN"))
    private double modifyY(double original) {
        if (!((Object) this instanceof Player player) || !EchoHandler.shouldModifyPosition(player)) return original;

        return EchoHandler.getEvasionPosition((Player) (Object) this, new Vec3(position.x, original, position.z)).y;
    }

    @ModifyReturnValue(method = "getZ()D", at = @At("RETURN"))
    private double modifyZ(double original) {
        if (!((Object) this instanceof Player player) || !EchoHandler.shouldModifyPosition(player)) return original;

        return EchoHandler.getEvasionPosition((Player) (Object) this, new Vec3(position.x, position.y, original)).z;
    }

    @ModifyReturnValue(method = "getEyeY", at = @At("RETURN"))
    private double modifyEyeY(double original) {
        if (!((Object) this instanceof Player player) || !EchoHandler.shouldModifyPosition(player)) return original;

        return EchoHandler.getEvasionPosition((Player) (Object) this, position).y + ((Entity) (Object) this).getEyeHeight();
    }
}
