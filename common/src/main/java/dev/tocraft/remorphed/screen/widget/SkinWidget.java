package dev.tocraft.remorphed.screen.widget;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.FakeClientPlayer;
import dev.tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.network.impl.SwapPackets;

@Environment(EnvType.CLIENT)
public class SkinWidget extends ShapeWidget {
    private final GameProfile skin;
    private final FakeClientPlayer fakePlayer;
    private final int size;

    public SkinWidget(int x, int y, int width, int height, @NotNull GameProfile skin, @NotNull FakeClientPlayer fakePlayer, Screen parent, boolean isFavorite, boolean isCurrent, int availability) {
        super(x, y, width, height, parent, isFavorite, isCurrent, availability);
        this.size = (int) (Remorphed.CONFIG.entity_size * (1 / (Math.max(fakePlayer.getBbHeight(), fakePlayer.getBbWidth()))));
        this.skin = skin;
        this.fakePlayer = fakePlayer;
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
            InventoryScreen.renderEntityInInventory(guiGraphics, k, l, m, n, size, new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), null, fakePlayer);
        }
    }
}
