package tocraft.remorphed.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.variant.ShapeType;
import tocraft.walkers.network.impl.SpecialSwapPackets;

@Environment(EnvType.CLIENT)
public class SpecialShapeWidget extends AbstractButton {
    private final RemorphedScreen parent;
    private final boolean isCurrent;
    private final boolean isAvailable;

    public SpecialShapeWidget(int x, int y, int width, int height, RemorphedScreen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;

        // check if current shape is the special shape
        CompoundTag nbt = new CompoundTag();
        if (Minecraft.getInstance().player != null && PlayerShape.getCurrentShape(Minecraft.getInstance().player) instanceof Wolf wolf) wolf.saveWithoutId(nbt);
        this.isCurrent = nbt.contains("isSpecial") && nbt.getBoolean("isSpecial");
        this.isAvailable = Walkers.CONFIG.specialShapeIsThirdShape || Minecraft.getInstance().player.isCreative() || ((RemorphedPlayerDataProvider) Minecraft.getInstance().player).remorphed$getUnlockedShapes().keySet().stream().map(ShapeType::getEntityType).toList().contains(EntityType.WOLF);

        setTooltip(Tooltip.create(Component.translatable(isAvailable ? "remorphed.special_shape_available" : "remorphed.special_shape_unavailable")));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(Remorphed.id("textures/gui/wolf.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 15, 15, 15, 15);

        if (!isCurrent && !isAvailable)
            guiGraphics.blit(Remorphed.id("textures/gui/unavailable.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 15, 15, 15, 15);
    }

    @Override
    public void onPress() {
        if (!isCurrent && isAvailable && Walkers.hasSpecialShape(Minecraft.getInstance().getUser().getProfileId())) {
            if (!Walkers.CONFIG.specialShapeIsThirdShape)
                NetworkHandler.sendSwap2ndShapeRequest(ShapeType.from(EntityType.WOLF, -1));
            SpecialSwapPackets.sendSpecialSwapRequest();
            parent.onClose();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
