package gaydev.yaiden.femboysleeping.client.random;

import java.util.List;

import com.wildfire.main.Gender;
import com.wildfire.main.entitydata.EntityConfig;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import gaydev.yaiden.femboysleeping.client.screen.Yaidensaddonscreen;
import gaydev.yaiden.femboysleeping.tryr.Vilgendersystem;
import gaydev.yaiden.femboysleeping.tryr.Villgaer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class oops {
    private BlockPos pos;
    private Level level;
    public static Villager npc;
    public Player player;
    public Villgaer vil;
    public void npcs() {
        if (level.random.nextInt(1, 100) <= Yaidensaddonscreen.percentage) {
            vil.gaypc();
            Yaidensaddon.LOGGER.info(
                "it spawned",
                player.getName().getString()
            );
        }

    }

        void gay() {
        List<Player> players = this.level.getEntitiesOfClass(
            Player.class,
            new AABB(this.pos).inflate(4.0),
            player -> player.isSleeping()
            && player.getSleepingPos().isPresent()
            && player.getSleepingPos().get().equals(this.pos)
    );
        if (players.isEmpty()) {
        Yaidensaddon.LOGGER.warn(
            "gay(): no sleeping player found at {}",
            this.pos
        );
        return;
    } else {
    Player player1 = players.get(0);

    EntityConfig config1 =
        EntityConfig.getEntity(player1);

    Gender gender1 =
        config1.getGender();


    // Generate the villager's gender
    Vilgendersystem genderSystem =
        new Vilgendersystem(npc);

    Gender villagerGender =
        genderSystem.randomez(level);


    // Check both genders
    if (!npc.isBaby()) {
        Yaidensaddon.LOGGER.info(
                "not baby",
                player.getName().getString()
            );
        if ((gender1 == Gender.MALE && villagerGender == Gender.FEMALE || gender1 == Gender.FEMALE && villagerGender == Gender.MALE)) {
                npcs();
            
    }
    } else {
        Yaidensaddon.LOGGER.info(
                "baby",
                player.getName().getString()
            );
        npc.isBaby();
    }
}
}}
