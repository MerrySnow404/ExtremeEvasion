package cc.sighs.extremeevasion.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ExtremeEvasionTriggeredEvent extends Event {
    private final ServerPlayer player;
    private final DamageSource damageSource;
    private final boolean bulletTimeTriggered;

    public ExtremeEvasionTriggeredEvent(ServerPlayer player, DamageSource damageSource, boolean bulletTimeTriggered) {
        this.player = player;
        this.damageSource = damageSource;
        this.bulletTimeTriggered = bulletTimeTriggered;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public boolean willTriggerBulletTime() {
        return bulletTimeTriggered;
    }
}
