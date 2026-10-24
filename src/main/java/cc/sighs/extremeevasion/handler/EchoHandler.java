package cc.sighs.extremeevasion.handler;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;

public class EchoHandler {
    /**
     * Players whose evasion boxes may be queried during the current level tick.
     */
    public static final HashSet<Player> ACTIVE_PLAYERS = new HashSet<>();
    /**
     * Entity types and tags excluded from trajectory collision checks.
     */
    public static final TagObject<EntityType<?>> ENTITY_TYPE_BLACKLIST = new TagObject<>();
    public static boolean modifiesBoundingBox;

    public static void prepareLevelTick(Level level) {
        synchronized (ACTIVE_PLAYERS) {
            ACTIVE_PLAYERS.clear();
        }
        if (level.isClientSide()) {
            return;
        }

        for (Player player : level.players()) {
            if (ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player)) {
                synchronized (ACTIVE_PLAYERS) {
                    ACTIVE_PLAYERS.add(player);
                }
            }
        }
    }

    public static boolean shouldModify(Entity entity) {
        return !ENTITY_TYPE_BLACKLIST.is(entity.getType().builtInRegistryHolder())
                && !ACTIVE_PLAYERS.isEmpty();
    }

    public static void reloadEntityTypeBlacklist(List<? extends String> entries) {
        synchronized (ENTITY_TYPE_BLACKLIST) {
            ENTITY_TYPE_BLACKLIST.clear();
            for (String entry : entries) {
                ENTITY_TYPE_BLACKLIST.add(entry, Registries.ENTITY_TYPE);
            }
        }
    }
}
