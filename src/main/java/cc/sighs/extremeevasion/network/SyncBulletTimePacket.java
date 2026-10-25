package cc.sighs.extremeevasion.network;

import cc.sighs.extremeevasion.client.BulletTimeClientState;
import cc.sighs.extremeevasion.client.BulletTimeSoundPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncBulletTimePacket(long durationMillis, boolean globalClock) {
    public SyncBulletTimePacket(long durationMillis) {
        this(durationMillis, false);
    }

    static void encode(SyncBulletTimePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarLong(packet.durationMillis);
        buffer.writeBoolean(packet.globalClock);
    }

    static SyncBulletTimePacket decode(FriendlyByteBuf buffer) {
        return new SyncBulletTimePacket(buffer.readVarLong(), buffer.readBoolean());
    }

    static void handle(SyncBulletTimePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    if (packet.globalClock) {
                        cc.sighs.extremeevasion.BulletTimeController.triggerGlobal();
                    }
                    BulletTimeClientState.start(packet.durationMillis);
                    BulletTimeSoundPlayer.playTrigger();
                }
        ));
        context.get().setPacketHandled(true);
    }
}
