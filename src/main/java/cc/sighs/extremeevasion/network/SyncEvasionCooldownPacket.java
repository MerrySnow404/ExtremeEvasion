package cc.sighs.extremeevasion.network;

import cc.sighs.extremeevasion.ExtremeEvasion;
import cc.sighs.extremeevasion.client.EvasionCooldownClientState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncEvasionCooldownPacket(int cooldownTicks) implements CustomPacketPayload {
    public static final Type<SyncEvasionCooldownPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExtremeEvasion.MODID, "sync_evasion_cooldown")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEvasionCooldownPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncEvasionCooldownPacket::cooldownTicks,
            SyncEvasionCooldownPacket::new
    );

    public static void handle(SyncEvasionCooldownPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> EvasionCooldownClientState.start(packet.cooldownTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
