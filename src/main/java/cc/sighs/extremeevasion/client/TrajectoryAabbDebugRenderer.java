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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = ExtremeEvasion.MODID, value = Dist.CLIENT)
public final class TrajectoryAabbDebugRenderer {
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (!Config.enableTrajectoryAabbDebugRendering || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        Minecraft mc = Minecraft.getInstance();
        var players = mc.getSingleplayerServer().getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        Player player = players.getFirst();
        if (!ExtremeEvasionEvents.hasActiveExtremeEvasionWindow(player)) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player clientPlayer = minecraft.player;
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer lines = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        var boundingBox = clientPlayer.getBoundingBox();
        var partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Vec3 interpolatedPosition = clientPlayer.getPosition(partialTick);
        Vec3 currentPosition = clientPlayer.position();
        AABB currentBox = boundingBox.move(interpolatedPosition.subtract(currentPosition));
        AABB startBox = ExtremeEvasionEvents.getExtremeEvasionStartAABB(player);
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        LevelRenderer.renderLineBox(poseStack, lines, startBox, 1.0F, 0.8F, 0.1F, 1.0F);
        LevelRenderer.renderLineBox(poseStack, lines, currentBox, 0.1F, 1.0F, 0.2F, 1.0F);
        Vec3[] start = corners(startBox), current = corners(currentBox);
        int[][] edges = {{0, 1}, {0, 2}, {0, 4}, {1, 3}, {1, 5}, {2, 3}, {2, 6}, {4, 5}, {4, 6}, {3, 7}, {5, 7}, {6, 7}};
        for (int[] edge : edges) renderLine(poseStack, lines, start[edge[0]], start[edge[1]], 1.0F, 0.15F, 0.15F, 1.0F);
        for (int i = 0; i < start.length; i++)
            renderLine(poseStack, lines, start[i], current[i], 1.0F, 0.15F, 0.15F, 1.0F);
        poseStack.popPose();
    }

    private static Vec3[] corners(AABB box) {
        return new Vec3[]{new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.maxX, box.minY, box.minZ), new Vec3(box.minX, box.maxY, box.minZ), new Vec3(box.maxX, box.maxY, box.minZ), new Vec3(box.minX, box.minY, box.maxZ), new Vec3(box.maxX, box.minY, box.maxZ), new Vec3(box.minX, box.maxY, box.maxZ), new Vec3(box.maxX, box.maxY, box.maxZ)};
    }

    private static void renderLine(PoseStack stack, VertexConsumer consumer, Vec3 start, Vec3 end, float r, float g, float b, float a) {
        PoseStack.Pose pose = stack.last();
        float dx = (float) (end.x - start.x), dy = (float) (end.y - start.y), dz = (float) (end.z - start.z);
        consumer.addVertex(pose.pose(), (float) start.x, (float) start.y, (float) start.z).setColor(r, g, b, a).setNormal(pose, dx, dy, dz);
        consumer.addVertex(pose.pose(), (float) end.x, (float) end.y, (float) end.z).setColor(r, g, b, a).setNormal(pose, dx, dy, dz);
    }
}
