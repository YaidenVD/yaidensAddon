package gaydev.yaiden.femboysleeping.client.belly.renderer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import gaydev.yaiden.femboysleeping.client.belly.Pregnant;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PregnantBellyLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final Logger LOGGER = LoggerFactory.getLogger("yaidensaddon-belly");
    private static boolean loggedFirstRender = false;

    public static final float MAX_GROWTH = 4.8F;

    /**
     * Belly growth per player (0.0 = none, 4.8 = maximum).
     * Later this gets filled from a server -> client packet; for now the F12 test key sets it.
     */
    private static final Map<UUID, Float> BELLY_GROWTH = new ConcurrentHashMap<>();

    private final Pregnant<AbstractClientPlayer> bellyModel;

    public PregnantBellyLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            EntityModelSet modelSet) {
        super(parent);
        this.bellyModel = new Pregnant<>(modelSet.bakeLayer(Pregnant.LAYER_LOCATION));
        LOGGER.info("[belly] layer created (model baked OK)");
    }

    public static void setBellyGrowth(AbstractClientPlayer player, float growth) {
        growth = Math.max(0.0F, Math.min(growth, MAX_GROWTH));
        if (growth <= 0.0F) {
            BELLY_GROWTH.remove(player.getUUID());
        } else {
            BELLY_GROWTH.put(player.getUUID(), growth);
        }
    }

    public static float getBellyGrowth(AbstractClientPlayer player) {
        return BELLY_GROWTH.getOrDefault(player.getUUID(), 0.0F);
    }

    public static void clearBellyGrowth(AbstractClientPlayer player) {
        BELLY_GROWTH.remove(player.getUUID());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        float growth = getBellyGrowth(player);
        if (growth <= 0.0F || player.isInvisible()) {
            return;
        }

        if (!loggedFirstRender) {
            loggedFirstRender = true;
            LOGGER.info("[belly] render() reached with growth={} for {}", growth, player.getName().getString());
        }

        // The belly uses the player's own skin, so it matches for every viewer.
        VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityCutoutNoCull(player.getSkin().texture()));

        poseStack.pushPose();

        // Follow the torso (rotation while sneaking, swimming, sleeping, ...).
        this.getParentModel().body.translateAndRotate(poseStack);

        // Scale around the middle of the belly's BACK edge (y = 9, z = +2, in pixels)
        // so it grows outward/forward instead of drifting away from the torso.
        // (Your old code scaled around the torso origin, which pushed the belly down.)
        float width = 1.0F + growth * 0.10F;
        float height = 1.0F + growth * 0.05F;
        float depth = 1.0F + growth * 0.20F;

        poseStack.translate(0.0F, 9.0F / 16.0F, 2.0F / 16.0F);
        poseStack.scale(width, height, depth);
        poseStack.translate(0.0F, -9.0F / 16.0F, -2.0F / 16.0F);

        bellyModel.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                0xFFFFFFFF);

        poseStack.popPose();
    }
}
