package tocraft.remorphed.network;

import com.mojang.authlib.GameProfile;
import dev.tocraft.skinshifter.SkinShifter;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tocraft.craftedcore.network.ModernNetworking;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Set;
import java.util.UUID;

// TODO: Add custom morph to normal Packet and disable walkers' morphing
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

        ModernNetworking.registerType(UNLOCKED_SYNC);
        ModernNetworking.registerType(FAVORITE_SYNC);
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
        compound.putString("id", EntityType.getKey(type.getEntityType()).toString());
        compound.putInt("variant", type.getVariantData());

        ModernNetworking.sendToServer(NetworkHandler.MORPH_REQUEST, compound);
    }

    public static <T extends LivingEntity> void sendSwapSkinRequest(@NotNull GameProfile playerProfile) {
        CompoundTag compound = new CompoundTag();
        compound.putIntArray("playerUUID", UUIDUtil.uuidToIntArray(playerProfile.getId()));

        ModernNetworking.sendToServer(NetworkHandler.MORPH_REQUEST, compound);
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private static void handleMorphRequestPacket(ModernNetworking.@NotNull Context context, CompoundTag compound) {
        context.getPlayer().getServer().execute(() -> {
            // check if player is blacklisted
            if (Walkers.isPlayerBlacklisted(context.getPlayer().getUUID()) && Walkers.CONFIG.blacklistPreventsMorphing) {
                context.getPlayer().displayClientMessage(Component.translatable("walkers.player_blacklisted"), true);
                return;
            }

            if (compound.contains("playerUUID") && Remorphed.foundSkinShifter) {
                UUID targetSkinUUID = UUIDUtil.uuidFromIntArray(compound.getIntArray("playerUUID").orElseThrow());
                SkinShifter.setSkin((ServerPlayer) context.getPlayer(), targetSkinUUID);
                PlayerMorph.handleSwap(context.getPlayer(), targetSkinUUID);
            } else {
                ResourceLocation typeId = ResourceLocation.parse(compound.getString("id").orElseThrow());
                int typeVariant = compound.getIntOr("variant", -1);

                EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null);

                // make the default ShapeType null, doing it this way, it's ensured that invalid 2ndShapes won't cause crashes.
                @Nullable
                ShapeType<? extends LivingEntity> type = ShapeType.from(eType, typeVariant);
                // update Player
                boolean result = PlayerShapeChanger.change2ndShape((ServerPlayer) context.getPlayer(), type);
                if (result && type != null) {
                    PlayerShape.updateShapes((ServerPlayer) context.getPlayer(), type.create(context.getPlayer().level(), context.getPlayer()));
                }

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
        favoriteSkins.forEach(skin -> skinIdList.add(new IntArrayTag(UUIDUtil.uuidToIntArray(skin))));
        tag.put("FavoriteShapes", shapeIdList);
        tag.put("FavoriteSkins", skinIdList);

        // Create & send packet with NBT
        ModernNetworking.sendToPlayer(player, NetworkHandler.FAVORITE_SYNC, tag);
    }

    public static void sendFavoriteRequest(@NotNull ShapeType<? extends LivingEntity> type, boolean favorite) {
        CompoundTag packet = new CompoundTag();
        packet.putString("id", EntityType.getKey(type.getEntityType()).toString());
        packet.putInt("variant", type.getVariantData());
        packet.putBoolean("favorite", favorite);
        ModernNetworking.sendToServer(FAVORITE_UPDATE, packet);
    }

    public static void sendFavoriteRequest(@NotNull GameProfile playerProfile, boolean favorite) {
        CompoundTag packet = new CompoundTag();
        packet.putIntArray("playerUUID", UUIDUtil.uuidToIntArray(playerProfile.getId()));
        packet.putBoolean("favorite", favorite);
        ModernNetworking.sendToServer(FAVORITE_UPDATE, packet);
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private static void handleFavoriteRequestPacket(ModernNetworking.Context context, @NotNull CompoundTag packet) {
        boolean favorite = packet.getBooleanOr("favorite", false);

        if (packet.contains("playerUUID")) {
            UUID skinId = UUIDUtil.uuidFromIntArray(packet.getIntArray("playerUUID").orElseThrow());

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
            EntityType<? extends LivingEntity> entityType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(packet.getString("id").orElseThrow())).map(Holder::value).orElse(null);
            int variant = packet.getIntOr("variant", -1);

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
