package tocraft.remorphed.handler;

import net.minecraft.server.level.ServerPlayer;
import tocraft.craftedcore.event.common.PlayerEvents;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.remorphed.network.NetworkHandler;

public class PlayerRespawnHandler implements PlayerEvents.PlayerRespawn {
    @Override
    public void clone(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        PlayerMorph.getUnlockedShapes(newPlayer).clear();
        PlayerMorph.getUnlockedShapes(newPlayer).putAll(PlayerMorph.getUnlockedShapes(oldPlayer));
        PlayerMorph.getFavorites(newPlayer).clear();
        PlayerMorph.getFavorites(newPlayer).addAll(PlayerMorph.getFavorites(oldPlayer));
        NetworkHandler.sendFavoriteSync(newPlayer);
    }
}
