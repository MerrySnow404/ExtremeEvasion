package cc.sighs.extremeevasion.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ExtremeEvasionNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(SyncCounterAttackPacket.TYPE, SyncCounterAttackPacket.STREAM_CODEC, SyncCounterAttackPacket::handle);
        registrar.playToClient(SyncBulletTimePacket.TYPE, SyncBulletTimePacket.STREAM_CODEC, SyncBulletTimePacket::handle);
    }

    public static void sendCounterAttack(ServerPlayer player, boolean active) {
        PacketDistributor.sendToPlayer(player, new SyncCounterAttackPacket(active));
    }

    public static void sendBulletTime(ServerPlayer player, long durationMillis) {
        sendBulletTime(player, durationMillis, true);
    }

    public static void sendBulletTime(ServerPlayer player, long durationMillis, boolean globalClock) {
        PacketDistributor.sendToPlayer(player, new SyncBulletTimePacket(durationMillis, globalClock));
    }

    public static void sendBulletTimeToAll(long durationMillis) {
        sendBulletTimeToAll(durationMillis, true);
    }

    public static void sendBulletTimeToAll(long durationMillis, boolean globalClock) {
        PacketDistributor.sendToAllPlayers(new SyncBulletTimePacket(durationMillis, globalClock));
    }
}
