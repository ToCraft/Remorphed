package dev.tocraft.remorphed.screen.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class GuiShapeRenderer extends PictureInPictureRenderer<GuiShapeRenderState> {
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final int id;

    public GuiShapeRenderer(int id, BufferSource bufferSource, EntityRenderDispatcher entityRenderDispatcher) {
        super(bufferSource);
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.id = id;
    }

    @Override
    public @NotNull Class<GuiShapeRenderState> getRenderStateClass() {
        return GuiShapeRenderState.class;
    }

    protected void renderToTexture(@NotNull GuiShapeRenderState state, @NotNull PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        Vector3f vector3f = state.translation();
        poseStack.translate(vector3f.x, vector3f.y, vector3f.z);
        poseStack.mulPose(state.rotation());
        Quaternionf quaternionf = state.overrideCameraAngle();
        if (quaternionf != null) {
            this.entityRenderDispatcher.overrideCameraOrientation(quaternionf.conjugate(new Quaternionf()).rotateY((float) Math.PI));
        }

        this.entityRenderDispatcher.setRenderShadow(false);
        this.entityRenderDispatcher.render(state.renderState(), 0.0, 0.0, 0.0, poseStack, this.bufferSource, 15728880);
        this.entityRenderDispatcher.setRenderShadow(true);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected @NotNull String getTextureLabel() {
        return String.format("walkers: entity %s", id);
    }
}
