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
import tocraft.remorphed.Remorphed;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@Environment(EnvType.CLIENT)
public class ShapeListWidget extends ContainerObjectSelectionList<ShapeListWidget.ShapeRow> {
    private static final int ITEM_HEIGHT = 35;

    public ShapeListWidget(Minecraft minecraft, int width, @NotNull HeaderAndFooterLayout layout) {
        super(minecraft, width, layout.getContentHeight(), layout.getHeaderHeight(), ITEM_HEIGHT);
    }

    public int addRow(ShapeWidget[] widgets) {
        return addEntry(new ShapeRow(widgets));
    }

    public int rowHeight() {
        return itemHeight;
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
            for (int i = 0; i < widgets.length; i++) {
                ShapeWidget widget = widgets[i];

                if (widget != null) {
                    int w = width / Remorphed.CONFIG.shapes_per_row;

                    widget.setPosition(left + w * i, top);
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

    @Override
    public int getRowWidth() {
        return Remorphed.CONFIG.row_width;
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    //#if MC<=1212
    //$$ @Override
    //$$ protected boolean isValidMouseClick(int button) {
    //$$     return true;
    //$$ }
    //#endif
}
