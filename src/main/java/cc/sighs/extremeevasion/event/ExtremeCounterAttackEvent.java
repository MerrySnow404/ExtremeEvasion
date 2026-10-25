package cc.sighs.extremeevasion.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ExtremeCounterAttackEvent extends Event {
    private final ServerPlayer player;
    private final Entity target;
    private final int remainingChargesBeforeConsume;

    public ExtremeCounterAttackEvent(ServerPlayer player, Entity target, int remainingChargesBeforeConsume) {
        this.player = player;
        this.target = target;
        this.remainingChargesBeforeConsume = remainingChargesBeforeConsume;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Entity getTarget() {
        return target;
    }

    public int getRemainingChargesBeforeConsume() {
        return remainingChargesBeforeConsume;
    }
}
