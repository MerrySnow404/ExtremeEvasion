package cc.sighs.extremeevasion.compat.roll;

import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

final class EpicFightRollProviderInner {
    static boolean isRolling(ServerPlayer player) {
        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch == null) {
            return false;
        }

        ServerAnimator animator = patch.getServerAnimator();
        AnimationPlayer animationPlayer = animator.animationPlayer;
        if (animationPlayer == null || animationPlayer.isEnd()) {
            return false;
        }

        AssetAccessor<? extends StaticAnimation> realAnimation = animationPlayer.getRealAnimation();
        return realAnimation.get() instanceof DodgeAnimation || isRollOrStepAnimation(realAnimation);
    }

    private static boolean isRollOrStepAnimation(AssetAccessor<? extends StaticAnimation> animation) {
        String name = animation.registryName().toString();
        return name.contains("roll_forward")
                || name.contains("roll_backward")
                || name.contains("step_forward")
                || name.contains("step_backward")
                || name.contains("step_left")
                || name.contains("step_right");
    }
}
