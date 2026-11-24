package pl.autosell;

import net.minecraft.client.MinecraftClient;

public class SwingAnimator {
    public void tick(MinecraftClient client, AutoSellConfig cfg) {
        if (!cfg.customSwingEnabled || client.player == null) {
            return;
        }
        if (client.player.handSwinging) {
            float step = 0.08f * cfg.swingSpeedMultiplier;
            client.player.handSwingProgress = Math.min(1.0f, client.player.handSwingProgress + step);
        }
    }
}
