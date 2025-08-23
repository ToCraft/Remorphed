package dev.tocraft.remorphed.handler.client;

import net.minecraft.client.player.LocalPlayer;
import dev.tocraft.craftedcore.event.client.ClientPlayerEvents;
import dev.tocraft.remorphed.impl.PlayerMorph;

public class ClientPlayerRespawnHandler implements ClientPlayerEvents.ClientPlayerRespawn {
    @Override
    public void respawn(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        // walkers
        PlayerMorph.getUnlockedShapes(newPlayer).clear();
        PlayerMorph.getUnlockedShapes(newPlayer).putAll(PlayerMorph.getUnlockedShapes(oldPlayer));
        PlayerMorph.getFavoriteShapes(newPlayer).clear();
        PlayerMorph.getFavoriteShapes(newPlayer).addAll(PlayerMorph.getFavoriteShapes(oldPlayer));
        // shapeshifter
        PlayerMorph.getUnlockedSkinIds(newPlayer).clear();
        PlayerMorph.getUnlockedSkinIds(newPlayer).putAll(PlayerMorph.getUnlockedSkinIds(oldPlayer));
        PlayerMorph.getFavoriteSkinIds(newPlayer).clear();
        PlayerMorph.getFavoriteSkinIds(newPlayer).addAll(PlayerMorph.getFavoriteSkinIds(oldPlayer));
    }
}
