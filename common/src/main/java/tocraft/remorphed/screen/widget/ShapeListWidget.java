package tocraft.remorphed.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@Environment(EnvType.CLIENT)
public class ShapeListWidget extends ContainerObjectSelectionList<ShapeListWidget.ShapeRow> {
    public ShapeListWidget(Minecraft minecraft, int width, @NotNull HeaderAndFooterLayout layout) {
        this(minecraft, width, layout.getContentHeight(), layout.getHeaderHeight(), 35);
    }

    private ShapeListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    public int addRow(ShapeWidget[] widgets) {
        return addEntry(new ShapeRow(widgets));
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    public static class ShapeRow extends ContainerObjectSelectionList.Entry<ShapeRow> {
        private final ShapeWidget[] widgets;

        public ShapeRow(ShapeWidget[] widgets) {
            this.widgets = widgets;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float delta) {
            int y = top + height / 2;

            for (int i = 0; i < widgets.length; i++) {
                ShapeWidget widget = widgets[i];

                if (widget != null) {
                    int w = width / 5;

                    widget.setPosition(left + w * i, y);
                    widget.setSize(w, height);
                    widget.render(guiGraphics, mouseX, mouseY, delta);
                }
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(widgets);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of(widgets);
        }
    }
}
