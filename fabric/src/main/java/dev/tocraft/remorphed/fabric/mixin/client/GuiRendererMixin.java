package dev.tocraft.remorphed.fabric.mixin.client;

import dev.tocraft.remorphed.screen.render.GuiShapeRenderState;
import dev.tocraft.remorphed.screen.render.GuiShapeRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow
    @Final
    GuiRenderState renderState;

    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Unique
    private final Map<Integer, GuiShapeRenderer> walkers$shapeRenderer = new HashMap<>();

    // adds a new gui shape renderer per id and preps it
    @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
    public <T extends PictureInPictureRenderState> void getShapeRenderer(T state, int guiScale, CallbackInfo ci) {
        if (state instanceof GuiShapeRenderState shapeState) {
            GuiShapeRenderer renderer = this.walkers$shapeRenderer.computeIfAbsent(shapeState.id(), id -> new GuiShapeRenderer(id, bufferSource, Minecraft.getInstance().getEntityRenderDispatcher()));

            renderer.prepare(shapeState, renderState, guiScale);
            ci.cancel();
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void closeShapeRenderer(CallbackInfo ci) {
        this.walkers$shapeRenderer.values().forEach(PictureInPictureRenderer::close);
    }
}
