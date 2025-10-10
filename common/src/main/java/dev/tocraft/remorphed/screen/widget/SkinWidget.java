package dev.tocraft.remorphed.screen.widget;

import com.mojang.authlib.GameProfile;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.RemorphedClient;
import dev.tocraft.remorphed.impl.FakeClientPlayer;
import dev.tocraft.remorphed.network.NetworkHandler;
import dev.tocraft.walkers.api.PlayerShape;
import dev.tocraft.walkers.network.impl.SwapPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SkinWidget extends ShapeWidget {
    private final GameProfile skin;
    private final FakeClientPlayer fakePlayer;
    private final int size;
    private final EntityRenderState cachedRenderState;

    public SkinWidget(int x, int y, int width, int height, @NotNull GameProfile skin, @NotNull FakeClientPlayer fakePlayer, Screen parent, boolean isFavorite, boolean isCurrent, int availability, @Nullable EntityRenderState cachedRenderState) {
        super(x, y, width, height, parent, isFavorite, isCurrent, availability);
        // Calculate size with cap for small entities like slimes and magma cubes
        float entitySize = Math.max(fakePlayer.getBbHeight(), fakePlayer.getBbWidth());
        float scaleFactor = 1 / entitySize;
        // Cap the scale factor to prevent slimes/magma cubes from being too big
        scaleFactor = Math.min(scaleFactor, 2.0f);
        this.size = (int) (Remorphed.CONFIG.entity_size * scaleFactor);
        this.skin = skin;
        this.fakePlayer = fakePlayer;
        this.cachedRenderState = cachedRenderState; // Use cached render state with proper scale
        setTooltip(Tooltip.create(Component.literal(skin.getName())));
    }

    @Override
    protected void sendFavoriteRequest(boolean isFavorite) {
        NetworkHandler.sendFavoriteRequest(skin, isFavorite);
    }

    @Override
    protected void sendSwap2ndShapeRequest() {
        NetworkHandler.sendSwapSkinRequest(skin);
        Player player = Minecraft.getInstance().player;
        if (player != null && PlayerShape.getCurrentShape(player) != null) {
            SwapPackets.sendSwapRequest();
        }
    }

    @Override
    protected void renderShape(GuiGraphics guiGraphics) {
        if (skin != null) {
            int leftPos = (int) (getX() + (float) this.getWidth() / 2);
            int topPos = (int) (getY() + this.getHeight() * .75f);
            int k = leftPos - 20;
            int l = topPos - 25;
            int m = leftPos + 20;
            int n = topPos + 35;
            // Use a unique ID for each skin widget (based on skin UUID hash)
            int id = skin.getId().hashCode();
            RemorphedClient.renderEntityInInventory(id, guiGraphics, k, l, m, n, size, new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), null, fakePlayer, cachedRenderState);
        }
    }
}
