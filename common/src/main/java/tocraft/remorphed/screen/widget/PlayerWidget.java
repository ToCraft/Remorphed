package tocraft.remorphed.screen.widget;

import dev.tocraft.skinshifter.SkinShifter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tocraft.craftedcore.platform.PlayerProfile;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.network.impl.SwapPackets;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class PlayerWidget extends AbstractButton {
    private final Screen parent;
    private boolean willCache = true;

    public PlayerWidget(int x, int y, int width, int height, Screen parent) {
        super(x, y, width, height, Component.nullToEmpty(""));
        this.parent = parent;
        setTooltip(Tooltip.create(Component.translatable("remorphed.player_icon")));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ResourceLocation skinLocation = player.getSkin().texture();
            if (Remorphed.foundSkinShifter && !player.getUUID().equals(SkinShifter.getCurrentSkin(player))) {
                // still render own skin as icon when in another skin
                PlayerProfile playerProfile = PlayerProfile.getCachedProfile(player.getUUID());
                if (playerProfile != null && playerProfile.skin() != null) {
                    skinLocation = playerProfile.getSkinId();
                } else if (this.willCache) {
                    // cache profile asynchronous
                    CompletableFuture.runAsync(() -> PlayerProfile.ofId(player.getUUID()));
                    this.willCache = false;
                }
            }

            guiGraphics.blit(RenderType::guiTextured, skinLocation, getX(), getY(), 8.0f, 8, getWidth(), getHeight(), 8, 8, 64, 64);
            guiGraphics.blit(RenderType::guiTextured, skinLocation, getX(), getY(), 40.0f, 8, getWidth(), getHeight(), 8, 8, 64, 64);
        } else {
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void onPress() {
        if (Minecraft.getInstance().player != null) {
            if (PlayerShape.getCurrentShape(Minecraft.getInstance().player) != null) {
                SwapPackets.sendSwapRequest();
                parent.onClose();
            }
            if (Remorphed.foundSkinShifter && SkinShifter.getCurrentSkin(Minecraft.getInstance().player) != Minecraft.getInstance().player.getUUID()) {
                NetworkHandler.sendResetSkinPacket();
                parent.onClose();
            }
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
