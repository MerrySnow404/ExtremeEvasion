package cc.sighs.extremeevasion.network;

import cc.sighs.extremeevasion.client.EvasionCooldownClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncEvasionCooldownPacket(int cooldownTicks) {
    static void encode(SyncEvasionCooldownPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.cooldownTicks);
    }

    static SyncEvasionCooldownPacket decode(FriendlyByteBuf buffer) {
        return new SyncEvasionCooldownPacket(buffer.readVarInt());
    }

    static void handle(SyncEvasionCooldownPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> EvasionCooldownClientState.start(packet.cooldownTicks)
        ));
        context.get().setPacketHandled(true);
    }
}
