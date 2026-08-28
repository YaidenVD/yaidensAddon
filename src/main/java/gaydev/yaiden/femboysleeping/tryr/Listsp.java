package gaydev.yaiden.femboysleeping.tryr;

import java.util.List;
import com.wildfire.main.Gender;
import com.wildfire.main.entitydata.EntityConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;


public class Listsp {
    
    public void idk(Level level, BlockPos pos) {
        List<Player> players = level.getEntitiesOfClass(
    Player.class,
    new AABB(pos).inflate(4.0),
    player -> player.isSleeping()
        && player.getSleepingPos().isPresent()
        && player.getSleepingPos().get().equals(pos)
);


if (players.size() == 2) {
    Player player1 = players.get(0);
    Player player2 = players.get(1);
    EntityConfig config1 = EntityConfig.getEntity(player1);
    EntityConfig config2 = EntityConfig.getEntity(player2);
    if (config1 != null && config2 != null) {
        Gender gender1 = config1.getGender();
        Gender gender2 = config2.getGender();
        if (gender1 == Gender.FEMALE && gender2 == Gender.MALE || gender1 == Gender.MALE && gender2 == Gender.FEMALE) {
            
        }
    }
}
    }
}