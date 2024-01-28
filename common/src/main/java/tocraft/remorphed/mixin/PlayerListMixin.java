package tocraft.remorphed.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tocraft.remorphed.network.NetworkHandler;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "respawn", at = @At("RETURN"))
    private void resyncPacket(ServerPlayer player, boolean keepEverything, CallbackInfoReturnable<ServerPlayer> cir) {
        NetworkHandler.sendFavoriteSync(player);
    }
}
