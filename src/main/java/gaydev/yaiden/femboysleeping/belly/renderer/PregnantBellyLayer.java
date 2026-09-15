package gaydev.yaiden.femboysleeping.belly.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import gaydev.yaiden.femboysleeping.belly.pregnant;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.resources.ResourceLocation;

public class PregnantBellyLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final pregnant<AbstractClientPlayer> bellyModel;

    /*
     * Temporary belly-growth storage.
     *
     * Later this will be replaced with your synchronized
     * pregnancy data so every player sees the correct value.
     */
    private static final Map<UUID, Float> BELLY_GROWTH = new HashMap<>();

    public PregnantBellyLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            EntityModelSet modelSet
    ) {
        super(parent);

        this.bellyModel = new pregnant<>(
                modelSet.bakeLayer(pregnant.LAYER_LOCATION)
        );
    }

    /**
     * Set the belly growth for a player.
     *
     * 0.0 = normal
     * 4.8 = maximum
     */
    public static void setBellyGrowth(AbstractClientPlayer player, float growth) {
        growth = Math.max(0.0F, Math.min(growth, 4.8F));
        BELLY_GROWTH.put(player.getUUID(), growth);
    }

    /**
     * Get the current belly growth for a player.
     */
    public static float getBellyGrowth(AbstractClientPlayer player) {
        return BELLY_GROWTH.getOrDefault(player.getUUID(), 0.0F);
    }

    /**
     * Remove stored data for a player.
     */
    public static void clearBellyGrowth(AbstractClientPlayer player) {
        BELLY_GROWTH.remove(player.getUUID());
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        float growth = getBellyGrowth(player);

        // No belly if the player isn't pregnant / has no growth.
        if (growth <= 0.0F) {
            return;
        }

        /*
         * Use the player's actual skin.
         *
         * This means Player A's belly uses Player A's skin,
         * and other clients will also use Player A's skin.
         */
        ResourceLocation skinTexture = player.getSkin().texture();

        VertexConsumer vertexConsumer =
                buffer.getBuffer(RenderType.entityCutoutNoCull(skinTexture));

        poseStack.pushPose();

        /*
         * Attach the belly to the player's torso.
         *
         * Without this, the belly would stay aligned to the
         * player's world position instead of following body rotation.
         */
        this.getParentModel().body.translateAndRotate(poseStack);

        /*
         * Growth scaling.
         *
         * The belly gets:
         * - slightly wider
         * - slightly taller
         * - considerably deeper
         *
         * Maximum growth is 4.8.
         */
        float width = 1.0F + growth * 0.10F;
        float height = 1.0F + growth * 0.05F;
        float depth = 1.0F + growth * 0.20F;

        poseStack.scale(width, height, depth);

        /*
         * Animate the model.
         */
        bellyModel.setupAnim(
                player,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch
        );

        /*
         * Render using the player's skin texture.
         */
        bellyModel.renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                0xFFFFFFFF
        );

        poseStack.popPose();
    }
}
