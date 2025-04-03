package tocraft.remorphed.screen.widget;

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
import tocraft.craftedcore.platform.PlayerProfile;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.FakeClientPlayer;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.network.impl.SwapPackets;

@Environment(EnvType.CLIENT)
public class SkinWidget extends ShapeWidget {
    private final PlayerProfile skin;
    private final FakeClientPlayer fakePlayer;
    private final int size;

    public SkinWidget(int x, int y, int width, int height, @NotNull PlayerProfile skin, @NotNull FakeClientPlayer fakePlayer, Screen parent, boolean isFavorite, boolean isCurrent, int availability) {
        super(x, y, width, height, parent, isFavorite, isCurrent, availability);
        this.size = (int) (Remorphed.CONFIG.entity_size * (1 / (Math.max(fakePlayer.getBbHeight(), fakePlayer.getBbWidth()))));
        this.skin = skin;
        this.fakePlayer = fakePlayer;
        setTooltip(Tooltip.create(Component.literal(skin.name())));
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
        if (skin.skin() != null) {
            InventoryScreen.renderEntityInInventory(guiGraphics, getX() + (float) this.getWidth() / 2, (int) (getY() + this.getHeight() * .75f), size, new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), null, fakePlayer);
        }
    }
}
