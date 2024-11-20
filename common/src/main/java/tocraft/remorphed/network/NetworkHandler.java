package tocraft.remorphed.network;

import dev.tocraft.skinshifter.SkinShifter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tocraft.craftedcore.network.ModernNetworking;
import tocraft.craftedcore.patched.CEntity;
import tocraft.craftedcore.patched.Identifier;
import tocraft.craftedcore.patched.TComponent;
import tocraft.craftedcore.platform.PlayerProfile;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Set;
import java.util.UUID;

public class NetworkHandler {
    public static final ResourceLocation MORPH_REQUEST = Remorphed.id("morph_request");
    public static final ResourceLocation UNLOCKED_SYNC = Remorphed.id("unlocked_sync");
    public static final ResourceLocation FAVORITE_SYNC = Remorphed.id("favorite_sync");
    public static final ResourceLocation FAVORITE_UPDATE = Remorphed.id("favorite_update");
    public static final ResourceLocation RESET_SKIN = Remorphed.id("reset_skin");

    public static void registerPacketReceiver() {
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, NetworkHandler.MORPH_REQUEST, NetworkHandler::handleMorphRequestPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, FAVORITE_UPDATE, NetworkHandler::handleFavoriteRequestPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, NetworkHandler.RESET_SKIN, NetworkHandler::handleResetSkinPacket);

        //#if MC>=1205
        ModernNetworking.registerType(UNLOCKED_SYNC);
        ModernNetworking.registerType(FAVORITE_SYNC);
        //#endif
    }

    private static void handleResetSkinPacket(ModernNetworking.Context context, CompoundTag data) {
        if (Remorphed.foundSkinShifter) {
            SkinShifter.setSkin((ServerPlayer) context.getPlayer(), null);
        }
    }

    public static void sendResetSkinPacket() {
        ModernNetworking.sendToServer(RESET_SKIN, new CompoundTag());
    }

    public static <T extends LivingEntity> void sendSwap2ndShapeRequest(@NotNull ShapeType<T> type) {
        CompoundTag compound = new CompoundTag();
        compound.putString("id", Walkers.getEntityTypeRegistry().getKey(type.getEntityType()).toString());
        compound.putInt("variant", type.getVariantData());

        ModernNetworking.sendToServer(NetworkHandler.MORPH_REQUEST, compound);
    }

    public static <T extends LivingEntity> void sendSwapSkinRequest(@NotNull PlayerProfile playerProfile) {
        CompoundTag compound = new CompoundTag();
        compound.putUUID("playerUUID", playerProfile.id());

        ModernNetworking.sendToServer(NetworkHandler.MORPH_REQUEST, compound);
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private static void handleMorphRequestPacket(ModernNetworking.Context context, CompoundTag compound) {
        context.getPlayer().getServer().execute(() -> {
            // check if player is blacklisted
            if (Walkers.isPlayerBlacklisted(context.getPlayer().getUUID()) && Walkers.CONFIG.blacklistPreventsMorphing) {
                context.getPlayer().displayClientMessage(TComponent.translatable("walkers.player_blacklisted"), true);
                return;
            }

            if (compound.contains("playerUUID") && Remorphed.foundSkinShifter) {
                UUID targetSkinUUID = compound.getUUID("playerUUID");
                SkinShifter.setSkin((ServerPlayer) context.getPlayer(), targetSkinUUID);
            } else {
                ResourceLocation typeId = Identifier.parse(compound.getString("id"));
                int typeVariant = compound.getInt("variant");

                EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) Walkers.getEntityTypeRegistry().get(typeId);

                // make the default ShapeType null, doing it this way, it's ensured that invalid 2ndShapes won't cause crashes.
                @Nullable
                ShapeType<? extends LivingEntity> type = ShapeType.from(eType, typeVariant);
                // update Player
                boolean result = PlayerShapeChanger.change2ndShape((ServerPlayer) context.getPlayer(), type);
                if (result && type != null)
                    PlayerShape.updateShapes((ServerPlayer) context.getPlayer(), type.create(CEntity.level(context.getPlayer())));

                // Refresh player dimensions
                context.getPlayer().refreshDimensions();
            }
        });
    }

    public static void sendFavoriteSync(ServerPlayer player) {
        Set<ShapeType<?>> favoriteShapes = PlayerMorph.getFavoriteShapes(player);
        Set<UUID> favoriteSkins = PlayerMorph.getFavoriteSkinIds(player);
        CompoundTag tag = new CompoundTag();
        ListTag shapeIdList = new ListTag();
        ListTag skinIdList = new ListTag();
        favoriteShapes.forEach(type -> shapeIdList.add(type.writeCompound()));
        favoriteSkins.forEach(skin -> skinIdList.add(NbtUtils.createUUID(skin)));
        tag.put("FavoriteShapes", shapeIdList);
        tag.put("FavoriteSkins", skinIdList);

        // Create & send packet with NBT
        ModernNetworking.sendToPlayer(player, NetworkHandler.FAVORITE_SYNC, tag);
    }

    public static void sendFavoriteRequest(ShapeType<? extends LivingEntity> type, boolean favorite) {
        CompoundTag packet = new CompoundTag();
        packet.putString("id", Walkers.getEntityTypeRegistry().getKey(type.getEntityType()).toString());
        packet.putInt("variant", type.getVariantData());
        packet.putBoolean("favorite", favorite);
        ModernNetworking.sendToServer(FAVORITE_UPDATE, packet);
    }

    public static void sendFavoriteRequest(PlayerProfile playerProfile, boolean favorite) {
        CompoundTag packet = new CompoundTag();
        packet.putUUID("playerUUID", playerProfile.id());
        packet.putBoolean("favorite", favorite);
        ModernNetworking.sendToServer(FAVORITE_UPDATE, packet);
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private static void handleFavoriteRequestPacket(ModernNetworking.Context context, CompoundTag packet) {
        boolean favorite = packet.getBoolean("favorite");

        if (packet.contains("playerUUID")) {
            UUID skinId = packet.getUUID("playerUUID");

            context.getPlayer().getServer().execute(() -> {
                if (favorite) {
                    PlayerMorph.getFavoriteSkinIds(context.getPlayer()).add(skinId);
                } else {
                    PlayerMorph.getFavoriteSkinIds(context.getPlayer()).remove(skinId);
                }
                // re-sync favorites
                sendFavoriteSync((ServerPlayer) context.getPlayer());
            });
        } else {
            EntityType<? extends LivingEntity> entityType = (EntityType<? extends LivingEntity>) Walkers.getEntityTypeRegistry().get(Identifier.parse(packet.getString("id")));
            int variant = packet.getInt("variant");

            context.getPlayer().getServer().execute(() -> {
                @Nullable ShapeType<?> type = ShapeType.from(entityType, variant);

                if (type != null) {
                    if (favorite) {
                        PlayerMorph.getFavoriteShapes(context.getPlayer()).add(type);
                    } else {
                        PlayerMorph.getFavoriteShapes(context.getPlayer()).remove(type);
                    }
                    // resync favorites
                    sendFavoriteSync((ServerPlayer) context.getPlayer());
                }
            });
        }
    }
}
