package pl.autosell;

public class AutoSellConfig {
    public enum AreaMode {
        M_3X3(3),
        M_5X5(5),
        M_7X7(7),
        M_9X9(9),
        M_10X10(10);

        public final int size;

        AreaMode(int size) {
            this.size = size;
        }
    }

    // Mod na Gencasha (AFK + obszar)
    public boolean genCashEnabled = false;
    public AreaMode areaMode = AreaMode.M_3X3;
    public double mineRange = 4.5;
    public int mineDelayTicks = 2;

    // AutoFood
    public boolean autoFoodEnabled = true;
    public int hungerThreshold = 7;

    // Swing animations
    public boolean customSwingEnabled = true;
    public float swingSpeedMultiplier = 1.6f;

    // Visuals: Ore ESP 3D Fill, Player ESP (2D + 3D), Tracers od celownika, Nametagi 3D
    public boolean oreEsp3dFill = true;
    public boolean playerEsp3d = true;
    public boolean playerEsp2d = true;
    public boolean tracersFromCrosshair = true;
    public boolean nametags3d = true;

    // Wyostrzony motyw GUI
    public int accentColor = 0xFF00E5FF;
    public int panelBgColor = 0xEE0D1117;
    public int cardBgColor = 0xFF161B22;
}
