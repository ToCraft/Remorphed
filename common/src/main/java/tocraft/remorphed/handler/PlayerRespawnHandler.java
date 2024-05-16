package tocraft.remorphed.handler;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import tocraft.craftedcore.event.client.ClientPlayerEvents;
import tocraft.craftedcore.event.common.PlayerEvents;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;

public class PlayerRespawnHandler implements PlayerEvents.PlayerRespawn {
    @Override
    public void clone(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        ((RemorphedPlayerDataProvider) newPlayer).remorphed$setUnlockedShapes(((RemorphedPlayerDataProvider) oldPlayer).remorphed$getUnlockedShapes());
        ((RemorphedPlayerDataProvider) newPlayer).remorphed$getFavorites().clear();
        ((RemorphedPlayerDataProvider) newPlayer).remorphed$getFavorites().addAll(((RemorphedPlayerDataProvider) oldPlayer).remorphed$getFavorites());
    }
}
