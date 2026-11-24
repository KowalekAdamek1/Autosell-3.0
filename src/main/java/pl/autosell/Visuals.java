package pl.autosell;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Set;

public class Visuals {

    private static final Set<Block> TARGET_ORES = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.ANCIENT_DEBRIS
    );

    private Matrix4f lastViewProjMatrix = null;

    public void renderWorld(WorldRenderContext context, MinecraftClient client, AutoSellConfig cfg) {
        if (client.player == null || client.world == null) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        Camera camera = context.camera();
        if (matrices == null || consumers == null) {
            return;
        }

        Vec3d camPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);

        // Zapis macierzy projekcji dla płynnych tracerów i 2D ESP
        Matrix4f proj = new Matrix4f(context.projectionMatrix());
        Matrix4f view = new Matrix4f(matrices.peek().getPositionMatrix());
        lastViewProjMatrix = proj.mul(view);

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        // 1. Ore ESP: pełny 3D fill
        if (cfg.oreEsp3dFill) {
            BlockPos center = client.player.getBlockPos();
            int r = 16;
            for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
                Block block = client.world.getBlockState(pos).getBlock();
                if (TARGET_ORES.contains(block)) {
                    Box box = new Box(pos);
                    render3DBoxFilled(matrices, consumers, box, 0.0f, 0.90f, 1.0f, 0.28f);
                }
            }
        }

        // 2. Player ESP 3D + Nametagi 3D
        for (PlayerEntity target : client.world.getPlayers()) {
            if (target == client.player || !target.isAlive()) {
                continue;
            }
            Vec3d interpPos = getInterpolatedPos(target, tickDelta);
            Box playerBox = target.getBoundingBox().offset(interpPos.subtract(target.getPos()));

            if (cfg.playerEsp3d) {
                render3DBoxFilled(matrices, consumers, playerBox, 1.0f, 0.22f, 0.35f, 0.26f);
            }

            if (cfg.nametags3d) {
                render3DNametag(matrices, consumers, client.textRenderer, camera, target, interpPos);
            }
        }

        matrices.pop();
    }

    public void renderHud(DrawContext context, MinecraftClient client, AutoSellConfig cfg, float tickDelta) {
        if (client.player == null || client.world == null || lastViewProjMatrix == null) {
            return;
        }

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        // Środek celownika jako punkt początkowy tracerów
        int crossX = screenW / 2;
        int crossY = screenH / 2;

        Vec3d camPos = client.gameRenderer.getCamera().getPos();

        for (PlayerEntity target : client.world.getPlayers()) {
            if (target == client.player || !target.isAlive()) {
                continue;
            }

            Vec3d interp = getInterpolatedPos(target, tickDelta);
            Vec3d center = interp.add(0, target.getHeight() * 0.5, 0);
            Vec3d head = interp.add(0, target.getHeight() + 0.15, 0);
            Vec3d feet = interp;

            int[] centerScreen = worldToScreen(center, camPos, screenW, screenH);
            if (centerScreen != null && cfg.tracersFromCrosshair) {
                drawSmoothLine(context, crossX, crossY, centerScreen[0], centerScreen[1], 0xDD00E5FF);
            }

            if (cfg.playerEsp2d) {
                int[] topScreen = worldToScreen(head, camPos, screenW, screenH);
                int[] botScreen = worldToScreen(feet, camPos, screenW, screenH);
                if (topScreen != null && botScreen != null) {
                    int h = Math.max(8, botScreen[1] - topScreen[1]);
                    int w = Math.max(6, (int) (h * 0.55f));
                    int left = topScreen[0] - w / 2;
                    int top = topScreen[1];
                    drawSharpRectOutline(context, left, top, w, h, 0xFFFF385C);
                }
            }
        }
    }

    private Vec3d getInterpolatedPos(PlayerEntity entity, float tickDelta) {
        double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());
        return new Vec3d(x, y, z);
    }

    private int[] worldToScreen(Vec3d worldPos, Vec3d camPos, int screenW, int screenH) {
        Vector4f pos = new Vector4f(
                (float) (worldPos.x - camPos.x),
                (float) (worldPos.y - camPos.y),
                (float) (worldPos.z - camPos.z),
                1.0f
        );
        pos.mul(lastViewProjMatrix);
        if (pos.w <= 0.05f) {
            return null;
        }
        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;
        int sx = (int) ((ndcX + 1.0f) * 0.5f * screenW);
        int sy = (int) ((1.0f - ndcY) * 0.5f * screenH);
        return new int[]{sx, sy};
    }

    private void drawSmoothLine(DrawContext ctx, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int steps = 0;
        while (steps++ < 2500) {
            ctx.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private void drawSharpRectOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void render3DNametag(MatrixStack matrices, VertexConsumerProvider consumers, TextRenderer textRenderer, Camera camera, PlayerEntity player, Vec3d pos) {
        matrices.push();
        matrices.translate(pos.x, pos.y + player.getHeight() + 0.45, pos.z);
        matrices.multiply(camera.getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);

        String label = player.getName().getString() + String.format(" [%.0f HP]", player.getHealth());
        float offsetX = -textRenderer.getWidth(label) / 2.0f;
        textRenderer.draw(label, offsetX, 0, 0xFFFFFFFF, false, matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x99000000, 15728880);
        matrices.pop();
    }

    public void render3DBoxFilled(MatrixStack matrices, VertexConsumerProvider consumers, Box box, float r, float g, float b, float alpha) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getDebugFilledBox());
        MatrixStack.Entry entry = matrices.peek();

        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        quad(buffer, entry, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, alpha);
        quad(buffer, entry, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, alpha);
        quad(buffer, entry, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, alpha);
        quad(buffer, entry, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, alpha);
        quad(buffer, entry, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, r, g, b, alpha);
        quad(buffer, entry, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, alpha);
    }

    private void quad(VertexConsumer vc, MatrixStack.Entry entry,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float x4, float y4, float z4,
                      float r, float g, float b, float a) {
        vc.vertex(entry, x1, y1, z1).color(r, g, b, a);
        vc.vertex(entry, x2, y2, z2).color(r, g, b, a);
        vc.vertex(entry, x3, y3, z3).color(r, g, b, a);
        vc.vertex(entry, x4, y4, z4).color(r, g, b, a);
    }
}
