package cc.sighs.extremeevasion.mixin;

import cc.sighs.extremeevasion.handler.EvasionData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(Player.class)
public class PlayerDataMixin implements EvasionData {
    @Unique
    private boolean extremeEvasion$windowActive;
    @Unique
    private boolean extremeEvasion$windowSuccess;
    @Unique
    private boolean extremeEvasion$windowBlocked;
    @Unique
    private double extremeEvasion$startMinX;
    @Unique
    private double extremeEvasion$startMinY;
    @Unique
    private double extremeEvasion$startMinZ;
    @Unique
    private double extremeEvasion$startMaxX;
    @Unique
    private double extremeEvasion$startMaxY;
    @Unique
    private double extremeEvasion$startMaxZ;
    @Unique
    private long extremeEvasion$minEndTicks;
    @Unique
    private long extremeEvasion$maxEndTicks;
    @Unique
    private int extremeEvasion$evasionCooldownTicksRemaining = -1;
    @Unique
    private boolean extremeEvasion$rollConsumed;
    @Unique
    private long extremeEvasion$counterAttackEndTicks;
    @Unique
    private boolean extremeEvasion$counterAttackActive;
    @Unique
    private boolean extremeEvasion$counterAttackActiveTick;
    @Unique
    private UUID extremeEvasion$counterAttackTargetUuid;
    @Unique
    private int extremeEvasion$counterAttackCharges;
    @Unique
    private long extremeEvasion$bulletTimeInvulnerableUntil;

    @Override
    public boolean extremeEvasion$isWindowActive() {
        return extremeEvasion$windowActive;
    }

    @Override
    public void extremeEvasion$setWindowActive(boolean value) {
        extremeEvasion$windowActive = value;
    }

    @Override
    public boolean extremeEvasion$isWindowSuccess() {
        return extremeEvasion$windowSuccess;
    }

    @Override
    public void extremeEvasion$setWindowSuccess(boolean value) {
        extremeEvasion$windowSuccess = value;
    }

    @Override
    public boolean extremeEvasion$isWindowBlocked() {
        return extremeEvasion$windowBlocked;
    }

    @Override
    public void extremeEvasion$setWindowBlocked(boolean value) {
        extremeEvasion$windowBlocked = value;
    }

    @Override
    public double extremeEvasion$getStartMinX() {
        return extremeEvasion$startMinX;
    }

    @Override
    public void extremeEvasion$setStartMinX(double value) {
        extremeEvasion$startMinX = value;
    }

    @Override
    public double extremeEvasion$getStartMinY() {
        return extremeEvasion$startMinY;
    }

    @Override
    public void extremeEvasion$setStartMinY(double value) {
        extremeEvasion$startMinY = value;
    }

    @Override
    public double extremeEvasion$getStartMinZ() {
        return extremeEvasion$startMinZ;
    }

    @Override
    public void extremeEvasion$setStartMinZ(double value) {
        extremeEvasion$startMinZ = value;
    }

    @Override
    public double extremeEvasion$getStartMaxX() {
        return extremeEvasion$startMaxX;
    }

    @Override
    public void extremeEvasion$setStartMaxX(double value) {
        extremeEvasion$startMaxX = value;
    }

    @Override
    public double extremeEvasion$getStartMaxY() {
        return extremeEvasion$startMaxY;
    }

    @Override
    public void extremeEvasion$setStartMaxY(double value) {
        extremeEvasion$startMaxY = value;
    }

    @Override
    public double extremeEvasion$getStartMaxZ() {
        return extremeEvasion$startMaxZ;
    }

    @Override
    public void extremeEvasion$setStartMaxZ(double value) {
        extremeEvasion$startMaxZ = value;
    }

    @Override
    public long extremeEvasion$getMinEndTicks() {
        return extremeEvasion$minEndTicks;
    }

    @Override
    public void extremeEvasion$setMinEndTicks(long value) {
        extremeEvasion$minEndTicks = value;
    }

    @Override
    public long extremeEvasion$getMaxEndTicks() {
        return extremeEvasion$maxEndTicks;
    }

    @Override
    public void extremeEvasion$setMaxEndTicks(long value) {
        extremeEvasion$maxEndTicks = value;
    }

    @Override
    public int extremeEvasion$getEvasionCooldownTicksRemaining() {
        return extremeEvasion$evasionCooldownTicksRemaining;
    }

    @Override
    public void extremeEvasion$setEvasionCooldownTicksRemaining(int value) {
        extremeEvasion$evasionCooldownTicksRemaining = value;
    }

    @Override
    public boolean extremeEvasion$isRollConsumed() {
        return extremeEvasion$rollConsumed;
    }

    @Override
    public void extremeEvasion$setRollConsumed(boolean value) {
        extremeEvasion$rollConsumed = value;
    }

    @Override
    public long extremeEvasion$getCounterAttackEndTicks() {
        return extremeEvasion$counterAttackEndTicks;
    }

    @Override
    public void extremeEvasion$setCounterAttackEndTicks(long value) {
        extremeEvasion$counterAttackEndTicks = value;
    }

    @Override
    public boolean extremeEvasion$isCounterAttackActive() {
        return extremeEvasion$counterAttackActive;
    }

    @Override
    public void extremeEvasion$setCounterAttackActive(boolean value) {
        extremeEvasion$counterAttackActive = value;
    }

    @Override
    public boolean extremeEvasion$isCounterAttackActiveTick() {
        return extremeEvasion$counterAttackActiveTick;
    }

    @Override
    public void extremeEvasion$setCounterAttackActiveTick(boolean value) {
        extremeEvasion$counterAttackActiveTick = value;
    }

    @Override
    public UUID extremeEvasion$getCounterAttackTargetUuid() {
        return extremeEvasion$counterAttackTargetUuid;
    }

    @Override
    public void extremeEvasion$setCounterAttackTargetUuid(UUID value) {
        extremeEvasion$counterAttackTargetUuid = value;
    }

    @Override
    public int extremeEvasion$getCounterAttackCharges() {
        return extremeEvasion$counterAttackCharges;
    }

    @Override
    public void extremeEvasion$setCounterAttackCharges(int value) {
        extremeEvasion$counterAttackCharges = value;
    }

    @Override
    public long extremeEvasion$getBulletTimeInvulnerableUntil() {
        return extremeEvasion$bulletTimeInvulnerableUntil;
    }

    @Override
    public void extremeEvasion$setBulletTimeInvulnerableUntil(long value) {
        extremeEvasion$bulletTimeInvulnerableUntil = value;
    }
}
