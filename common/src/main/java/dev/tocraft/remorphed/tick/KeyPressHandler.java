package dev.tocraft.remorphed.tick;

import dev.tocraft.craftedcore.event.client.ClientTickEvents;
import dev.tocraft.remorphed.RemorphedClient;
import dev.tocraft.remorphed.network.ClientPermissionCache;
import dev.tocraft.remorphed.network.PermissionCheckPacket;
import dev.tocraft.remorphed.screen.RemorphedMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class KeyPressHandler implements ClientTickEvents.Client {
    @Override
    public void tick(Minecraft client) {
        assert client.player != null;

        if (RemorphedClient.MENU_KEY.consumeClick()) {
            // Check if we have cached permission result
            Boolean hasMenuPermission = ClientPermissionCache.getPermission("remorphed.menu");
            
            if (hasMenuPermission != null && hasMenuPermission) {
                // Permission cached and granted, open menu directly
                Minecraft.getInstance().setScreen(new RemorphedMenu());
            } else if (hasMenuPermission != null && !hasMenuPermission) {
                // Permission cached and denied, show error message
                if (client.player != null) {
                    client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("remorphed.permission.menu.denied"), true);
                }
            } else {
                // Permission not cached, request from server
                PermissionCheckPacket.sendPermissionCheck("remorphed.menu");
            }
        }
    }
}
