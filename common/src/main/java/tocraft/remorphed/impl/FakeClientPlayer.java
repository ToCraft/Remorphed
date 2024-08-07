package tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import dev.tocraft.skinshifter.SkinShifter;
import dev.tocraft.skinshifter.data.SkinCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tocraft.craftedcore.platform.PlayerProfile;

import java.util.UUID;

//#if MC>1201
import net.minecraft.client.resources.PlayerSkin;
//#endif

@Environment(EnvType.CLIENT)
public class FakeClientPlayer extends AbstractClientPlayer {
    //#if MC>1201
    private final PlayerSkin skin;
    
    public FakeClientPlayer(ClientLevel level, @NotNull PlayerProfile skinProfile) {
        super(level, new GameProfile(skinProfile.id(), skinProfile.name()));
        if (skinProfile.skin() != null) {
            ResourceLocation skinId = SkinCache.getCustomSkinId(skinProfile.skin());
            ResourceLocation capeId = SkinShifter.CONFIG.changeCape ? SkinCache.getCustomCapeId(skinProfile.cape()) : null;
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
    //#else
    //$$ @Nullable
    //$$ private final ResourceLocation skinId;
    //$$ @Nullable
    //$$ private final String modelType;
    //$$ @Nullable
    //$$ private final ResourceLocation cloakId;
    //$$
    //$$ public FakeClientPlayer(ClientLevel clientLevel, @NotNull PlayerProfile skinProfile) {
    //$$     super(clientLevel, new GameProfile(skinProfile.id(), skinProfile.name()));
    //$$
    //$$     if (skinProfile.skin() != null) {
    //$$         skinId = SkinCache.getCustomSkinId(skinProfile.skin());
    //$$         modelType = skinProfile.isSlim() ? "slim" : "default";
    //$$         cloakId = SkinCache.getCustomCapeId(skinProfile.cape());
    //$$     } else {
    //$$         skinId = null;
    //$$         modelType = null;
    //$$         cloakId = null;
    //$$     }
    //$$ }
    //$$
    //$$ @Override
    //$$ public @NotNull ResourceLocation getSkinTextureLocation() {
    //$$     return skinId != null ? skinId : super.getSkinTextureLocation();
    //$$ }
    //$$ @Override
    //$$ public @NotNull String getModelName() {
    //$$     return modelType != null ? modelType : super.getModelName();
    //$$ }
    //$$ @Override
    //$$ public @Nullable ResourceLocation getCloakTextureLocation() {
    //$$     return skinId != null || cloakId != null ? cloakId : super.getCloakTextureLocation();
    //$$ }
    //#endif
}
