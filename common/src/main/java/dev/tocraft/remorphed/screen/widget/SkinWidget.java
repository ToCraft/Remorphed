package dev.tocraft.remorphed.screen.widget;

import com.mojang.authlib.GameProfile;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.network.NetworkHandler;
import dev.tocraft.walkers.api.PlayerShape;
import dev.tocraft.walkers.network.impl.SwapPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class SkinWidget extends ShapeWidget {
    private final GameProfile skinProfile;
    private final int size;
    private final PlayerSkin playerSkin;
    private final PlayerModel model;

    public SkinWidget(int x, int y, int width, int height, @NotNull GameProfile skinProfile, PlayerSkin playerSkin, PlayerModel model, Screen parent, boolean isFavorite, boolean isCurrent, int availability) {
        super(x, y, width, height, parent, isFavorite, isCurrent, availability);
        this.size = (int) (Remorphed.CONFIG.entity_size * (1 / (Math.max(EntityType.PLAYER.getHeight(), EntityType.PLAYER.getWidth()))));
        setTooltip(Tooltip.create(Component.literal(skinProfile.name())));
        this.skinProfile = skinProfile;
        this.playerSkin = playerSkin;
        this.model = model;
    }

    @Override
    protected void sendFavoriteRequest(boolean isFavorite) {
        NetworkHandler.sendFavoriteRequest(skinProfile, isFavorite);
    }

    @Override
    protected void sendSwap2ndShapeRequest() {
        NetworkHandler.sendSwapSkinRequest(skinProfile);
        Player player = Minecraft.getInstance().player;
        if (player != null && PlayerShape.getCurrentShape(player) != null) {
            SwapPackets.sendSwapRequest();
        }
    }

    @Override
    protected void extractShape(GuiGraphicsExtractor guiGraphics) {
        PlayerSkin skin = this.playerSkin;
        if (skin != null) {
            int leftPos = (int) (getX() + (float) this.getWidth() / 2);
            int topPos = getY();
            int k = leftPos - 20;
            int l = topPos - 35;
            int m = leftPos + 20;
            int n = topPos + 25;
            guiGraphics.skin(model, skin.body().texturePath(), size, -5, 30, -1.0625F, k, l, m, n);
        }
    }

    @Override
    void sendDeleteShapePacket() {
        NetworkHandler.sendDeleteShapePacket(skinProfile.id());
    }
}
