package tocraft.remorphed.handler;

import net.minecraft.server.level.ServerPlayer;
import tocraft.craftedcore.event.common.PlayerEvents;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.remorphed.network.NetworkHandler;

public class PlayerRespawnHandler implements PlayerEvents.PlayerRespawn {
    @Override
    public void clone(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        // walkers
        PlayerMorph.getUnlockedShapes(newPlayer).clear();
        PlayerMorph.getUnlockedShapes(newPlayer).putAll(PlayerMorph.getUnlockedShapes(oldPlayer));
        PlayerMorph.getFavoriteShapes(newPlayer).clear();
        PlayerMorph.getFavoriteShapes(newPlayer).addAll(PlayerMorph.getFavoriteShapes(oldPlayer));
        // skin shifter
        PlayerMorph.getUnlockedSkinIds(newPlayer).clear();
        PlayerMorph.getUnlockedSkinIds(newPlayer).putAll(PlayerMorph.getUnlockedSkinIds(oldPlayer));
        PlayerMorph.getFavoriteSkinIds(newPlayer).clear();
        PlayerMorph.getFavoriteSkinIds(newPlayer).addAll(PlayerMorph.getFavoriteSkinIds(oldPlayer));
        // re-sync
        NetworkHandler.sendFavoriteSync(newPlayer);
    }
}
