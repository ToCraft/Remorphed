package tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LongTextWidget extends AbstractScrollArea {
    private final List<MultiLineTextWidget> text = new ArrayList<>();

    public LongTextWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.nullToEmpty(""));
    }

    @Override
    protected int contentHeight() {
        int i = 4;

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
        return 330;
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

        int y = getY() - (int) scrollAmount();
        for (MultiLineTextWidget widget : this.text) {
            widget.setPosition(this.getX() + (getWidth() - textWidth()) / 2, y);
            widget.setMaxWidth(textWidth());
            widget.render(guiGraphics, mouseX, mouseY, delta);

            y += widget.getHeight();
        }

        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }
}
