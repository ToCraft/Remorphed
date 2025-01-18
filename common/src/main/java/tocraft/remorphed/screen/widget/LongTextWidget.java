package tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import tocraft.remorphed.Remorphed;

import java.util.ArrayList;
import java.util.List;

public class LongTextWidget extends AbstractScrollArea {
    protected final List<MultiLineTextWidget> text = new ArrayList<>();
    protected final boolean separators;

    public LongTextWidget(int x, int y, int width, int height, boolean separators) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.separators = separators;
    }

    @Override
    protected int contentHeight() {
        int i = 8; // 4 space below and on top of the text

        for (MultiLineTextWidget widget : text) {
             i+= widget.getHeight();
        }

        return i;
    }

    public void addText(Component text) {
        this.addText(text, Minecraft.getInstance().font, -1);
    }

    public void addText(Component text, Font font, int color) {
        this.text.add(new MultiLineTextWidget(text, font).setColor(color));
    }

    protected int textWidth() {
        return Remorphed.CONFIG.row_width;
    }

    @Override
    protected double scrollRate() {
        return (double) contentHeight() / text.size() / 2;
    }

    @Override
    protected int scrollBarX() {
        return super.scrollBarX() - (getWidth() - textWidth()) / 2;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());

        int x = this.getX() + (getWidth() - textWidth()) / 2;
        int y = getY() - (int) scrollAmount() + 4; // 4 space on top
        for (MultiLineTextWidget widget : this.text) {
            widget.setPosition(x, y);
            widget.setMaxWidth(textWidth());
            widget.render(guiGraphics, mouseX, mouseY, delta);

            y += widget.getHeight();
        }

        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics);

        if (separators) {
            renderSeparators(guiGraphics);
        }
    }


    protected void renderSeparators(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderType::guiTextured, Screen.INWORLD_HEADER_SEPARATOR, this.getX(), this.getY() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
        guiGraphics.blit(RenderType::guiTextured, Screen.INWORLD_FOOTER_SEPARATOR, this.getX(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
    }

    public void updateSize(int width, HeaderAndFooterLayout layout) {
        this.updateSizeAndPosition(width, layout.getContentHeight(), layout.getHeaderHeight());
    }

    public void updateSizeAndPosition(int width, int height, int y) {
        this.setSize(width, height);
        this.setPosition(0, y);
        this.refreshScrollAmount();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        MutableComponent narration = Component.empty();
        for (MultiLineTextWidget widget : text) {
            narration.append(widget.getMessage());
        }
        narrationElementOutput.add(NarratedElementType.TITLE, narration);
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }
}
