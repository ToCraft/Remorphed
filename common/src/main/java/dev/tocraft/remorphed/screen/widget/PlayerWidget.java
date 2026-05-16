package dev.tocraft.remorphed.screen.widget;

import com.mojang.authlib.GameProfile;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.mixin.client.accessor.ClientPlayerAccessor;
import dev.tocraft.remorphed.network.NetworkHandler;
import dev.tocraft.skinshifter.SkinShifter;
import dev.tocraft.skinshifter.data.SkinPlayerData;
import dev.tocraft.walkers.api.PlayerShape;
import dev.tocraft.walkers.network.impl.SwapPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class PlayerWidget extends AbstractButton {
    private final Screen parent;
    private final boolean willCache = true;
    private final Identifier skinId;

    public PlayerWidget(int x, int y, int width, int height, Screen parent) {
        super(x, y, width, height, Component.nullToEmpty("Head"));
        this.parent = parent;
        ClientPlayerAccessor accessor = ((ClientPlayerAccessor) Minecraft.getInstance().player);
        if (accessor != null) {
            this.skinId = accessor.callGetPlayerInfo().getSkin().body().texturePath();
        } else {
            this.skinId = null;
        }
        setTooltip(Tooltip.create(Component.translatable("remorphed.player_icon")));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        if (skinId != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinId, getX(), getY(), 8.0f, 8, getWidth(), getHeight(), 8, 8, 64, 64);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinId, getX(), getY(), 40.0f, 8, getWidth(), getHeight(), 8, 8, 64, 64);
        }

        // Highlight when focused
        if (isHoveredOrFocused()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Remorphed.id("textures/gui/head_focus.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 16, 16, 16, 16);
        }
    }

    @Override
    public void onPress(@NotNull InputWithModifiers input) {
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

    @SuppressWarnings("UnstableApiUsage")
    private static Optional<CompletableFuture<Optional<PlayerSkin>>> getPlayerSkin(Player player) {
        Optional<GameProfile> profileFuture = SkinPlayerData.getSkinProfile(player);
        return profileFuture.map(gameProfile -> Minecraft.getInstance().getSkinManager().get(gameProfile));
    }
}
