package tocraft.remorphed.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.platform.PlayerProfile;
import tocraft.remorphed.impl.FakeClientPlayer;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.impl.PlayerDataProvider;
import tocraft.walkers.network.impl.SwapPackets;

//#if MC>=1201
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif
//#if MC>1182
import org.joml.Quaternionf;
import net.minecraft.client.gui.components.Tooltip;
//#if MC>=1202
import org.joml.Vector3f;
//#endif
//#else
//$$ import net.minecraft.client.gui.screens.Screen;
//#endif

@Environment(EnvType.CLIENT)
public class SkinWidget extends ShapeWidget {
    private final PlayerProfile skin;
    private final FakeClientPlayer fakePlayer;
    private final int size;

    public SkinWidget(float x, float y, float width, float height, PlayerProfile skin, FakeClientPlayer fakePlayer, RemorphedScreen parent, boolean isFavorite, boolean isCurrent) {
        super(x, y, width, height, parent, isFavorite, isCurrent);
        this.size = (int) (25 * (1 / (Math.max(fakePlayer.getBbHeight(), fakePlayer.getBbWidth()))));
        this.skin = skin;
        this.fakePlayer = fakePlayer;
        //#if MC>1182
        setTooltip(Tooltip.create(Component.literal(skin.name())));
        //#endif
    }

    @Override
    protected void sendFavoriteRequest(boolean isFavorite) {
        NetworkHandler.sendFavoriteRequest(skin, isFavorite);
    }

    @Override
    protected void sendSwap2ndShapeRequest() {
        NetworkHandler.sendSwapSkinRequest(skin);
        Player player = Minecraft.getInstance().player;
        if (player != null && ((PlayerDataProvider) player).walkers$getCurrentShape() != null) {
            SwapPackets.sendSwapRequest();
        }
    }

    @Override
    //#if MC>1194
    protected void renderShape(GuiGraphics guiGraphics) {
    //#else
    //$$ protected void renderShape(PoseStack guiGraphics) {
    //#endif
        if (skin.skin() != null) {
            //#if MC>1201
            InventoryScreen.renderEntityInInventory(guiGraphics, getX() + (float) this.getWidth() / 2, (int) (getY() + this.getHeight() * .75f), size, new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), null, fakePlayer);
            //#elseif MC>1182
            //$$ InventoryScreen.renderEntityInInventory(guiGraphics, getX() + this.getWidth() / 2, (int) (getY() + this.getHeight() * .75f), size, new Quaternionf().rotationXYZ((float) Math.PI, 0, 0), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), fakePlayer);
            //#else
            //$$ InventoryScreen.renderEntityInInventory(x + this.getWidth() / 2, (int) (y + this.getHeight() * .75f), size, -10, -10, fakePlayer);
            //#endif
        }
    }
}
