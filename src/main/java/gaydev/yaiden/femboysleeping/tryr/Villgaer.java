package gaydev.yaiden.femboysleeping.tryr;

import java.util.List;

import com.wildfire.main.Gender;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
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

    private BlockPos pos;
    private Level level;
    public static Villager npc;
    public Player player;

    public Villgaer(Level level, BlockPos pos, Villager npc, Player player) {
        this.level = level;
        this.pos = pos;
        Villgaer.npc = npc;
        this.player = player;
    }


public void gay() {

    List<Player> players = this.level.getEntitiesOfClass(
        Player.class,
        new AABB(this.pos).inflate(4.0),
        player -> player.isSleeping()
            && player.getSleepingPos().isPresent()
            && player.getSleepingPos().get().equals(this.pos)
    );

    
}


    public void gaypc() {
        Villager villager =
            EntityType.VILLAGER.create(level);
        

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
                "Villager spawned for {}",
                player.getName().getString()
            );
        }
        }
}