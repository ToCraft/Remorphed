package tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import dev.tocraft.skinshifter.SkinShifter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import tocraft.craftedcore.platform.PlayerProfile;

//#if MC>1201
import net.minecraft.client.resources.PlayerSkin;
//#else
//$$ import org.jetbrains.annotations.Nullable;
//#endif

@Environment(EnvType.CLIENT)
public class FakeClientPlayer extends AbstractClientPlayer {
    //#if MC>1201
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
    //$$         skinId = skinProfile.getSkinId();
    //$$         modelType = skinProfile.isSlim() ? "slim" : "default";
    //$$         cloakId = skinProfile.getCapeId();
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
