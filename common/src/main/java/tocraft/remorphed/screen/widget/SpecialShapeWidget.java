package tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Wolf;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.network.impl.SpecialSwapPackets;

public class SpecialShapeWidget extends AbstractButton {
    private final RemorphedScreen parent;
    private final boolean isCurrent;

    public SpecialShapeWidget(int x, int y, int width, int height, RemorphedScreen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;

        // check if current shape is the special shape
        CompoundTag nbt = new CompoundTag();
        if (PlayerShape.getCurrentShape(Minecraft.getInstance().player) instanceof Wolf wolf)
            wolf.saveWithoutId(nbt);
        this.isCurrent = nbt.contains("isSpecial") && nbt.getBoolean("isSpecial");
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(Remorphed.id("textures/gui/wolf.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 15, 15, 15, 15);
        // Render selected outline
        if (isCurrent)
            guiGraphics.blit(Remorphed.id("textures/gui/selected.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
    }

    @Override
    public void onPress() {
        if (!isCurrent && Walkers.hasSpecialShape(Minecraft.getInstance().getUser().getProfileId())) {
            SpecialSwapPackets.sendSpecialSwapRequest();
            parent.onClose();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
