package pl.autosell;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MainScreen extends Screen {
    private final AutoSellConfig cfg;

    public MainScreen(AutoSellConfig cfg) {
        super(Text.literal("MOD NA GENCASHA // PANEL"));
        this.cfg = cfg;
    }

    @Override
    protected void init() {
        int colW = 290;
        int x = this.width / 2 - (colW / 2);
        int y = 48;
        int step = 23;

        // 1. Mod na Gencasha (AFK)
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Mod na Gencasha (AFK): " + status(cfg.genCashEnabled)),
                btn -> {
                    cfg.genCashEnabled = !cfg.genCashEnabled;
                    btn.setMessage(Text.literal("Mod na Gencasha (AFK): " + status(cfg.genCashEnabled)));
                }
        ).dimensions(x, y, colW, 20).build());

        // 2. Obszar kopania: 3x3, 5x5, 7x7, 9x9, 10x10
        y += step;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Obszar kopania: " + cfg.areaMode.size + "x" + cfg.areaMode.size + " (" + (cfg.areaMode.size * cfg.areaMode.size) + " bloków)"),
                btn -> {
                    AutoSellConfig.AreaMode[] values = AutoSellConfig.AreaMode.values();
                    cfg.areaMode = values[(cfg.areaMode.ordinal() + 1) % values.length];
                    btn.setMessage(Text.literal("Obszar kopania: " + cfg.areaMode.size + "x" + cfg.areaMode.size + " (" + (cfg.areaMode.size * cfg.areaMode.size) + " bloków)"));
                }
        ).dimensions(x, y, colW, 20).build());

        // 3. Szybkość (Delay) i Range kopania
        y += step;
        addDrawableChild(ButtonWidget.builder(
                Text.literal(String.format("Range: %.1f | Szybkość (Delay): %d tick", cfg.mineRange, cfg.mineDelayTicks)),
                btn -> {
                    cfg.mineRange = cfg.mineRange >= 6.0 ? 3.5 : cfg.mineRange + 0.5;
                    cfg.mineDelayTicks = cfg.mineDelayTicks <= 1 ? 4 : cfg.mineDelayTicks - 1;
                    btn.setMessage(Text.literal(String.format("Range: %.1f | Szybkość (Delay): %d tick", cfg.mineRange, cfg.mineDelayTicks)));
                }
        ).dimensions(x, y, colW, 20).build());

        // 4. AutoFood + Próg głodu (np. 7)
        y += step;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("AutoFood (Offhand): " + status(cfg.autoFoodEnabled) + " | Próg głodu: " + cfg.hungerThreshold),
                btn -> {
                    cfg.hungerThreshold = cfg.hungerThreshold >= 15 ? 4 : cfg.hungerThreshold + 1;
                    btn.setMessage(Text.literal("AutoFood (Offhand): " + status(cfg.autoFoodEnabled) + " | Próg głodu: " + cfg.hungerThreshold));
                }
        ).dimensions(x, y, colW, 20).build());

        // 5. Swing Animations
        y += step;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Swing Animations: " + status(cfg.customSwingEnabled) + " (" + String.format("%.1fx", cfg.swingSpeedMultiplier) + ")"),
                btn -> {
                    cfg.customSwingEnabled = !cfg.customSwingEnabled;
                    btn.setMessage(Text.literal("Swing Animations: " + status(cfg.customSwingEnabled) + " (" + String.format("%.1fx", cfg.swingSpeedMultiplier) + ")"));
                }
        ).dimensions(x, y, colW, 20).build());

        // 6. Ore ESP 3D Fill
        y += step;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Ore ESP (Pełne 3D Fill): " + status(cfg.oreEsp3dFill)),
                btn -> {
                    cfg.oreEsp3dFill = !cfg.oreEsp3dFill;
                    btn.setMessage(Text.literal("Ore ESP (Pełne 3D Fill): " + status(cfg.oreEsp3dFill)));
                }
        ).dimensions(x, y, colW, 20).build());

        // 7. Tracers od celownika
        y += step;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Płynne Tracers (Od celownika): " + status(cfg.tracersFromCrosshair)),
                btn -> {
                    cfg.tracersFromCrosshair = !cfg.tracersFromCrosshair;
                    btn.setMessage(Text.literal("Płynne Tracers (Od celownika): " + status(cfg.tracersFromCrosshair)));
                }
        ).dimensions(x, y, colW, 20).build());

        // 8. Player ESP 3D + 2D + Nametagi 3D
        y += step;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Player ESP 3D: " + status(cfg.playerEsp3d) + " | 2D: " + status(cfg.playerEsp2d) + " | Nametag 3D: " + status(cfg.nametags3d)),
                btn -> {
                    cfg.playerEsp3d = !cfg.playerEsp3d;
                    cfg.playerEsp2d = !cfg.playerEsp2d;
                    cfg.nametags3d = !cfg.nametags3d;
                    btn.setMessage(Text.literal("Player ESP 3D: " + status(cfg.playerEsp3d) + " | 2D: " + status(cfg.playerEsp2d) + " | Nametag 3D: " + status(cfg.nametags3d)));
                }
        ).dimensions(x, y, colW, 20).build());
    }

    private String status(boolean val) {
        return val ? "ON" : "OFF";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelW = 314;
        int panelH = 224;
        int panelX = this.width / 2 - (panelW / 2);
        int panelY = 22;

        // Ostre, wysokokontrastowe tło i obramowanie 1px
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, cfg.panelBgColor);
        context.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 21, cfg.cardBgColor);
        drawSharpBorder(context, panelX, panelY, panelW, panelH, cfg.accentColor);
        context.fill(panelX, panelY + 21, panelX + panelW, panelY + 22, cfg.accentColor);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 7, 0xFF00E5FF);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSharpBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }
}
