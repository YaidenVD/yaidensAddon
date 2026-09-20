package gaydev.yaiden.femboysleeping.client.belly;

import gaydev.yaiden.femboysleeping.client.belly.renderer.PregnantBellyLayer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the belly model and attaches the belly layer to the player renderer(s).
 * Call BellyClientSetup.register() once from YaidensaddonClient.onInitializeClient().
 */
public final class BellyClientSetup {
    private static final Logger LOGGER = LoggerFactory.getLogger("yaidensaddon-belly");

    public BellyClientSetup() {}

    public static void register() {
        // 1) Tell the game how to build the model (this was missing - without it
        //    modelSet.bakeLayer(...) crashes / the layer can never exist).
        EntityModelLayerRegistry.registerModelLayer(Pregnant.LAYER_LOCATION, Pregnant::createBodyLayer);
        LOGGER.info("[belly] model layer registered");

        // 2) Add the layer to every player renderer (there is one for the wide arms
        //    skin and one for the slim arms skin).
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof PlayerRenderer playerRenderer) {
                        LOGGER.info("[belly] attaching belly layer to a player renderer");
                        registrationHelper.register(
                                new PregnantBellyLayer(playerRenderer, context.getModelSet()));
                    }
                });
    }
}
