package gaydev.yaiden.femboysleeping.client.belly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * The belly bump, drawn on top of the player's torso using the player's own skin.
 *
 * Coordinates are in the same space as PlayerModel.body (origin = top-centre of
 * the torso, y grows downward, the torso front faces -z). The torso is
 * x -4..4, y 0..12, z -2..2, so this slab covers the lower half of it.
 *
 * UV: texOffs(17, 22) with a 6x6x4 box puts the FRONT face on skin pixels
 * (21..27, 26..32) - the lower-middle of the skin's torso front - so the belly
 * matches the skin instead of sampling random parts of the texture.
 */
public class Pregnant<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("yaidensaddon", "pregnant"), "main");

    private final ModelPart belly;
    private final ModelPart bellyBottom;

    public Pregnant(ModelPart root) {
        this.belly = root.getChild("belly");
        this.bellyBottom = this.belly.getChild("belly_bottom");
    }

    public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();

    // Main belly
    PartDefinition belly = root.addOrReplaceChild(
            "belly",
            CubeListBuilder.create()
                    .texOffs(17, 22)
                    .addBox(
                            // x, y, z, width, height, depth
                            -3.0F, 6.0F, -2.0F,
                            6.0F, 6.0F, 4.0F,
                            new CubeDeformation(0.2F)
                    ),
            PartPose.offsetAndRotation(
                    0.0F, 0.0F, -2.0F,
                    -0.1309F, 0.0F, 0.0F
            )
    );

    // Bottom is now CHILD of belly
    belly.addOrReplaceChild(
            "belly_bottom",
            CubeListBuilder.create()
                    .texOffs(17, 22)
                    .addBox(
                            // x, y, z, width, height, depth
                            -3.0F, 9.18F, -2.0F,
                            6.0F, 0.04F, 4.0F,
                            new CubeDeformation(0.2F)
                    ),
            PartPose.offsetAndRotation(
                    0.0F, 0.0F, -2.0F,
                    -0.1309F, 0.0F, 0.0F
            )
    );

    return LayerDefinition.create(mesh, 64, 64);
}

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // The belly just follows the torso (see PregnantBellyLayer); nothing to animate.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
        belly.render(poseStack, buffer, packedLight, packedOverlay, color);
        bellyBottom.render(
            poseStack,
            buffer,
            packedLight,
            packedOverlay,
            color
    );
    }
}