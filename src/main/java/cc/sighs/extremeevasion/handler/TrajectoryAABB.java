package cc.sighs.extremeevasion.handler;

import net.minecraft.core.BlockPos;
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
    public AABB setMinX(double minX) {
        return new TrajectoryAABB(startBox.setMinX(minX), currentBox.setMinX(minX));
    }

    @Override
    public AABB setMinY(double minY) {
        return new TrajectoryAABB(startBox.setMinY(minY), currentBox.setMinY(minY));
    }

    @Override
    public AABB setMinZ(double minZ) {
        return new TrajectoryAABB(startBox.setMinZ(minZ), currentBox.setMinZ(minZ));
    }

    @Override
    public AABB setMaxX(double maxX) {
        return new TrajectoryAABB(startBox.setMaxX(maxX), currentBox.setMaxX(maxX));
    }

    @Override
    public AABB setMaxY(double maxY) {
        return new TrajectoryAABB(startBox.setMaxY(maxY), currentBox.setMaxY(maxY));
    }

    @Override
    public AABB setMaxZ(double maxZ) {
        return new TrajectoryAABB(startBox.setMaxZ(maxZ), currentBox.setMaxZ(maxZ));
    }

    @Override
    public AABB contract(double x, double y, double z) {
        return new TrajectoryAABB(startBox.contract(x, y, z), currentBox.contract(x, y, z));
    }

    @Override
    public AABB expandTowards(Vec3 direction) {
        return new TrajectoryAABB(startBox.expandTowards(direction), currentBox.expandTowards(direction));
    }

    @Override
    public AABB expandTowards(double x, double y, double z) {
        return new TrajectoryAABB(startBox.expandTowards(x, y, z), currentBox.expandTowards(x, y, z));
    }

    @Override
    public AABB inflate(double x, double y, double z) {
        return new TrajectoryAABB(startBox.inflate(x, y, z), currentBox.inflate(x, y, z));
    }

    @Override
    public AABB inflate(double amount) {
        return inflate(amount, amount, amount);
    }

    @Override
    public AABB intersect(AABB other) {
        return new TrajectoryAABB(startBox.intersect(other), currentBox.intersect(other));
    }

    @Override
    public AABB minmax(AABB other) {
        return new TrajectoryAABB(startBox.minmax(other), currentBox.minmax(other));
    }

    @Override
    public AABB move(double x, double y, double z) {
        return new TrajectoryAABB(startBox.move(x, y, z), currentBox.move(x, y, z));
    }

    @Override
    public AABB move(BlockPos position) {
        return new TrajectoryAABB(startBox.move(position), currentBox.move(position));
    }

    @Override
    public AABB move(Vec3 position) {
        return new TrajectoryAABB(startBox.move(position), currentBox.move(position));
    }

    @Override
    public AABB deflate(double x, double y, double z) {
        return new TrajectoryAABB(startBox.deflate(x, y, z), currentBox.deflate(x, y, z));
    }

    @Override
    public AABB deflate(double amount) {
        return deflate(amount, amount, amount);
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

    private record ParameterPoint(double rayTime, double boxTime) {
    }
}
