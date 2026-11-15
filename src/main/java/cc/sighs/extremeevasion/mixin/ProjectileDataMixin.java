package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.handler.ProjectileData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Projectile.class)
public abstract class ProjectileDataMixin implements ProjectileData {
    @Unique
    private Entity extremeEvasion$ignoreForEntity;

    @Override
    public Entity extremeEvasion$getIgnoreForEntity() {
        return extremeEvasion$ignoreForEntity;
    }

    @Override
    public void extremeEvasion$setIgnoreForEntity(Entity entity) {
        extremeEvasion$ignoreForEntity = entity;
    }
}
