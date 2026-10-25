package cc.sighs.extremeevasion.network;

import cc.sighs.extremeevasion.ExtremeEvasion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ExtremeEvasionNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int nextPacketId;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExtremeEvasion.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        CHANNEL.messageBuilder(SyncCounterAttackPacket.class, nextPacketId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncCounterAttackPacket::encode)
                .decoder(SyncCounterAttackPacket::decode)
                .consumerMainThread(SyncCounterAttackPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncBulletTimePacket.class, nextPacketId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncBulletTimePacket::encode)
                .decoder(SyncBulletTimePacket::decode)
                .consumerMainThread(SyncBulletTimePacket::handle)
                .add();
    }

    public static void sendCounterAttack(ServerPlayer player, boolean active) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCounterAttackPacket(active));
    }

    public static void sendBulletTime(ServerPlayer player, long durationMillis) {
        sendBulletTime(player, durationMillis, true);
    }

    public static void sendBulletTime(ServerPlayer player, long durationMillis, boolean globalClock) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncBulletTimePacket(durationMillis, globalClock));
    }

    public static void sendBulletTimeToAll(long durationMillis) {
        sendBulletTimeToAll(durationMillis, true);
    }

    public static void sendBulletTimeToAll(long durationMillis, boolean globalClock) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncBulletTimePacket(durationMillis, globalClock));
    }
}
