package dev.tocraft.remorphed.tick;

import dev.tocraft.craftedcore.event.client.ClientTickEvents;
import dev.tocraft.remorphed.RemorphedClient;
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
            // Open menu directly - menu access is not restricted by permissions
            // as it can be easily bypassed client-side
            Minecraft.getInstance().setScreen(new RemorphedMenu());
        }
    }
}
