package cc.sighs.extremeevasion.client;

import cc.sighs.extremeevasion.Config;
import cc.sighs.extremeevasion.ExtremeEvasion;
import cc.sighs.extremeevasion.ExtremeEvasionEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExtremeEvasion.MODID, value = Dist.CLIENT)
public class TrajectoryAabbDebugRenderer {
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (!Config.enableTrajectoryAabbDebugRendering
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        var players = mc.getSingleplayerServer().getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        Player player = players.get(0);
        if (!ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player)) return;

        PoseStack poseStack = event.getPoseStack();
        VertexConsumer lines = mc.renderBuffers()
                .bufferSource()
                .getBuffer(RenderType.lines());

        Vec3 cameraPos = mc.gameRenderer
                .getMainCamera()
                .getPosition();

        poseStack.pushPose();

        // 将世界坐标转换为相对于摄像机的坐标
        poseStack.translate(
                -cameraPos.x,
                -cameraPos.y,
                -cameraPos.z
        );

        var clientPlayer = mc.player;
        var boundingBox = clientPlayer.getBoundingBox();
        float partialTick = event.getPartialTick();
        Vec3 interpolatedPosition = clientPlayer.getPosition(partialTick);
        Vec3 currentPosition = clientPlayer.position();
        AABB currentBox = boundingBox.move(interpolatedPosition.subtract(currentPosition));

        AABB startBox = ExtremeEvasionEvents.getExtremeEvasionStartAABB(player);

        renderTrajectory(poseStack, lines, startBox, currentBox);

        LevelRenderer.renderLineBox(
                poseStack,
                lines,
                startBox,
                1.0F,
                0.8F,
                0.1F,
                1.0F
        );

        LevelRenderer.renderLineBox(
                poseStack,
                lines,
                currentBox,
                0.1F,
                1.0F,
                0.2F,
                1.0F
        );

        poseStack.popPose();
    }

    private static void renderTrajectory(
            PoseStack poseStack,
            VertexConsumer consumer,
            AABB startBox,
            AABB currentBox
    ) {
        Vec3[] startCorners = corners(startBox);
        Vec3[] currentCorners = corners(currentBox);

        // Draw the two end boxes and the eight corner paths. Together these
        // are the wireframe of the box swept along the player's movement.
        int[][] boxEdges = {
                {0, 1}, {0, 2}, {0, 4},
                {1, 3}, {1, 5}, {2, 3},
                {2, 6}, {4, 5}, {4, 6},
                {3, 7}, {5, 7}, {6, 7}
        };

        for (int[] edge : boxEdges) {
            renderLine(poseStack, consumer, startCorners[edge[0]], startCorners[edge[1]], 1.0F, 0.15F, 0.15F, 1.0F);
            renderLine(poseStack, consumer, currentCorners[edge[0]], currentCorners[edge[1]], 1.0F, 0.15F, 0.15F, 1.0F);
        }
        for (int i = 0; i < startCorners.length; i++) {
            renderLine(poseStack, consumer, startCorners[i], currentCorners[i], 1.0F, 0.15F, 0.15F, 1.0F);
        }
    }

    private static Vec3[] corners(AABB box) {
        return new Vec3[]{
                new Vec3(box.minX, box.minY, box.minZ),
                new Vec3(box.maxX, box.minY, box.minZ),
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.minX, box.minY, box.maxZ),
                new Vec3(box.maxX, box.minY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.maxZ),
                new Vec3(box.maxX, box.maxY, box.maxZ)
        };
    }

    private static void renderLine(
            PoseStack poseStack,
            VertexConsumer consumer,
            Vec3 start,
            Vec3 end,
            float r,
            float g,
            float b,
            float a
    ) {
        PoseStack.Pose pose = poseStack.last();

        float dx = (float) (end.x - start.x);
        float dy = (float) (end.y - start.y);
        float dz = (float) (end.z - start.z);

        consumer.vertex(
                        pose.pose(),
                        (float) start.x,
                        (float) start.y,
                        (float) start.z
                ).color(r, g, b, a)
                .normal(pose.normal(), dx, dy, dz)
                .endVertex();

        consumer.vertex(
                        pose.pose(),
                        (float) end.x,
                        (float) end.y,
                        (float) end.z
                ).color(r, g, b, a)
                .normal(pose.normal(), dx, dy, dz)
                .endVertex();
    }
}
