package tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class FakeClientPlayer extends AbstractClientPlayer {
    public FakeClientPlayer(ClientLevel level, @NotNull GameProfile skinProfile) {
        super(level, skinProfile);
    }
}
