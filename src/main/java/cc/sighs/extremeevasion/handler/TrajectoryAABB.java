package cc.sighs.extremeevasion.handler;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An AABB swept from the evasion start position to the player's current box.
 */
public final class TrajectoryAABB extends AABB {
    private final AABB startBox;
    private final AABB currentBox;

    public TrajectoryAABB(AABB startBox, AABB currentBox) {
        super(Math.min(startBox.minX, currentBox.minX),
                Math.min(startBox.minY, currentBox.minY),
                Math.min(startBox.minZ, currentBox.minZ),
                Math.max(startBox.maxX, currentBox.maxX),
                Math.max(startBox.maxY, currentBox.maxY),
                Math.max(startBox.maxZ, currentBox.maxZ));
        this.startBox = startBox;
        this.currentBox = currentBox;
    }

    private static List<ParameterPoint> clipAgainst(
            List<ParameterPoint> polygon,
            double rayCoefficient,
            double movementCoefficient,
            double limit
    ) {
        if (polygon.isEmpty()) {
            return polygon;
        }

        List<ParameterPoint> clipped = new ArrayList<>(polygon.size() + 1);
        ParameterPoint previous = polygon.get(polygon.size() - 1);
        double previousValue = value(previous, rayCoefficient, movementCoefficient) - limit;
        boolean previousInside = previousValue <= 1.0E-10D;

        for (ParameterPoint current : polygon) {
            double currentValue = value(current, rayCoefficient, movementCoefficient) - limit;
            boolean currentInside = currentValue <= 1.0E-10D;

            if (currentInside != previousInside) {
                double interpolation = previousValue / (previousValue - currentValue);
                clipped.add(new ParameterPoint(
                        previous.rayTime() + (current.rayTime() - previous.rayTime()) * interpolation,
                        previous.boxTime() + (current.boxTime() - previous.boxTime()) * interpolation
                ));
            }
            if (currentInside) {
                clipped.add(current);
            }

            previous = current;
            previousValue = currentValue;
            previousInside = currentInside;
        }
        return clipped;
    }

    private static double value(ParameterPoint point, double rayCoefficient, double movementCoefficient) {
        return rayCoefficient * point.rayTime() + movementCoefficient * point.boxTime();
    }

    @Override
    public Optional<Vec3> clip(Vec3 from, Vec3 to) {
        Vec3 ray = to.subtract(from);
        Vec3 movement = new Vec3(
                currentBox.minX - startBox.minX,
                currentBox.minY - startBox.minY,
                currentBox.minZ - startBox.minZ
        );

        // A point on the ray collides with the moving box when
        // from + ray * s - movement * t is inside the start box, where
        // both s (ray time) and t (box time) are in [0, 1]. The feasible
        // region in (s, t) is a convex polygon, so clip it against the
        // six slab planes of the box.
        List<ParameterPoint> feasible = new ArrayList<>(List.of(
                new ParameterPoint(0.0D, 0.0D),
                new ParameterPoint(1.0D, 0.0D),
                new ParameterPoint(1.0D, 1.0D),
                new ParameterPoint(0.0D, 1.0D)
        ));

        feasible = clipAgainst(feasible, ray.x, -movement.x, startBox.maxX - from.x);
        feasible = clipAgainst(feasible, -ray.x, movement.x, from.x - startBox.minX);
        feasible = clipAgainst(feasible, ray.y, -movement.y, startBox.maxY - from.y);
        feasible = clipAgainst(feasible, -ray.y, movement.y, from.y - startBox.minY);
        feasible = clipAgainst(feasible, ray.z, -movement.z, startBox.maxZ - from.z);
        feasible = clipAgainst(feasible, -ray.z, movement.z, from.z - startBox.minZ);

        if (feasible.isEmpty()) {
            return Optional.empty();
        }

        double firstRayTime = feasible.stream()
                .mapToDouble(ParameterPoint::rayTime)
                .min()
                .orElse(0.0D);
        return Optional.of(from.add(ray.scale(firstRayTime)));
    }

    @Override
    public AABB inflate(double x, double y, double z) {
        return new TrajectoryAABB(startBox.inflate(x, y, z), currentBox.inflate(x, y, z));
    }

    @Override
    public AABB inflate(double amount) {
        return inflate(amount, amount, amount);
    }

    private record ParameterPoint(double rayTime, double boxTime) {
    }
}
