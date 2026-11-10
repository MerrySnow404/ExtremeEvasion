package cc.sighs.extremeevasion.handler;

import java.util.UUID;

public interface EvasionData {
    boolean extremeEvasion$isWindowActive();

    void extremeEvasion$setWindowActive(boolean value);

    boolean extremeEvasion$isWindowSuccess();

    void extremeEvasion$setWindowSuccess(boolean value);

    boolean extremeEvasion$isWindowBlocked();

    void extremeEvasion$setWindowBlocked(boolean value);

    double extremeEvasion$getStartMinX();

    void extremeEvasion$setStartMinX(double value);

    double extremeEvasion$getStartMinY();

    void extremeEvasion$setStartMinY(double value);

    double extremeEvasion$getStartMinZ();

    void extremeEvasion$setStartMinZ(double value);

    double extremeEvasion$getStartMaxX();

    void extremeEvasion$setStartMaxX(double value);

    double extremeEvasion$getStartMaxY();

    void extremeEvasion$setStartMaxY(double value);

    double extremeEvasion$getStartMaxZ();

    void extremeEvasion$setStartMaxZ(double value);

    long extremeEvasion$getMinEndTicks();

    void extremeEvasion$setMinEndTicks(long value);

    long extremeEvasion$getMaxEndTicks();

    void extremeEvasion$setMaxEndTicks(long value);

    int extremeEvasion$getEvasionCooldownTicksRemaining();

    void extremeEvasion$setEvasionCooldownTicksRemaining(int value);

    boolean extremeEvasion$isRollConsumed();

    void extremeEvasion$setRollConsumed(boolean value);

    long extremeEvasion$getCounterAttackEndTicks();

    void extremeEvasion$setCounterAttackEndTicks(long value);

    boolean extremeEvasion$isCounterAttackActive();

    void extremeEvasion$setCounterAttackActive(boolean value);

    boolean extremeEvasion$isCounterAttackActiveTick();

    void extremeEvasion$setCounterAttackActiveTick(boolean value);

    UUID extremeEvasion$getCounterAttackTargetUuid();

    void extremeEvasion$setCounterAttackTargetUuid(UUID value);

    int extremeEvasion$getCounterAttackCharges();

    void extremeEvasion$setCounterAttackCharges(int value);

    long extremeEvasion$getBulletTimeInvulnerableUntil();

    void extremeEvasion$setBulletTimeInvulnerableUntil(long value);
}
