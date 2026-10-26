package cc.sighs.extremeevasion.handler;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EchoHandler {
    /**
     * Entity types and tags excluded from trajectory collision checks.
     */
    public static final KeySet<EntityType<?>> ENTITY_TYPE_BLACKLIST = new KeySet<>(Registries.ENTITY_TYPE);
    /**
     * Per-thread scope used while a whitelisted entity is ticking.
     */
    public static final ThreadLocal<Boolean> modifiesBoundingBoxAndPos = ThreadLocal.withInitial(() -> false);
    public static Entity currentEntity;

    public static boolean shouldModifyFor(Entity entity) {
        return !ENTITY_TYPE_BLACKLIST.contains(entity.getType());
    }

    public static boolean shouldModifyBoundingBox(Object object) {
        return EchoHandler.modifiesBoundingBoxAndPos.get() && object instanceof Player player && ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player);
    }

    public static Vec3 getEvasionPosition(Player player, Vec3 original) {
        if (!shouldModifyPosition(player)) {
            return original;
        }

        AABB startAABB = ExtremeEvasionEvents.getExtremeEvasionStartAABB(player);
        Vec3 start = new Vec3(
                (startAABB.minX + startAABB.maxX) * 0.5D,
                startAABB.minY,
                (startAABB.minZ + startAABB.maxZ) * 0.5D
        );
        Vec3 target = EchoHandler.currentEntity.position();
        Vec3 movement = original.subtract(start);
        double movementLengthSqr = movement.lengthSqr();
        if (movementLengthSqr <= 1.0E-12D) {
            return start;
        }

        double segmentTime = target.subtract(start).dot(movement) / movementLengthSqr;
        segmentTime = Math.max(0.0D, Math.min(1.0D, segmentTime));
        return start.add(movement.scale(segmentTime));
    }

    public static boolean shouldModifyPosition(Player player) {
        return EchoHandler.modifiesBoundingBoxAndPos.get()
                && EchoHandler.currentEntity != null
                && ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player);
    }

    public static void reloadEntityTypeBlacklist(List<? extends String> entries) {
        ENTITY_TYPE_BLACKLIST.updateKeys(entries);
    }
}
