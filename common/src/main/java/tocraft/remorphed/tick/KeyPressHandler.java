package tocraft.remorphed.tick;

import net.minecraft.client.Minecraft;
import tocraft.craftedcore.events.client.ClientTickEvents;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.RemorphedClient;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.remorphed.screen.RemorphedHelpScreen;
import tocraft.remorphed.screen.RemorphedScreen;

public class KeyPressHandler implements ClientTickEvents.Client {

	@Override
	public void tick(Minecraft client) {
		assert client.player != null;

		if (RemorphedClient.MENU_KEY.consumeClick()) {
			if (Remorphed.transformationIsLocked(client.player) || ((RemorphedPlayerDataProvider) client.player).getUnlockedShapes().isEmpty())
				Minecraft.getInstance().setScreen(new RemorphedHelpScreen());
			else
				Minecraft.getInstance().setScreen(new RemorphedScreen());
		}
	}
}
