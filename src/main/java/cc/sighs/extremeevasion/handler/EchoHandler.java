package cc.sighs.extremeevasion.handler;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;

public final class EchoHandler {
    public static final HashSet<Player> ACTIVE_PLAYERS = new HashSet<>();
    public static final TagObject<EntityType<?>> ENTITY_TYPE_BLACKLIST = new TagObject<>();
    public static final ThreadLocal<Boolean> modifiesBoundingBoxAndPos = ThreadLocal.withInitial(() -> false);
    public static Entity currentEntity;

    public static void prepareLevelTick(Level level) {
        ACTIVE_PLAYERS.clear();
        if (level.isClientSide()) return;
        for (Player player : level.players()) {
            if (ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player)) ACTIVE_PLAYERS.add(player);
        }
    }

    public static boolean shouldModifyFor(Entity entity) {
        return !ENTITY_TYPE_BLACKLIST.is(entity.getType().builtInRegistryHolder()) && !ACTIVE_PLAYERS.isEmpty();
    }

    public static boolean shouldModifyBoundingBox(Object object) {
        return modifiesBoundingBoxAndPos.get() && ACTIVE_PLAYERS.contains(object);
    }

    public static Vec3 getEvasionPosition(Player player, Vec3 original) {
        if (!shouldModifyPosition(player)) return original;
        AABB startAABB = ExtremeEvasionEvents.getExtremeEvasionStartAABB(player);
        Vec3 start = new Vec3((startAABB.minX + startAABB.maxX) * 0.5D, startAABB.minY, (startAABB.minZ + startAABB.maxZ) * 0.5D);
        Vec3 target = currentEntity.position();
        Vec3 movement = original.subtract(start);
        double movementLengthSqr = movement.lengthSqr();
        if (movementLengthSqr <= 1.0E-12D) return start;
        double segmentTime = target.subtract(start).dot(movement) / movementLengthSqr;
        segmentTime = Math.clamp(segmentTime, 0.0D, 1.0D);
        return start.add(movement.scale(segmentTime));
    }

    public static boolean shouldModifyPosition(Player player) {
        return modifiesBoundingBoxAndPos.get() && currentEntity != null && ACTIVE_PLAYERS.contains(player);
    }

    public static void reloadEntityTypeBlacklist(List<? extends String> entries) {
        ENTITY_TYPE_BLACKLIST.clear();
        for (String entry : entries) ENTITY_TYPE_BLACKLIST.add(entry, Registries.ENTITY_TYPE);
    }
}
