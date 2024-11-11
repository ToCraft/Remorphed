package tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import dev.tocraft.skinshifter.SkinShifter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import tocraft.craftedcore.platform.PlayerProfile;

@Environment(EnvType.CLIENT)
public class FakeClientPlayer extends AbstractClientPlayer {
    private final PlayerSkin skin;

    public FakeClientPlayer(ClientLevel level, @NotNull PlayerProfile skinProfile) {
        super(level, new GameProfile(skinProfile.id(), skinProfile.name()));
        if (skinProfile.skin() != null) {
            ResourceLocation skinId = skinProfile.getSkinId();
            ResourceLocation capeId = SkinShifter.CONFIG.changeCape ? skinProfile.getCapeId() : null;
            PlayerSkin.Model model = skinProfile.isSlim() ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
            this.skin = new PlayerSkin(skinId, skinProfile.skin().toString(), capeId, null, model, true);
        } else {
            this.skin = super.getSkin();
        }
    }

    @Override
    public @NotNull PlayerSkin getSkin() {
        return this.skin;
    }
}
