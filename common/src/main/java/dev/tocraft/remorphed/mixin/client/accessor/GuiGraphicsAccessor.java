package dev.tocraft.remorphed.mixin.client.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {
    @Accessor
    GuiRenderState getGuiRenderState();
    @Accessor
    GuiGraphics.ScissorStack getScissorStack();
}
