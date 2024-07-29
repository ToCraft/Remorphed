package tocraft.remorphed.screen.widget;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tocraft.craftedcore.patched.TComponent;
import tocraft.craftedcore.patched.client.CGraphics;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.api.variant.ShapeType;
import tocraft.walkers.impl.PlayerDataProvider;
import tocraft.walkers.network.impl.SwapPackets;

@Environment(EnvType.CLIENT)
public class PlayerWidget extends AbstractButton {
    private final RemorphedScreen parent;

    public PlayerWidget(int x, int y, int width, int height, RemorphedScreen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;
        //#if MC>1182
        setTooltip(Tooltip.create(Component.translatable("remorphed.player_icon")));
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
            if (Minecraft.getInstance().player != null) {
            //#if MC>1201
            ResourceLocation skinLocation = Minecraft.getInstance().player.getSkin().texture();
            //#else
            //$$ ResourceLocation skinLocation = Minecraft.getInstance().player.getSkinTextureLocation();
            //#endif
            CGraphics.blit(guiGraphics, skinLocation, getX(), getY(), getWidth(), getHeight(), 8.0f, 8, 8, 8, 64, 64);
                CGraphics.blit(guiGraphics, skinLocation, getX(), getY(), getWidth(), getHeight(), 40.0f, 8, 8, 8, 64, 64);
        } else {
            //#if MC>1182
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
            //#else
            //$$ super.renderButton(guiGraphics, mouseX, mouseY, delta);
            //#endif
        }
        //#if MC<=1182
        //$$ if (isHoveredOrFocused()) {
        //$$     renderToolTip(guiGraphics, mouseX, mouseY);
        //$$ }
        //#endif
    }

    @Override
    public void onPress() {
        if (Minecraft.getInstance().player != null && ((PlayerDataProvider) Minecraft.getInstance().player).walkers$getCurrentShape() != null) {
            SwapPackets.sendSwapRequest();
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
    //$$ public int getY() {
    //$$     return y;
    //$$ }
    //$$
    //$$ @Override
    //$$ public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
    //$$     Screen currentScreen = Minecraft.getInstance().screen;
    //$$
    //$$     if (currentScreen != null) {
    //$$         currentScreen.renderTooltip(poseStack,  TComponent.translatable("remorphed.player_icon"), mouseX, mouseY);
    //$$     }
    //$$ }
    //#endif
}
