package tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.impl.PlayerDataProvider;
import tocraft.walkers.network.impl.SwapPackets;

public class PlayerWidget extends AbstractButton {
    private final RemorphedScreen parent;

    public PlayerWidget(int x, int y, int width, int height, RemorphedScreen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;
        setTooltip(Tooltip.create(Component.translatable("remorphed.player_icon")));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (Minecraft.getInstance().player != null) {
            ResourceLocation skinLocation = Minecraft.getInstance().player.getSkin().texture();
            guiGraphics.blit(skinLocation, getX(), getY(), getWidth(), getHeight(), 8.0f, 8, 8, 8, 64, 64);
            guiGraphics.blit(skinLocation, getX(), getY(), getWidth(), getHeight(), 40.0f, 8, 8, 8, 64, 64);
        } else
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void onPress() {
        if (Minecraft.getInstance().player != null && ((PlayerDataProvider) Minecraft.getInstance().player).walkers$getCurrentShape() != null) {
            SwapPackets.sendSwapRequest();
            parent.onClose();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
