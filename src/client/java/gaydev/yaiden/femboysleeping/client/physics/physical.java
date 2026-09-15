package gaydev.yaiden.femboysleeping.client.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public class physical {
    void idk(LivingEntity entity, EntityConfig config, IGenderArmor armor) {
    BreastPhysics physics = new BreastPhysics(config);

    if (entity instanceof Player player) {
        physics.update(player, armor);

        float y = physics.getPositionY();
        float x = physics.getPositionX();
        float rotation = physics.getBounceRotation();
        

    };
    

    }

}
