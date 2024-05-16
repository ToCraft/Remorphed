package tocraft.remorphed.handler.client;

import net.minecraft.client.player.LocalPlayer;
import tocraft.craftedcore.event.client.ClientPlayerEvents;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;

public class ClientPlayerRespawnHandler implements ClientPlayerEvents.ClientPlayerRespawn {
    @Override
    public void respawn(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        ((RemorphedPlayerDataProvider) newPlayer).remorphed$setUnlockedShapes(((RemorphedPlayerDataProvider) oldPlayer).remorphed$getUnlockedShapes());
        ((RemorphedPlayerDataProvider) newPlayer).remorphed$getFavorites().clear();
        ((RemorphedPlayerDataProvider) newPlayer).remorphed$getFavorites().addAll(((RemorphedPlayerDataProvider) oldPlayer).remorphed$getFavorites());
    }
}
