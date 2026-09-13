package gaydev.yaiden.femboysleeping.config;

/**
 * Plain data holder for mod settings. Lives in common code (not client-only)
 * so both the Cloth Config screen and the actual game logic (which runs on
 * the server/integrated server) can read the same values.
 */
public class ModConfig {

    /** Chance (0-100) that a baby villager spawns when conditions are met. */
    public int sliderValue = 5;

    /** In-game days between a successful roll and the baby actually spawning. */
    public int gestationDays = 3;

    public boolean allowGay = true;
}
