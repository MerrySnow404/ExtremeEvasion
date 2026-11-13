package cc.sighs.extremeevasion;

import cc.sighs.extremeevasion.compat.roll.RollCompat;
import cc.sighs.extremeevasion.event.ExtremeCounterAttackEvent;
import cc.sighs.extremeevasion.event.ExtremeEvasionTriggeredEvent;
import cc.sighs.extremeevasion.handler.EvasionData;
import cc.sighs.extremeevasion.handler.TaCZHandler;
import cc.sighs.extremeevasion.network.ExtremeEvasionNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ExtremeEvasionEvents {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        EvasionData data = getData(player);
        boolean rolling = RollCompat.isRolling(player);
        boolean active;
        sanitizeLegacyState(player, data);
        active = data.extremeEvasion$isWindowActive();
        decrementRemainingTicks(data);
        clearExpiredCounterAttack(player, data);
        clearExpiredActiveCounterAttack(player, data);
        clearExpiredBulletTimeInvulnerability(data);
        consumeBlocked(player);

        int remainingTicks = data.extremeEvasion$getEvasionCooldownTicksRemaining();
        if (remainingTicks > 0) {
            remainingTicks -= 1;
            data.extremeEvasion$setEvasionCooldownTicksRemaining(remainingTicks);
            if (remainingTicks == 0
                    && player instanceof ServerPlayer serverPlayer) {
                ExtremeEvasionNetwork.sendEvasionCooldown(serverPlayer, 0);
            }
        } else if (remainingTicks == -1) {
            data.extremeEvasion$setEvasionCooldownTicksRemaining(0);
            if (player instanceof ServerPlayer serverPlayer) {
                ExtremeEvasionNetwork.sendEvasionCooldown(serverPlayer, 0);
            }
        }

        if (!rolling) {
            data.extremeEvasion$setRollConsumed(false);
        }

        if (data.extremeEvasion$isRollConsumed()) {
            if (active) {
                endWindow(data);
            }
            return;
        }

        if (rolling && !active) {
            startWindow(player, data);
            return;
        }

        if (!active) {
            return;
        }

        if (!isExtremeEvasionWindowOpen(player, data) || data.extremeEvasion$isWindowSuccess()) {
            endWindow(data);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingPreIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        sanitizeLegacyState(player, getData(player));
        consumeBlocked(player);
        if (hasActiveExtremeEvasionWindow(player)
                && isTriggerableSource(event.getSource())
                && tryTriggerExtremeEvasion(player, event.getSource())) {
            setBlocked(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingPostIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (isBulletTimeInvulnerable(player) || consumeBlocked(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        var rayTraceResult = event.getRayTraceResult();
        if (!(rayTraceResult instanceof EntityHitResult entityHitResult)) return;

        var entity = entityHitResult.getEntity();
        if (!(entity instanceof Player player) || !hasActiveExtremeEvasionWindow(player)) return;

        var projectile = event.getProjectile();
        var owner = projectile.getOwner();
        var living = owner instanceof LivingEntity ? (LivingEntity) owner : null;
        if (tryTriggerExtremeEvasion((ServerPlayer) player, projectile.damageSources().mobProjectile(projectile, living))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()) {
            return;
        }

        EvasionData data = getData(player);
        clearActiveCounterAttack(data);
        if (!hasCounterAttack(player, data)) {
            return;
        }

        ExtremeCounterAttackEvent counterAttackEvent = new ExtremeCounterAttackEvent(
                serverPlayer,
                event.getTarget(),
                data.extremeEvasion$getCounterAttackCharges()
        );
        NeoForge.EVENT_BUS.post(counterAttackEvent);
        if (counterAttackEvent.isCanceled()) {
            return;
        }

        data.extremeEvasion$setCounterAttackActive(true);
        data.extremeEvasion$setCounterAttackActiveTick(true);
        data.extremeEvasion$setCounterAttackTargetUuid(event.getTarget().getUUID());
        consumeCounterAttack(data);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        EvasionData data = getData(player);
        if (!data.extremeEvasion$isCounterAttackActive()) {
            return;
        }

        if (Config.enableExtremeCounterAttackCritical) {
            event.setCriticalHit(true);
            event.setDamageMultiplier(Math.max(event.getDamageMultiplier(), 1.5F));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCounterAttackDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        EvasionData data = getData(player);
        boolean activatedByDamageType = false;
        if (!data.extremeEvasion$isCounterAttackActive()
                && hasCounterAttack(player, data)
                && Config.EXTREME_COUNTER_ATTACK_DAMAGE_TYPES.contains(event.getSource().type())) {
            data.extremeEvasion$setCounterAttackActive(true);
            data.extremeEvasion$setCounterAttackActiveTick(true);
            data.extremeEvasion$setCounterAttackTargetUuid(event.getEntity().getUUID());
            consumeCounterAttack(data);
            activatedByDamageType = true;
        }
        if (!data.extremeEvasion$isCounterAttackActive() || !isActiveCounterAttackTarget(event.getEntity(), data)) {
            return;
        }

        if (activatedByDamageType && Config.enableExtremeCounterAttackCritical) {
            event.setNewDamage(event.getNewDamage() * 1.5F);
            player.crit(event.getEntity());
        }
        if (Config.enableExtremeCounterAttackArmorPiercing) {
            event.setNewDamage(getArmorPiercingHurtAmount(event.getEntity(), event.getSource(), event.getNewDamage()));
        }
        clearActiveCounterAttack(data);
        if (!hasCounterAttack(player, data)) {
            ExtremeEvasionNetwork.sendCounterAttack(player, false);
        }
    }

    private static boolean isTriggerableSource(DamageSource source) {
        return isMobMeleeAttack(source) || isProjectileAttack(source) || isBulletAttack(source);
    }

    private static boolean isMobMeleeAttack(DamageSource source) {
        return source.getEntity() instanceof Mob attacker && (source.getDirectEntity() == null || source.getDirectEntity() == attacker);
    }

    private static boolean isProjectileAttack(DamageSource source) {
        return source.is(DamageTypeTags.IS_PROJECTILE);
    }

    private static boolean isBulletAttack(DamageSource source) {
        return ModList.get().isLoaded("tacz") && source.is(TaCZHandler.BULLETS_TAG);
    }

    private static void startWindow(Player player, EvasionData data) {
        if (!(player instanceof ServerPlayer) || isExtremeEvasionOnCooldown(player, data)) {
            return;
        }

        var aabb = player.getBoundingBox();
        data.extremeEvasion$setWindowActive(true);
        data.extremeEvasion$setWindowSuccess(false);
        data.extremeEvasion$setStartMinX(aabb.minX);
        data.extremeEvasion$setStartMinY(aabb.minY);
        data.extremeEvasion$setStartMinZ(aabb.minZ);
        data.extremeEvasion$setStartMaxX(aabb.maxX);
        data.extremeEvasion$setStartMaxY(aabb.maxY);
        data.extremeEvasion$setStartMaxZ(aabb.maxZ);
        data.extremeEvasion$setMinEndTicks(Config.minimumExtremeEvasionWindowTicks);
        data.extremeEvasion$setMaxEndTicks(Config.maximumExtremeEvasionWindowTicks);
    }

    private static void endWindow(EvasionData data) {
        clearWindow(data);
    }

    private static void clearWindow(EvasionData data) {
        data.extremeEvasion$setWindowActive(false);
        data.extremeEvasion$setWindowSuccess(false);
        data.extremeEvasion$setMinEndTicks(0);
        data.extremeEvasion$setMaxEndTicks(0);
    }

    private static void setBlocked(Player player) {
        var data = getData(player);
        data.extremeEvasion$setWindowBlocked(true);
    }

    private static boolean consumeBlocked(Player player) {
        var data = getData(player);
        if (!data.extremeEvasion$isWindowBlocked()) return false;

        data.extremeEvasion$setWindowBlocked(false);
        return true;
    }

    private static EvasionData getData(Player player) {
        return (EvasionData) player;
    }

    public static boolean hasActiveExtremeEvasionWindow(Player player) {
        EvasionData data = getData(player);
        return isWindowArmed(data) && isExtremeEvasionWindowOpen(player, data);
    }

    public static void startExternalRollWindow(Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        EvasionData data = getData(player);
        sanitizeLegacyState(player, data);
        if (data.extremeEvasion$isRollConsumed() || data.extremeEvasion$isWindowActive()) {
            return;
        }

        startWindow(player, data);
    }

    public static AABB getExtremeEvasionStartAABB(Player player) {
        EvasionData data = getData(player);
        return new AABB(
                data.extremeEvasion$getStartMinX(), data.extremeEvasion$getStartMinY(), data.extremeEvasion$getStartMinZ(),
                data.extremeEvasion$getStartMaxX(), data.extremeEvasion$getStartMaxY(), data.extremeEvasion$getStartMaxZ()
        );
    }

    public static boolean tryTriggerExtremeEvasion(ServerPlayer player, DamageSource damageSource) {
        EvasionData data = getData(player);
        if (isExtremeEvasionOnCooldown(player, data)
                || !isWindowArmed(data)
                || !isExtremeEvasionWindowOpen(player, data)
                || protectsPlayer(player, damageSource)) {
            return false;
        }

        boolean triggerBulletTime = canTriggerBulletTime(player);
        ExtremeEvasionTriggeredEvent evasionEvent = new ExtremeEvasionTriggeredEvent(player, damageSource, triggerBulletTime);
        NeoForge.EVENT_BUS.post(evasionEvent);
        if (evasionEvent.isCanceled()) {
            return false;
        }

        clearWindow(data);
        data.extremeEvasion$setRollConsumed(true);
        if (Config.extremeEvasionCooldownTicks > 0) {
            data.extremeEvasion$setEvasionCooldownTicksRemaining(Config.extremeEvasionCooldownTicks);
            ExtremeEvasionNetwork.sendEvasionCooldown(player, Config.extremeEvasionCooldownTicks);
        }
        grantCounterAttack(player, data);
        if (triggerBulletTime) {
            boolean usingTimeScaleLib = BulletTimeController.trigger();
            grantBulletTimeInvulnerability(data);
            if (player.getServer() != null
                    && (!player.getServer().isSingleplayer() || player.getServer().isPublished())) {
                ExtremeEvasionNetwork.sendBulletTimeToAll(Config.bulletTimeDurationMillis, !usingTimeScaleLib);
            } else {
                ExtremeEvasionNetwork.sendBulletTime(player, Config.bulletTimeDurationMillis, !usingTimeScaleLib);
            }
        }
        return true;
    }

    private static boolean protectsPlayer(ServerPlayer player, DamageSource damageSource) {
        if (damageSource.getEntity() != player) return false;

        setBlocked(player);
        return true;
    }

    private static boolean isExtremeEvasionOnCooldown(Player player, EvasionData data) {
        return data.extremeEvasion$getEvasionCooldownTicksRemaining() > 0;
    }

    private static boolean canTriggerBulletTime(ServerPlayer player) {
        return Config.enableBulletTime
                && player.getServer() != null
                && (Config.enableBulletTimeInMultiplayer
                || (player.getServer().isSingleplayer() && !player.getServer().isPublished()));
    }

    private static void grantBulletTimeInvulnerability(EvasionData data) {
        if (!Config.enableBulletTimeInvulnerability || Config.bulletTimeDurationMillis <= 0) {
            data.extremeEvasion$setBulletTimeInvulnerableUntil(0);
            return;
        }

        data.extremeEvasion$setBulletTimeInvulnerableUntil(BulletTimeController.monotonicMillis() + Config.bulletTimeDurationMillis);
    }

    private static boolean isBulletTimeInvulnerable(ServerPlayer player) {
        if (!Config.enableBulletTimeInvulnerability) {
            return false;
        }

        EvasionData data = getData(player);
        if (data.extremeEvasion$getBulletTimeInvulnerableUntil() <= 0) {
            return false;
        }

        if (BulletTimeController.monotonicMillis() <= data.extremeEvasion$getBulletTimeInvulnerableUntil()) {
            return true;
        }

        data.extremeEvasion$setBulletTimeInvulnerableUntil(0);
        return false;
    }

    private static void clearExpiredBulletTimeInvulnerability(EvasionData data) {
        if (data.extremeEvasion$getBulletTimeInvulnerableUntil() > 0
                && BulletTimeController.monotonicMillis() > data.extremeEvasion$getBulletTimeInvulnerableUntil()) {
            data.extremeEvasion$setBulletTimeInvulnerableUntil(0);
        }
    }

    private static boolean isWindowArmed(EvasionData data) {
        return data.extremeEvasion$isWindowActive() && !data.extremeEvasion$isWindowSuccess();
    }

    private static void sanitizeLegacyState(Player player, EvasionData data) {
        if (data.extremeEvasion$isWindowActive()
                && (data.extremeEvasion$getMinEndTicks() <= 0
                || data.extremeEvasion$getMaxEndTicks() <= 0
                || !isExtremeEvasionWindowOpen(player, data))) {
            clearWindow(data);
        }

        if (data.extremeEvasion$getBulletTimeInvulnerableUntil() <= 0) {
            return;
        }

        long now = BulletTimeController.monotonicMillis();
        long invulnerableUntil = data.extremeEvasion$getBulletTimeInvulnerableUntil();
        long maxExpectedRemainingMillis = Math.max(Config.bulletTimeDurationMillis + 1000L, 5000L);
        if (invulnerableUntil <= now || invulnerableUntil > now + maxExpectedRemainingMillis) {
            data.extremeEvasion$setBulletTimeInvulnerableUntil(0);
        }
    }

    private static boolean isMinimumWindowActive(Player player, EvasionData data) {
        return data.extremeEvasion$getMinEndTicks() > 0;
    }

    private static boolean isMaximumWindowActive(Player player, EvasionData data) {
        return data.extremeEvasion$getMaxEndTicks() > 0;
    }

    private static boolean isExtremeEvasionWindowOpen(Player player, EvasionData data) {
        return isMaximumWindowActive(player, data)
                && (isMinimumWindowActive(player, data) || RollCompat.isRolling(player));
    }

    private static void grantCounterAttack(ServerPlayer player, EvasionData data) {
        int windowTicks = Config.extremeCounterAttackWindowTicks;
        int charges = Config.extremeCounterAttackCharges;
        if (windowTicks <= 0 || charges <= 0) {
            clearCounterAttack(data);
            ExtremeEvasionNetwork.sendCounterAttack(player, false);
            return;
        }

        data.extremeEvasion$setCounterAttackEndTicks(windowTicks);
        data.extremeEvasion$setCounterAttackCharges(charges);
        clearActiveCounterAttack(data);
        ExtremeEvasionNetwork.sendCounterAttack(player, true);
    }

    public static boolean hasCounterAttackBoost(Player player) {
        return hasCounterAttack(player, getData(player));
    }

    private static boolean hasCounterAttack(Player player, EvasionData data) {
        return data.extremeEvasion$getCounterAttackEndTicks() > 0 && data.extremeEvasion$getCounterAttackCharges() > 0;
    }

    private static void clearExpiredCounterAttack(Player player, EvasionData data) {
        if (data.extremeEvasion$getCounterAttackEndTicks() <= 0 && data.extremeEvasion$getCounterAttackCharges() > 0) {
            clearCounterAttack(data);
            if (player instanceof ServerPlayer serverPlayer) {
                ExtremeEvasionNetwork.sendCounterAttack(serverPlayer, false);
            }
        }
    }

    private static void clearExpiredActiveCounterAttack(Player player, EvasionData data) {
        if (!data.extremeEvasion$isCounterAttackActive() || data.extremeEvasion$isCounterAttackActiveTick()) {
            clearActiveCounterAttack(data);
            if (!hasCounterAttack(player, data) && player instanceof ServerPlayer serverPlayer) {
                ExtremeEvasionNetwork.sendCounterAttack(serverPlayer, false);
            }
        }
    }

    private static void consumeCounterAttack(EvasionData data) {
        int remainingCharges = data.extremeEvasion$getCounterAttackCharges() - 1;
        if (remainingCharges <= 0) {
            data.extremeEvasion$setCounterAttackEndTicks(0);
            data.extremeEvasion$setCounterAttackCharges(0);
        } else {
            data.extremeEvasion$setCounterAttackCharges(remainingCharges);
        }
    }

    private static boolean isActiveCounterAttackTarget(Entity target, EvasionData data) {
        return data.extremeEvasion$getCounterAttackTargetUuid() != null
                && target.getUUID().equals(data.extremeEvasion$getCounterAttackTargetUuid());
    }

    private static void clearCounterAttack(EvasionData data) {
        data.extremeEvasion$setCounterAttackEndTicks(0);
        data.extremeEvasion$setCounterAttackCharges(0);
        clearActiveCounterAttack(data);
    }

    private static void clearActiveCounterAttack(EvasionData data) {
        data.extremeEvasion$setCounterAttackActive(false);
        data.extremeEvasion$setCounterAttackActiveTick(false);
        data.extremeEvasion$setCounterAttackTargetUuid(null);
    }

    private static void decrementRemainingTicks(EvasionData data) {
        if (data.extremeEvasion$getMinEndTicks() > 0)
            data.extremeEvasion$setMinEndTicks(data.extremeEvasion$getMinEndTicks() - 1);
        if (data.extremeEvasion$getMaxEndTicks() > 0)
            data.extremeEvasion$setMaxEndTicks(data.extremeEvasion$getMaxEndTicks() - 1);
        if (data.extremeEvasion$getCounterAttackEndTicks() > 0)
            data.extremeEvasion$setCounterAttackEndTicks(data.extremeEvasion$getCounterAttackEndTicks() - 1);
    }

    private static float getArmorPiercingHurtAmount(LivingEntity target, DamageSource damageSource, float desiredAfterArmor) {
        if (desiredAfterArmor <= 0.0F) {
            return desiredAfterArmor;
        }

        float armor = (float) target.getArmorValue();
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        if (armor <= 0.0F) {
            return desiredAfterArmor;
        }

        float low = desiredAfterArmor;
        float high = desiredAfterArmor;
        while (CombatRules.getDamageAfterAbsorb(target, high, damageSource, armor, toughness) < desiredAfterArmor && high < desiredAfterArmor * 100.0F) {
            high *= 2.0F;
        }

        for (int i = 0; i < 16; i++) {
            float mid = (low + high) * 0.5F;
            if (CombatRules.getDamageAfterAbsorb(target, mid, damageSource, armor, toughness) < desiredAfterArmor) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return high;
    }
}
