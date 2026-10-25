package cc.sighs.extremeevasion.handler;

import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * TaCZ integration through its stable damage-type tag; no hard dependency is required.
 */
public final class TaCZHandler {
    public static final TagKey<DamageType> BULLETS_TAG = TagKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("tacz", "bullets"));

    @SubscribeEvent
    public static void onEntityHurtByGunEvent(EntityHurtByGunEvent.Pre event) {
        var hurtEntity = event.getHurtEntity();
        if (!(hurtEntity instanceof Player player) || !ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player))
            return;

        ExtremeEvasionEvents.tryTriggerExtremeEvasion((ServerPlayer) player, event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING));
    }
}
