package dev.tocraft.remorphed.mixin.client.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayer.class)
public interface ClientPlayerAccessor {
    @Invoker
    PlayerInfo callGetPlayerInfo();
}
