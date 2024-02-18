package tocraft.remorphed.tick;

import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.RemorphedClient;
import tocraft.remorphed.screen.RemorphedHelpScreen;
import tocraft.remorphed.screen.RemorphedScreen;

@Environment(EnvType.CLIENT)
public class KeyPressHandler implements ClientTickEvent.Client {
    @Override
    public void tick(Minecraft client) {
        assert client.player != null;

        if (RemorphedClient.MENU_KEY.consumeClick()) {
            if (Remorphed.canUseAnyShape(client.player))
                Minecraft.getInstance().setScreen(new RemorphedScreen());
            else
                Minecraft.getInstance().setScreen(new RemorphedHelpScreen());
        }
    }
}
