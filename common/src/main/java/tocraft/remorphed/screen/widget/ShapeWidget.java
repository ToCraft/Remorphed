package tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.screen.RemorphedScreen;

public abstract class ShapeWidget extends AbstractButton {
    private final RemorphedScreen parent;
    private boolean crashed = false;
    private boolean isFavorite;
    private final boolean isCurrent;

    public ShapeWidget(float x, float y, float width, float height, RemorphedScreen parent, boolean isFavorite, boolean isCurrent) {
        super((int) x, (int) y, (int) width, (int) height, Component.nullToEmpty(""));
        this.parent = parent;
        this.isFavorite = isFavorite;
        this.isCurrent = isCurrent;
    }

    protected abstract void sendFavoriteRequest(boolean isFavorite);

    protected abstract void sendSwap2ndShapeRequest();

    protected abstract void renderShape(GuiGraphics guiGraphics);

    protected void setCrashed() {
        this.crashed = true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
        if (bl && Minecraft.getInstance().player != null) {
            // switch to new shape
            if (button == 0 && !isCurrent) {
                // Update 2nd Shape
                sendSwap2ndShapeRequest();
                // close active screen handler
                parent.onClose();
            }
            // Add to favorites
            else if (button == 1) {
                isFavorite = !isFavorite;
                sendFavoriteRequest(isFavorite);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (!crashed) {
            renderShape(guiGraphics);

            // Render selected outline
            if (isCurrent) {
                guiGraphics.blit(RenderType::guiTextured, Remorphed.id("textures/gui/selected.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32, 48, 32);
            }
            // Render favorite
            if (isFavorite) {
                guiGraphics.blit(RenderType::guiTextured, Remorphed.id("textures/gui/favorite.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32, 48, 32);
            }
        }
    }

    @Override
    public void onPress() {

    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
