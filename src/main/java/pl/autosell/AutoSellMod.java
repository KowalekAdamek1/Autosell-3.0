package pl.autosell;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoSellMod implements ClientModInitializer {
    public static final AutoSellConfig CONFIG = new AutoSellConfig();

    private final GenCashMiner genCashMiner = new GenCashMiner();
    private final AutoFoodTask autoFoodTask = new AutoFoodTask();
    private final SwingAnimator swingAnimator = new SwingAnimator();
    private final Visuals visuals = new Visuals();

    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mod_na_gencasha.gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.mod_na_gencasha"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            visuals.renderWorld(context, client, CONFIG);
        });

        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            visuals.renderHud(drawContext, client, CONFIG, renderTickCounter.getTickDelta(true));
        });
    }

    private void onClientTick(MinecraftClient client) {
        while (openGuiKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new MainScreen(CONFIG));
            }
        }
        genCashMiner.tick(client, CONFIG);
        autoFoodTask.tick(client, CONFIG);
        swingAnimator.tick(client, CONFIG);
    }
}
