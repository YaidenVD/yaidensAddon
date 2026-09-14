package gaydev.yaiden.femboysleeping.tryr;

import java.util.List;

import com.wildfire.main.Gender;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import gaydev.yaiden.femboysleeping.config.YaidensaddonConfigManager;
import gaydev.yaiden.femboysleeping.gayness.VillagerDataAccessor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import com.wildfire.main.entitydata.EntityConfig;


public class Villgaer {

    private final BlockPos pos;
    private final Level level;
    public Player player;

    /**
     * npc is currently unused (kept for source compatibility with existing
     * call sites).
     */
    public Villgaer(Level level, BlockPos pos, Villager npc, Player player) {
        this.level = level;
        this.pos = pos;
        this.player = player;
    }

    /**
     * Two ways this can trigger:
     *  - Two real players sharing a bed (opposite Wildfire genders), or
     *  - One player sharing a bed with a villager via Room For Two
     *    (singleplayer-friendly path; uses the villager's own persisted
     *    gender rather than rerolling one).
     * Either way, if genders are opposite, rolls the configured spawn
     * chance and schedules a baby villager `gestationDays` days out.
     */
    public void gay(long currentDay) {

        List<Player> sleepingPlayers = this.level.getEntitiesOfClass(
            Player.class,
            new AABB(this.pos).inflate(4.0),
            p -> p.isSleeping()
                && p.getSleepingPos().isPresent()
                && p.getSleepingPos().get().equals(this.pos)
        );

        if (sleepingPlayers.isEmpty()) {
            return;
        }

        Player player1 = sleepingPlayers.get(0);
        EntityConfig config1 = EntityConfig.getEntity(player1);

        if (config1 == null) {
            return;
        }

        Gender gender1 = config1.getGender();
        Gender partnerGender;

        if (sleepingPlayers.size() >= 2) {

            // Two-player path.
            EntityConfig config2 = EntityConfig.getEntity(sleepingPlayers.get(1));

            if (config2 == null) {
                return;
            }

            partnerGender = config2.getGender();

        } else {

            // Singleplayer path: look for an adult villager sharing the
            // bed via Room For Two instead of a second player.
            List<Villager> nearbyVillagers = this.level.getEntitiesOfClass(
                Villager.class,
                new AABB(this.pos).inflate(4.0),
                v -> v.isSleeping()
                    && v.getSleepingPos().isPresent()
                    && v.getSleepingPos().get().equals(this.pos)
                    && !v.isBaby()
            );

            if (nearbyVillagers.isEmpty()) {
                return;
            }

            VillagerDataAccessor accessor = (VillagerDataAccessor) nearbyVillagers.get(0);

            if (!accessor.hasGender()) {
                return;
            }

            partnerGender = accessor.getGender();
        }

        boolean oppositeGenders =
            (gender1 == Gender.MALE && partnerGender == Gender.FEMALE)
                || (gender1 == Gender.FEMALE && partnerGender == Gender.MALE);

        if (!oppositeGenders) {
            return;
        }

        this.player = player1;

        int roll = level.random.nextInt(1, 101);
        int chance = YaidensaddonConfigManager.CONFIG.sliderValue;

        if (roll > chance) {
            Yaidensaddon.LOGGER.info(
                "gay(): rolled {} against {}% chance, no spawn scheduled",
                roll,
                chance
            );
            return;
        }

        long dueDay = currentDay + YaidensaddonConfigManager.CONFIG.gestationDays;

        SpawnScheduler.schedule(
            level,
            player1.getX(),
            player1.getY(),
            player1.getZ(),
            player1.getUUID(),
            currentDay,
            dueDay
        );
    }
}
