package cc.sighs.extremeevasion.handler;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.wrapper.WrapperNpcAPI;

public class NPCHandler {
    @SubscribeEvent
    public static void onNpcMeleeAttack(NpcEvent.MeleeAttackEvent event) {
        var target = event.target.getMCEntity();
        if (!(target instanceof Player player) || !ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player))
            return;

        var npc = event.npc.getMCEntity();
        var damageTypeHolder = npc.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NpcDamageSource.NPC);
        if (ExtremeEvasionEvents.tryTriggerExtremeEvasion((ServerPlayer) player, new DamageSource(damageTypeHolder, npc))) {
            event.setCanceled(true);
        }
    }

    public static void register() {
        WrapperNpcAPI.EVENT_BUS.register(NPCHandler.class);
    }
}
