package gaydev.yaiden.femboysleeping.tryr;

import java.util.List;

import com.wildfire.main.Gender;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import gaydev.yaiden.femboysleeping.config.YaidensaddonConfigManager;
import gaydev.yaiden.femboysleeping.functions.Fabricatedfuncs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.core.BlockPos;

import com.wildfire.main.entitydata.EntityConfig;


public class Villgaer {

    private final BlockPos pos;
    private final Level level;
    public Player player;

    /**
     * npc is currently unused (kept for source compatibility with existing
     * call sites) - gaypc() always spawns a brand new villager rather than
     * reusing an existing one.
     */
    public Villgaer(Level level, BlockPos pos, Villager npc, Player player) {
        this.level = level;
        this.pos = pos;
        this.player = player;
    }

    /**
     * Looks for two sleeping players at this bed. If they have opposite
     * Wildfire genders, rolls the configured spawn chance and spawns a
     * baby villager if it hits.
     */
    public void gay() {

        List<Player> players = this.level.getEntitiesOfClass(
            Player.class,
            new AABB(this.pos).inflate(4.0),
            p -> p.isSleeping()
                && p.getSleepingPos().isPresent()
                && p.getSleepingPos().get().equals(this.pos)
        );

        if (players.size() < 2) {
            Yaidensaddon.LOGGER.info(
                "gay(): only {} sleeping player(s) at {}, skipping",
                players.size(),
                this.pos
            );
            return;
        }

        Player player1 = players.get(0);
        Player player2 = players.get(1);

        EntityConfig config1 = EntityConfig.getEntity(player1);
        EntityConfig config2 = EntityConfig.getEntity(player2);

        if (config1 == null || config2 == null) {
            return;
        }

        Gender gender1 = config1.getGender();
        Gender gender2 = config2.getGender();

        boolean oppositeGenders =
            (gender1 == Gender.MALE && gender2 == Gender.FEMALE)
                || (gender1 == Gender.FEMALE && gender2 == Gender.MALE);

        if (!oppositeGenders) {
            return;
        }

        // this.player is whichever of the pair triggered the day-tick;
        // used only for the spawn position / log message below.
        this.player = player1;

        gaypc();
    }

    /**
     * Rolls the configured spawn percentage and, if it hits, spawns a
     * baby villager at the current player's position.
     */
    public void gaypc() {

        int roll = level.random.nextInt(1, 101);
        int chance = YaidensaddonConfigManager.CONFIG.sliderValue;

        if (roll > chance) {
            Yaidensaddon.LOGGER.info(
                "gaypc(): rolled {} against {}%% chance, no spawn",
                roll,
                chance
            );
            return;
        }

        Villager villager = EntityType.VILLAGER.create(level);

        if (villager != null) {

            villager.setAge(-24000);
            villager.setBaby(true);

            villager.moveTo(
                player.getX(),
                player.getY(),
                player.getZ(),
                0.0F,
                0.0F
            );

            level.addFreshEntity(villager);

            Yaidensaddon.LOGGER.info(
                "Villager spawned near {}",
                player.getName().getString()
            );
        }
    }
}