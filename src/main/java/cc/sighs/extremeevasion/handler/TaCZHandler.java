package cc.sighs.extremeevasion.handler;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * TaCZ integration through its stable damage-type tag; no hard dependency is required.
 */
public final class TaCZHandler {
    public static final TagKey<DamageType> BULLETS_TAG = TagKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("tacz", "bullets"));
}
