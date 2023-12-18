package tocraft.remorphed.tick;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.events.client.ClientTickEvents;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.RemorphedClient;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.remorphed.screen.RemorphedHelpScreen;
import tocraft.remorphed.screen.RemorphedScreen;

public class KeyPressHandler implements ClientTickEvents.Client {
	private boolean canUseShapes = false;

	@Override
	public void tick(Minecraft client) {
		assert client.player != null;

		if (RemorphedClient.MENU_KEY.consumeClick()) {
			if (!canUseAnyShape(client.player))
				Minecraft.getInstance().setScreen(new RemorphedHelpScreen());
			else
				Minecraft.getInstance().setScreen(new RemorphedScreen());
		}
	}
	
	private boolean canUseAnyShape(Player player) {
		canUseShapes = false;
		
		((RemorphedPlayerDataProvider) player).getUnlockedShapes().forEach((shape, killAmount) -> {
			if (killAmount >= Remorphed.CONFIG.killToUnlock) {
				canUseShapes = true;
				return;
			}
		});
		
		return canUseShapes && !Remorphed.transformationIsLocked(player);
	}
}
