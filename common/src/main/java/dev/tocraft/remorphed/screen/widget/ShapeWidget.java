package dev.tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import dev.tocraft.remorphed.Remorphed;

public abstract class ShapeWidget extends AbstractButton {
    private final Screen parent;
    private boolean crashed = false;
    private boolean isFavorite;
    private final boolean isCurrent;
    private final int availability;

    public ShapeWidget(float x, float y, float width, float height, Screen parent, boolean isFavorite, boolean isCurrent, int availability) {
        super((int) x, (int) y, (int) width, (int) height, Component.nullToEmpty(""));
        this.parent = parent;
        this.isFavorite = isFavorite;
        this.isCurrent = isCurrent;
        this.availability = availability;
    }

    protected abstract void sendFavoriteRequest(boolean isFavorite);

    protected abstract void sendSwap2ndShapeRequest();

    protected abstract void renderShape(GuiGraphics guiGraphics);

    protected void setCrashed() {
        this.crashed = true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Add to favorites
        if (isHovered() && Minecraft.getInstance().player != null && button == 1) {
            isFavorite = !isFavorite;
            sendFavoriteRequest(isFavorite);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (!crashed) {
            // make the widget is even DARKER when hovered
            if (isHoveredOrFocused()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Remorphed.id("textures/gui/focused.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32, 48, 32);
            }

            if (Remorphed.displayDataInMenu && availability != -1) {
                String s = String.valueOf(availability);
                int w = parent.getFont().width(s);
                guiGraphics.drawString(parent.getFont(), s, getX() + getWidth() - w - getWidth() / 8, (int) (getY() + getHeight() * 0.125), 0xFFFFFF, false);
            }

            renderShape(guiGraphics);

            // Render selected outline
            if (isCurrent) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Remorphed.id("textures/gui/selected.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32, 48, 32);
            }
            // Render favorite
            if (isFavorite) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Remorphed.id("textures/gui/favorite.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32, 48, 32);
            }
        }
    }

    @Override
    public void onPress() {
        // switch to new shape
        if (!isCurrent) {
            // Update 2nd Shape
            sendSwap2ndShapeRequest();
            // close active screen handler
            parent.onClose();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
