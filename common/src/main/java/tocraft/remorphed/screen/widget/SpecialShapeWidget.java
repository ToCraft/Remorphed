package tocraft.remorphed.screen.widget;

import com.mojang.util.UUIDTypeAdapter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
//#if MC>=1201
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif
import net.minecraft.client.gui.components.AbstractButton;
//#if MC>1182
import net.minecraft.client.gui.components.Tooltip;
//#endif
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import tocraft.craftedcore.patched.TComponent;
import tocraft.craftedcore.patched.client.CGraphics;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.remorphed.screen.RemorphedScreen;
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
    private final RemorphedScreen parent;
    private final boolean isCurrent;
    private final boolean isAvailable;

    public SpecialShapeWidget(int x, int y, int width, int height, RemorphedScreen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;

        // check if current shape is the special shape
        CompoundTag nbt = new CompoundTag();
        if (Minecraft.getInstance().player != null && PlayerShape.getCurrentShape(Minecraft.getInstance().player) instanceof Wolf wolf)
            wolf.saveWithoutId(nbt);
        this.isCurrent = nbt.contains("isSpecial") && nbt.getBoolean("isSpecial");
        this.isAvailable = Remorphed.canUseEveryShape(Minecraft.getInstance().player) || (PlayerMorph.getUnlockedShapes(Minecraft.getInstance().player)).keySet().stream().anyMatch(type -> type.getEntityType().equals(EntityType.WOLF));
        //#if MC>1182
        setTooltip(Tooltip.create(Component.translatable(isAvailable ? "remorphed.special_shape_available" : "remorphed.special_shape_unavailable")));
        //#endif
    }

    @Override
    //#if MC>1194
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    //#elseif MC>1182
    //$$ public void renderWidget(PoseStack guiGraphics, int mouseX, int mouseY, float delta) {
    //#else
    //$$ public void renderButton(PoseStack guiGraphics, int mouseX, int mouseY, float delta) {
    //#endif
        CGraphics.blit(guiGraphics, Remorphed.id("textures/gui/wolf.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 15, 15, 15, 15);

        if (!isCurrent && !isAvailable)
            CGraphics.blit(guiGraphics, Remorphed.id("textures/gui/unavailable.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 15, 15, 15, 15);
    }

    @Override
    public void onPress() {
        //#if MC>1182
        UUID profileId = Minecraft.getInstance().getUser().getProfileId();
        //#else
        //$$ UUID profileId = UUIDTypeAdapter.fromString(Minecraft.getInstance().getUser().getUuid());
        //#endif
        if (!isCurrent && isAvailable && Walkers.hasSpecialShape(profileId)) {
            // get variant range
            TypeProvider<Wolf> typeProvider = TypeProviderRegistry.getProvider(EntityType.WOLF);
            int range = typeProvider != null ? typeProvider.getRange() : -1;
            // swap to variant
            NetworkHandler.sendSwap2ndShapeRequest(Objects.requireNonNull(ShapeType.from(EntityType.WOLF, -1)));
            SwapVariantPackets.sendSwapRequest(range + 1);
            parent.onClose();
        }
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
    //$$
    //$$ public int getX() {
    //$$     return x;
    //$$ }
    //$$
    //$$ public int getY() {
    //$$     return y;
    //$$ }
    //$$
    //$$ @Override
    //$$ public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
    //$$     Screen currentScreen = Minecraft.getInstance().screen;
    //$$
    //$$     if (currentScreen != null) {
    //$$         currentScreen.renderTooltip(poseStack,  TComponent.translatable(isAvailable ? "remorphed.special_shape_available" : "remorphed.special_shape_unavailable"), mouseX, mouseY);
    //$$     }
    //$$ }
    //#endif
}
