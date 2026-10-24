package cc.sighs.extremeevasion;

import cc.sighs.extremeevasion.compat.roll.RollCompat;
import cc.sighs.extremeevasion.event.ExtremeCounterAttackEvent;
import cc.sighs.extremeevasion.event.ExtremeEvasionTriggeredEvent;
import cc.sighs.extremeevasion.handler.TaCZHandler;
import cc.sighs.extremeevasion.network.ExtremeEvasionNetwork;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExtremeEvasion.MODID)
public final class ExtremeEvasionEvents {
    private static final String DATA_ROOT = ExtremeEvasion.MODID;
    private static final String DATA_ACTIVE = "extreme_window_active";
    private static final String DATA_SUCCESS = "extreme_window_success";
    private static final String DATA_START_MIN_X = "extreme_window_start_min_x";
    private static final String DATA_START_MIN_Y = "extreme_window_start_min_y";
    private static final String DATA_START_MIN_Z = "extreme_window_start_min_z";
    private static final String DATA_START_MAX_X = "extreme_window_start_max_x";
    private static final String DATA_START_MAX_Y = "extreme_window_start_max_y";
    private static final String DATA_START_MAX_Z = "extreme_window_start_max_z";
    private static final String DATA_MIN_END_TICK = "extreme_window_min_end_tick";
    private static final String DATA_MAX_END_TICK = "extreme_window_max_end_tick";
    private static final String DATA_ROLL_CONSUMED = "extreme_roll_consumed";
    private static final String DATA_COUNTER_ATTACK_END_TICK = "extreme_counter_attack_end_tick";
    private static final String DATA_COUNTER_ATTACK_ACTIVE = "extreme_counter_attack_active";
    private static final String DATA_COUNTER_ATTACK_ACTIVE_TICK = "extreme_counter_attack_active_tick";
    private static final String DATA_COUNTER_ATTACK_TARGET_UUID = "extreme_counter_attack_target_uuid";
    private static final String DATA_COUNTER_ATTACK_CHARGES = "extreme_counter_attack_charges";
    private static final String DATA_BULLET_TIME_INVULNERABLE_UNTIL = "bullet_time_invulnerable_until";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }

        Player player = event.player;
        CompoundTag data = getData(player);
        boolean rolling = RollCompat.isRolling(player);
        boolean active;
        sanitizeLegacyState(player, data);
        active = data.getBoolean(DATA_ACTIVE);
        clearExpiredCounterAttack(player, data);
        clearExpiredActiveCounterAttack(player, data);
        clearExpiredBulletTimeInvulnerability(data);

        if (!rolling) {
            data.remove(DATA_ROLL_CONSUMED);
        }

        if (data.getBoolean(DATA_ROLL_CONSUMED)) {
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

        if (!isExtremeEvasionWindowOpen(player, data) || data.getBoolean(DATA_SUCCESS)) {
            endWindow(data);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        sanitizeLegacyState(player, getData(player));
        if (hasActiveExtremeEvasionWindow(player) && isTriggerableSource(event.getSource()) && tryTriggerExtremeEvasion(player, event.getSource())) {
            event.setCanceled(true);
            return;
        }

        if (isBulletTimeInvulnerable(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        sanitizeLegacyState(player, getData(player));
        if (hasActiveExtremeEvasionWindow(player) && isTriggerableSource(event.getSource()) && tryTriggerExtremeEvasion(player, event.getSource())) {
            event.setAmount(0.0F);
            event.setCanceled(true);
            return;
        }

        if (isBulletTimeInvulnerable(player)) {
            event.setAmount(0.0F);
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
        tryTriggerExtremeEvasion((ServerPlayer) player, projectile.damageSources().mobProjectile(projectile, null));
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()) {
            return;
        }

        CompoundTag data = getData(player);
        clearActiveCounterAttack(data);
        if (!hasCounterAttack(player, data)) {
            return;
        }

        ExtremeCounterAttackEvent counterAttackEvent = new ExtremeCounterAttackEvent(
                serverPlayer,
                event.getTarget(),
                data.getInt(DATA_COUNTER_ATTACK_CHARGES)
        );
        if (MinecraftForge.EVENT_BUS.post(counterAttackEvent)) {
            return;
        }

        data.putBoolean(DATA_COUNTER_ATTACK_ACTIVE, true);
        data.putLong(DATA_COUNTER_ATTACK_ACTIVE_TICK, player.level().getGameTime());
        data.putUUID(DATA_COUNTER_ATTACK_TARGET_UUID, event.getTarget().getUUID());
        consumeCounterAttack(data);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        CompoundTag data = getData(player);
        if (!data.getBoolean(DATA_COUNTER_ATTACK_ACTIVE)) {
            return;
        }

        if (Config.enableExtremeCounterAttackCritical) {
            event.setDamageModifier(Math.max(event.getDamageModifier(), 1.5F));
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCounterAttackHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = getData(player);
        if (!data.getBoolean(DATA_COUNTER_ATTACK_ACTIVE) || !isActiveCounterAttackTarget(event.getEntity(), data)) {
            return;
        }

        if (Config.enableExtremeCounterAttackArmorPiercing) {
            event.setAmount(getArmorPiercingHurtAmount(event.getEntity(), event.getAmount()));
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
        return source.getEntity() instanceof Mob attacker
                && (source.getDirectEntity() == null || source.getDirectEntity() == attacker);
    }

    private static boolean isProjectileAttack(DamageSource source) {
        return source.is(DamageTypeTags.IS_PROJECTILE);
    }

    private static boolean isBulletAttack(DamageSource source) {
        return ModList.get().isLoaded("tacz") && source.is(TaCZHandler.BULLETS_TAG);
    }

    private static void startWindow(Player player, CompoundTag data) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }

        var aabb = player.getBoundingBox();
        data.putBoolean(DATA_ACTIVE, true);
        data.putBoolean(DATA_SUCCESS, false);
        data.putDouble(DATA_START_MIN_X, aabb.minX);
        data.putDouble(DATA_START_MIN_Y, aabb.minY);
        data.putDouble(DATA_START_MIN_Z, aabb.minZ);
        data.putDouble(DATA_START_MAX_X, aabb.maxX);
        data.putDouble(DATA_START_MAX_Y, aabb.maxY);
        data.putDouble(DATA_START_MAX_Z, aabb.maxZ);
        data.putLong(DATA_MIN_END_TICK, player.level().getGameTime() + Config.minimumExtremeEvasionWindowTicks);
        data.putLong(DATA_MAX_END_TICK, player.level().getGameTime() + Config.maximumExtremeEvasionWindowTicks);
    }

    private static void endWindow(CompoundTag data) {
        clearWindow(data);
    }

    private static void clearWindow(CompoundTag data) {
        data.remove(DATA_ACTIVE);
        data.remove(DATA_SUCCESS);
        data.remove(DATA_START_MIN_X);
        data.remove(DATA_START_MIN_Y);
        data.remove(DATA_START_MIN_Z);
        data.remove(DATA_START_MAX_X);
        data.remove(DATA_START_MAX_Y);
        data.remove(DATA_START_MAX_Z);
        data.remove(DATA_MIN_END_TICK);
        data.remove(DATA_MAX_END_TICK);
    }

    private static CompoundTag getData(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(DATA_ROOT, CompoundTag.TAG_COMPOUND)) {
            persistentData.put(DATA_ROOT, new CompoundTag());
        }
        return persistentData.getCompound(DATA_ROOT);
    }

    public static boolean hasActiveExtremeEvasionWindow(Player player) {
        CompoundTag data = getData(player);
        return isWindowArmed(data) && isExtremeEvasionWindowOpen(player, data);
    }

    public static void startExternalRollWindow(Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        CompoundTag data = getData(player);
        sanitizeLegacyState(player, data);
        if (data.getBoolean(DATA_ROLL_CONSUMED) || data.getBoolean(DATA_ACTIVE)) {
            return;
        }

        startWindow(player, data);
    }

    public static AABB getExtremeEvasionStartAABB(Player player) {
        CompoundTag data = getData(player);
        return new AABB(
                data.getDouble(DATA_START_MIN_X),
                data.getDouble(DATA_START_MIN_Y),
                data.getDouble(DATA_START_MIN_Z),
                data.getDouble(DATA_START_MAX_X),
                data.getDouble(DATA_START_MAX_Y),
                data.getDouble(DATA_START_MAX_Z)
        );
    }

    public static boolean tryTriggerExtremeEvasion(ServerPlayer player, DamageSource damageSource) {
        CompoundTag data = getData(player);
        if (!isWindowArmed(data) || !isExtremeEvasionWindowOpen(player, data)) {
            return false;
        }

        boolean triggerBulletTime = canTriggerBulletTime(player);
        ExtremeEvasionTriggeredEvent evasionEvent = new ExtremeEvasionTriggeredEvent(player, damageSource, triggerBulletTime);
        if (MinecraftForge.EVENT_BUS.post(evasionEvent)) {
            return false;
        }

        clearWindow(data);
        data.putBoolean(DATA_ROLL_CONSUMED, true);
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

    private static boolean canTriggerBulletTime(ServerPlayer player) {
        return Config.enableBulletTime
                && player.getServer() != null
                && (Config.enableBulletTimeInMultiplayer
                || (player.getServer().isSingleplayer() && !player.getServer().isPublished()));
    }

    private static void grantBulletTimeInvulnerability(CompoundTag data) {
        if (!Config.enableBulletTimeInvulnerability || Config.bulletTimeDurationMillis <= 0) {
            data.remove(DATA_BULLET_TIME_INVULNERABLE_UNTIL);
            return;
        }

        data.putLong(
                DATA_BULLET_TIME_INVULNERABLE_UNTIL,
                BulletTimeController.monotonicMillis() + Config.bulletTimeDurationMillis
        );
    }

    private static boolean isBulletTimeInvulnerable(ServerPlayer player) {
        if (!Config.enableBulletTimeInvulnerability) {
            return false;
        }

        CompoundTag data = getData(player);
        if (!data.contains(DATA_BULLET_TIME_INVULNERABLE_UNTIL)) {
            return false;
        }

        if (BulletTimeController.monotonicMillis() <= data.getLong(DATA_BULLET_TIME_INVULNERABLE_UNTIL)) {
            return true;
        }

        data.remove(DATA_BULLET_TIME_INVULNERABLE_UNTIL);
        return false;
    }

    private static void clearExpiredBulletTimeInvulnerability(CompoundTag data) {
        if (data.contains(DATA_BULLET_TIME_INVULNERABLE_UNTIL)
                && BulletTimeController.monotonicMillis() > data.getLong(DATA_BULLET_TIME_INVULNERABLE_UNTIL)) {
            data.remove(DATA_BULLET_TIME_INVULNERABLE_UNTIL);
        }
    }

    private static boolean isWindowArmed(CompoundTag data) {
        return data.getBoolean(DATA_ACTIVE) && !data.getBoolean(DATA_SUCCESS);
    }

    private static void sanitizeLegacyState(Player player, CompoundTag data) {
        if (data.getBoolean(DATA_ACTIVE)
                && (!data.contains(DATA_MIN_END_TICK)
                || !data.contains(DATA_MAX_END_TICK)
                || !isExtremeEvasionWindowOpen(player, data))) {
            clearWindow(data);
        }

        if (!data.contains(DATA_BULLET_TIME_INVULNERABLE_UNTIL)) {
            return;
        }

        long now = BulletTimeController.monotonicMillis();
        long invulnerableUntil = data.getLong(DATA_BULLET_TIME_INVULNERABLE_UNTIL);
        long maxExpectedRemainingMillis = Math.max(Config.bulletTimeDurationMillis + 1000L, 5000L);
        if (invulnerableUntil <= now || invulnerableUntil > now + maxExpectedRemainingMillis) {
            data.remove(DATA_BULLET_TIME_INVULNERABLE_UNTIL);
        }
    }

    private static boolean isMinimumWindowActive(Player player, CompoundTag data) {
        return data.contains(DATA_MIN_END_TICK)
                && player.level().getGameTime() <= data.getLong(DATA_MIN_END_TICK);
    }

    private static boolean isMaximumWindowActive(Player player, CompoundTag data) {
        return data.contains(DATA_MAX_END_TICK)
                && player.level().getGameTime() <= data.getLong(DATA_MAX_END_TICK);
    }

    private static boolean isExtremeEvasionWindowOpen(Player player, CompoundTag data) {
        return isMaximumWindowActive(player, data)
                && (isMinimumWindowActive(player, data) || RollCompat.isRolling(player));
    }

    private static void grantCounterAttack(ServerPlayer player, CompoundTag data) {
        int windowTicks = Config.extremeCounterAttackWindowTicks;
        int charges = Config.extremeCounterAttackCharges;
        if (windowTicks <= 0 || charges <= 0) {
            clearCounterAttack(data);
            ExtremeEvasionNetwork.sendCounterAttack(player, false);
            return;
        }

        data.putLong(DATA_COUNTER_ATTACK_END_TICK, player.level().getGameTime() + windowTicks);
        data.putInt(DATA_COUNTER_ATTACK_CHARGES, charges);
        clearActiveCounterAttack(data);
        ExtremeEvasionNetwork.sendCounterAttack(player, true);
    }

    public static boolean hasCounterAttackBoost(Player player) {
        return hasCounterAttack(player, getData(player));
    }

    private static boolean hasCounterAttack(Player player, CompoundTag data) {
        return data.contains(DATA_COUNTER_ATTACK_END_TICK)
                && player.level().getGameTime() <= data.getLong(DATA_COUNTER_ATTACK_END_TICK)
                && data.getInt(DATA_COUNTER_ATTACK_CHARGES) > 0;
    }

    private static void clearExpiredCounterAttack(Player player, CompoundTag data) {
        if (data.contains(DATA_COUNTER_ATTACK_END_TICK)
                && player.level().getGameTime() > data.getLong(DATA_COUNTER_ATTACK_END_TICK)) {
            clearCounterAttack(data);
            if (player instanceof ServerPlayer serverPlayer) {
                ExtremeEvasionNetwork.sendCounterAttack(serverPlayer, false);
            }
        }
    }

    private static void clearExpiredActiveCounterAttack(Player player, CompoundTag data) {
        if (data.getBoolean(DATA_COUNTER_ATTACK_ACTIVE)
                && player.level().getGameTime() > data.getLong(DATA_COUNTER_ATTACK_ACTIVE_TICK)) {
            clearActiveCounterAttack(data);
            if (!hasCounterAttack(player, data) && player instanceof ServerPlayer serverPlayer) {
                ExtremeEvasionNetwork.sendCounterAttack(serverPlayer, false);
            }
        }
    }

    private static void consumeCounterAttack(CompoundTag data) {
        int remainingCharges = data.getInt(DATA_COUNTER_ATTACK_CHARGES) - 1;
        if (remainingCharges <= 0) {
            data.remove(DATA_COUNTER_ATTACK_END_TICK);
            data.remove(DATA_COUNTER_ATTACK_CHARGES);
        } else {
            data.putInt(DATA_COUNTER_ATTACK_CHARGES, remainingCharges);
        }
    }

    private static boolean isActiveCounterAttackTarget(Entity target, CompoundTag data) {
        return data.hasUUID(DATA_COUNTER_ATTACK_TARGET_UUID)
                && target.getUUID().equals(data.getUUID(DATA_COUNTER_ATTACK_TARGET_UUID));
    }

    private static void clearCounterAttack(CompoundTag data) {
        data.remove(DATA_COUNTER_ATTACK_END_TICK);
        data.remove(DATA_COUNTER_ATTACK_CHARGES);
        clearActiveCounterAttack(data);
    }

    private static void clearActiveCounterAttack(CompoundTag data) {
        data.remove(DATA_COUNTER_ATTACK_ACTIVE);
        data.remove(DATA_COUNTER_ATTACK_ACTIVE_TICK);
        data.remove(DATA_COUNTER_ATTACK_TARGET_UUID);
    }

    private static float getArmorPiercingHurtAmount(LivingEntity target, float desiredAfterArmor) {
        if (desiredAfterArmor <= 0.0F) {
            return desiredAfterArmor;
        }

        float armor = (float) target.getArmorValue();
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        if (armor <= 0.0F) {
            return desiredAfterArmor;
        }

        return getHigh(desiredAfterArmor, armor, toughness);
    }

    private static float getHigh(float desiredAfterArmor, float armor, float toughness) {
        float low = desiredAfterArmor;
        float high = desiredAfterArmor;
        while (CombatRules.getDamageAfterAbsorb(high, armor, toughness) < desiredAfterArmor && high < desiredAfterArmor * 100.0F) {
            high *= 2.0F;
        }

        for (int i = 0; i < 16; i++) {
            float mid = (low + high) * 0.5F;
            if (CombatRules.getDamageAfterAbsorb(mid, armor, toughness) < desiredAfterArmor) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return high;
    }
}
