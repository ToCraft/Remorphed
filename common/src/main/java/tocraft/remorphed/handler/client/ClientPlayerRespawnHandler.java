package tocraft.remorphed.handler.client;

import net.minecraft.client.player.LocalPlayer;
import tocraft.craftedcore.event.client.ClientPlayerEvents;
import tocraft.remorphed.impl.PlayerMorph;

public class ClientPlayerRespawnHandler implements ClientPlayerEvents.ClientPlayerRespawn {
    @Override
    public void respawn(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        PlayerMorph.getUnlockedShapes(newPlayer).clear();
        PlayerMorph.getUnlockedShapes(newPlayer).putAll(PlayerMorph.getUnlockedShapes(oldPlayer));
        PlayerMorph.getFavorites(newPlayer).clear();
        PlayerMorph.getFavorites(newPlayer).addAll(PlayerMorph.getFavorites(oldPlayer));
    }
}
