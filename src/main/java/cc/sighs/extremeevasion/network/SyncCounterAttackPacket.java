package cc.sighs.extremeevasion.network;

import cc.sighs.extremeevasion.client.CounterAttackClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncCounterAttackPacket(boolean active) {
    static void encode(SyncCounterAttackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
    }

    static SyncCounterAttackPacket decode(FriendlyByteBuf buffer) {
        return new SyncCounterAttackPacket(buffer.readBoolean());
    }

    static void handle(SyncCounterAttackPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> CounterAttackClientState.setActive(packet.active)
        ));
        context.get().setPacketHandled(true);
    }
}
