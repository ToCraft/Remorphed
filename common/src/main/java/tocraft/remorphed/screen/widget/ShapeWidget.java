package tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
//#if MC>1194
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import tocraft.craftedcore.patched.client.CGraphics;
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
    //#if MC>1194
    protected abstract void renderShape(GuiGraphics guiGraphics);
    //#else
    //$$ protected abstract void renderShape(PoseStack guiGraphics);
    //#endif

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
    //#if MC>1194
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        //#elseif MC>1182
        //$$ public void renderWidget(PoseStack guiGraphics, int mouseX, int mouseY, float delta) {
        //#else
        //$$ public void renderButton(PoseStack guiGraphics, int mouseX, int mouseY, float delta) {
        //#endif
        if (!crashed) {
            renderShape(guiGraphics);

            // Render selected outline
            if (isCurrent) {
                CGraphics.blit(guiGraphics, Remorphed.id("textures/gui/selected.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
            }
            // Render favorite
            if (isFavorite) {
                CGraphics.blit(guiGraphics, Remorphed.id("textures/gui/favorite.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
            }
        }
    }

    @Override
    public void onPress() {

    }

    //#if MC>1182
    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
    //#else
    //$$ @Override
    //$$ public void updateNarration(NarrationElementOutput narrationElementOutput) {
    //$$
    //$$ }
    //$$ public int getX() {
    //$$     return x;
    //$$ }
    //$$ public int getY() {
    //$$     return y;
    //$$ }
    //#endif
}
