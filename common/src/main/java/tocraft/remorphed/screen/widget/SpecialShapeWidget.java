package tocraft.remorphed.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.jetbrains.annotations.NotNull;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.variant.ShapeType;
import tocraft.walkers.api.variant.TypeProvider;
import tocraft.walkers.api.variant.TypeProviderRegistry;
import tocraft.walkers.network.impl.SwapVariantPackets;

import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class SpecialShapeWidget extends AbstractButton {
    private final Screen parent;
    private final boolean isCurrent;
    private final boolean isAvailable;

    public SpecialShapeWidget(int x, int y, int width, int height, Screen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;

        // check if current shape is the special shape
        CompoundTag nbt = new CompoundTag();
        if (Minecraft.getInstance().player != null && PlayerShape.getCurrentShape(Minecraft.getInstance().player) instanceof Wolf wolf) {
            wolf.saveWithoutId(nbt);
        }
        this.isCurrent = nbt.getBooleanOr("isSpecial", false);
        this.isAvailable = Minecraft.getInstance().player != null && Remorphed.canUseShape(Minecraft.getInstance().player, EntityType.WOLF);
        setTooltip(Tooltip.create(Component.translatable(isAvailable ? "remorphed.special_shape_available" : "remorphed.special_shape_unavailable")));
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderType::guiTextured, Remorphed.id("textures/gui/wolf.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 15, 15, 15, 15);

        if (!isCurrent && !isAvailable) {
            guiGraphics.blit(RenderType::guiTextured, Remorphed.id("textures/gui/unavailable.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 15, 15, 15, 15);
        }

        // Highlight when focused
        if (isHoveredOrFocused()) {
            guiGraphics.blit(RenderType::guiTextured, Remorphed.id("textures/gui/head_focus.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 16, 16, 16, 16);
        }
    }

    @Override
    public void onPress() {
        UUID profileId = Minecraft.getInstance().getUser().getProfileId();
        if (!isCurrent && isAvailable && Walkers.hasSpecialShape(profileId)) {
            // get variant range
            TypeProvider<Wolf> typeProvider = TypeProviderRegistry.getProvider(EntityType.WOLF);
            int range = typeProvider != null ? typeProvider.size(Minecraft.getInstance().level) : -1;
            // swap to variant
            NetworkHandler.sendSwap2ndShapeRequest(Objects.requireNonNull(ShapeType.from(EntityType.WOLF, -1)));
            SwapVariantPackets.sendSwapRequest(range);
            parent.onClose();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
