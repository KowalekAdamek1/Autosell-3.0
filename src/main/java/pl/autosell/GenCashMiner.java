package pl.autosell;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class GenCashMiner {
    private int tickCounter = 0;

    public void tick(MinecraftClient client, AutoSellConfig cfg) {
        if (!cfg.genCashEnabled || client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        tickCounter++;
        if (tickCounter < Math.max(1, cfg.mineDelayTicks)) {
            return;
        }
        tickCounter = 0;

        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos center = bhr.getBlockPos();
        List<BlockPos> targets = collectArea(center, bhr.getSide(), cfg.areaMode.size, client, cfg.mineRange);

        for (BlockPos pos : targets) {
            BlockState state = client.world.getBlockState(pos);
            if (state.isAir() || state.getHardness(client.world, pos) < 0) {
                continue;
            }
            client.interactionManager.updateBlockBreakingProgress(pos, bhr.getSide());
            client.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private List<BlockPos> collectArea(BlockPos center, Direction side, int size, MinecraftClient client, double maxRange) {
        List<BlockPos> list = new ArrayList<>();
        int radiusLow = -(size - 1) / 2;
        int radiusHigh = size / 2;

        double maxDistSq = maxRange * maxRange;
        Vec3d eyePos = client.player.getEyePos();

        for (int a = radiusLow; a <= radiusHigh; a++) {
            for (int b = radiusLow; b <= radiusHigh; b++) {
                BlockPos target = switch (side.getAxis()) {
                    case X -> center.add(0, a, b);
                    case Y -> center.add(a, 0, b);
                    case Z -> center.add(a, b, 0);
                };
                if (target.getSquaredDistance(eyePos.x, eyePos.y, eyePos.z) <= maxDistSq) {
                    list.add(target);
                }
            }
        }
        return list;
    }
}
