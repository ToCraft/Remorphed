package tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import dev.tocraft.skinshifter.SkinShifter;
import dev.tocraft.skinshifter.data.SkinCache;
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

    public FakeClientPlayer(ClientLevel level, @NotNull PlayerProfile skin) {
        super(level, new GameProfile(skin.id(), skin.name()));
        if (skin.skin() != null) {
            ResourceLocation skinId = SkinCache.getCustomSkinId(skin.skin());
            ResourceLocation capeId = SkinShifter.CONFIG.changeCape ? SkinCache.getCustomCapeId(skin.cape()) : null;
            PlayerSkin.Model model = skin.isSlim() ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
            this.skin = new PlayerSkin(skinId, skin.skin().toString(), capeId, null, model, true);
        } else {
            this.skin = super.getSkin();
        }
    }

    //#if MC>1201
    @Override
    public @NotNull PlayerSkin getSkin() {
        return this.skin;
    }
    //#else
    //$$ @Overwrite
    //$$ public ResourceLocation setToNewSkin() {
    //$$     ShiftPlayerSkin skin = SkinPlayerData.getSkin((Player) (Object) this);
    //$$     if (skin != null && skin.skin() != null) {
    //$$         cir.setReturnValue(SkinCache.getCustomSkinId(skin.skin()));
    //$$     }
    //$$ }
    //$$ @Overwrite
    //$$ public String setModelType() {
    //$$     ShiftPlayerSkin skin = SkinPlayerData.getSkin((Player) (Object) this);
    //$$     if (null != skin && skin.skin() != null) {
    //$$         cir.setReturnValue(skin.isSlim() ? "slim" : "default");
    //$$     }
    //$$ }
    //$$ @Overwrite
    //$$ public ResourceLocation setToNewCloak() {
    //$$     if (SkinShifter.CONFIG.changeCape) {
    //$$         ShiftPlayerSkin skin = SkinPlayerData.getSkin((Player) (Object) this);
    //$$         if (skin != null && skin.cape() != null) {
    //$$             cir.setReturnValue(SkinCache.getCustomCapeId(skin.cape()));
    //$$         }
    //$$     }
    //$$ }
    //#endif
}
