package tocraft.remorphed.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class ClientPlayerDataCacheMixin extends ClientCommonPacketListenerImpl {
    @Unique
    private RemorphedPlayerDataProvider remorphed$dataCache = null;

    protected ClientPlayerDataCacheMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    // This @Inject caches the custom data attached to this client's player before it
    // is reset when changing dimensions.
    // For example, switching from The End => Overworld will reset the player's NBT.
    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;createPlayer(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/stats/StatsCounter;Lnet/minecraft/client/ClientRecipeBook;ZZ)Lnet/minecraft/client/player/LocalPlayer;"))
    private void beforePlayerReset(ClientboundRespawnPacket packet, CallbackInfo ci) {
        remorphed$dataCache = ((RemorphedPlayerDataProvider) minecraft.player);
    }

    // This inject applies data cached from the previous inject.
    // Re-applying on the client will help to prevent sync blips which occur when
    // wiping data and waiting for the server to send a sync packet.
    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;", ordinal = 1))
    private void afterPlayerReset(ClientboundRespawnPacket packet, CallbackInfo ci) {
        if (remorphed$dataCache != null && minecraft.player != null) {
            ((RemorphedPlayerDataProvider) minecraft.player).remorphed$setUnlockedShapes(remorphed$dataCache.remorphed$getUnlockedShapes());
            ((RemorphedPlayerDataProvider) minecraft.player).remorphed$getFavorites().clear();
            ((RemorphedPlayerDataProvider) minecraft.player).remorphed$getFavorites().addAll(remorphed$dataCache.remorphed$getFavorites());
        }

        remorphed$dataCache = null;
    }
}