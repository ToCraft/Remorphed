package dev.tocraft.remorphed.screen.widget;

import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.walkers.Walkers;
import dev.tocraft.walkers.network.impl.SwapPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public abstract class ShapeWidget extends AbstractButton {
    private final Screen parent;
    private boolean crashed = false;
    private boolean isFavorite;
    private final boolean isCurrent;
    private int availability;

    public ShapeWidget(float x, float y, float width, float height, Screen parent, boolean isFavorite, boolean isCurrent, int availability) {
        super((int) x, (int) y, (int) width, (int) height, Component.nullToEmpty("WOLF"));
        this.parent = parent;
        this.isFavorite = isFavorite;
        this.isCurrent = isCurrent;
        this.availability = availability;
    }

    protected abstract void sendFavoriteRequest(boolean isFavorite);

    protected abstract void sendSwap2ndShapeRequest();

    protected abstract void extractShape(GuiGraphicsExtractor guiGraphics);

    protected void setCrashed() {
        this.crashed = true;
    }

    private void deletePoint() {
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        if (this.availability > 0) {
            this.availability--;
            sendDeleteShapePacket();
        } else {
            // reopen screen to avoid negative shape availability. Do so after half a second so the server can process the loss
            if (isCurrent) {
                SwapPackets.sendSwapRequest();
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Add to favorites
        if (active && visible && isHovered() && Minecraft.getInstance().player != null) {
            int button = event.button();
            if (button == 1) {
                isFavorite = !isFavorite;
                sendFavoriteRequest(isFavorite);
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            } else if (button == 2 && availability != -1) {
                deletePoint();
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        if (!crashed) {
            // make the widget is even DARKER when hovered
            if (isHoveredOrFocused()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Remorphed.id("textures/gui/focused.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32, 48, 32);
            }

            // render availability counter
            if (Remorphed.displayDataInMenu && availability > 0) {
                String s = String.valueOf(availability);
                int w = parent.getFont().width(s);
                guiGraphics.text(parent.getFont(), s, getX() + getWidth() - w - getWidth() / 8, (int) (getY() + getHeight() * 0.125), -1, false);
            } else if (availability == 0) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Remorphed.id("textures/gui/deleted.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32, 48, 32);
            }

            extractShape(guiGraphics);

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
    public void onPress(@NotNull InputWithModifiers input) {
        // switch to new shape
        if (!isCurrent) {
            // Update 2nd Shape
            sendSwap2ndShapeRequest();
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            // close active screen handler
            parent.onClose();
        }
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        if (this.active && this.visible && isHovered() && event.key() == GLFW.GLFW_KEY_X && availability != -1) {
            deletePoint();
            return true;
        }
        return super.keyPressed(event);
    }

    abstract void sendDeleteShapePacket();

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
