package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import cc.sighs.extremeevasion.handler.EchoHandler;
import cc.sighs.extremeevasion.handler.TrajectoryAABB;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyReturnValue(method = "getBoundingBox", at = @At("RETURN"))
    private AABB modifyBoundingBox(AABB original) {
        if (!EchoHandler.modifiesBoundingBox || !EchoHandler.ACTIVE_PLAYERS.contains(this))
            return original;

        var player = (Player) (Object) this;
        return new TrajectoryAABB(ExtremeEvasionEvents.getExtremeEvasionStartAABB(player), original);
    }
}
