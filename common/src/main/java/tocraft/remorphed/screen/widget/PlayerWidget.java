package tocraft.remorphed.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.impl.PlayerDataProvider;
import tocraft.walkers.network.impl.SwapPackets;

public class PlayerWidget extends AbstractButton {
    private final RemorphedScreen parent;

    public PlayerWidget(int x, int y, int width, int height, RemorphedScreen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;
    }

    @Override
    public void renderButton(PoseStack guiGraphics, int mouseX, int mouseY, float delta) {
        if (Minecraft.getInstance().player != null) {
            ResourceLocation skinLocation = Minecraft.getInstance().player.getSkinTextureLocation();
            RenderSystem.setShaderTexture(0, skinLocation);
            GuiComponent.blit(guiGraphics, x, y, getWidth(), getHeight(), 8.0f, 8, 8, 8, 64, 64);
            GuiComponent.blit(guiGraphics, x, y, getWidth(), getHeight(), 40.0f, 8, 8, 8, 64, 64);
        } else
            super.renderButton(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void onPress() {
        if (Minecraft.getInstance().player != null && ((PlayerDataProvider) Minecraft.getInstance().player).walkers$getCurrentShape() != null) {
            SwapPackets.sendSwapRequest();
            parent.onClose();
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }
}
