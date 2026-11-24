package pl.autosell;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoFoodTask {
    private boolean eating = false;

    public void tick(MinecraftClient client, AutoSellConfig cfg) {
        if (!cfg.autoFoodEnabled || client.player == null || client.interactionManager == null) {
            stopIfNeeded(client);
            return;
        }

        int currentHunger = client.player.getHungerManager().getFoodLevel();
        ItemStack offhandStack = client.player.getOffHandStack();

        boolean offhandIsFood = !offhandStack.isEmpty()
                && offhandStack.getItem().getComponents().contains(DataComponentTypes.FOOD);

        if (currentHunger <= cfg.hungerThreshold && offhandIsFood) {
            eating = true;
            client.options.useKey.setPressed(true);
            client.interactionManager.interactItem(client.player, Hand.OFF_HAND);
        } else if (eating && (currentHunger >= 20 || !offhandIsFood)) {
            stopIfNeeded(client);
        }
    }

    private void stopIfNeeded(MinecraftClient client) {
        if (eating) {
            eating = false;
            if (client != null && client.options != null) {
                client.options.useKey.setPressed(false);
            }
        }
    }
}
