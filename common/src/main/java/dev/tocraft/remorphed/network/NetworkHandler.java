package dev.tocraft.remorphed.network;

import com.mojang.authlib.GameProfile;
import dev.tocraft.craftedcore.network.ModernNetworking;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.compatibility.SkinShifterCompat;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.remorphed.permission.PermissionManager;
import dev.tocraft.walkers.Walkers;
import dev.tocraft.walkers.api.PlayerShape;
import dev.tocraft.walkers.api.PlayerShapeChanger;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

// TODO: Add custom morph to normal Packet and disable walkers' morphing
public class NetworkHandler {
    public static final Identifier MORPH_REQUEST = Remorphed.id("morph_request");
    public static final Identifier UNLOCKED_SYNC = Remorphed.id("unlocked_sync");
    public static final Identifier FAVORITE_SYNC = Remorphed.id("favorite_sync");
    public static final Identifier FAVORITE_UPDATE = Remorphed.id("favorite_update");
    public static final Identifier RESET_SKIN = Remorphed.id("reset_skin");
    public static final Identifier PERMISSION_CHECK = Remorphed.id("permission_check");
    public static final Identifier PERMISSION_RESPONSE = Remorphed.id("permission_response");
    public static final Identifier DELETE_SHAPE = Remorphed.id("delete_shape");
    public static final Identifier DEFAULT_VARIANT_SYNC = Remorphed.id("default_variant_sync");

    public static void registerPacketReceiver() {
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, NetworkHandler.MORPH_REQUEST, NetworkHandler::handleMorphRequestPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, FAVORITE_UPDATE, NetworkHandler::handleFavoriteRequestPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, NetworkHandler.RESET_SKIN, NetworkHandler::handleResetSkinPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, PERMISSION_CHECK, NetworkHandler::handlePermissionCheckPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, DELETE_SHAPE, NetworkHandler::handleDeleteShapePacket);

        ModernNetworking.registerType(UNLOCKED_SYNC);
        ModernNetworking.registerType(FAVORITE_SYNC);
        ModernNetworking.registerType(PERMISSION_RESPONSE);
        ModernNetworking.registerType(DEFAULT_VARIANT_SYNC);
    }

    private static void handleDeleteShapePacket(ModernNetworking.Context context, CompoundTag data) {
        boolean is_entity = data.getBoolean("is_entity").orElse(true);
        if (is_entity) {
            String id = data.getString("id").orElseThrow();
            int v = data.getIntOr("variant", -1);
            @SuppressWarnings("unchecked") EntityType<LivingEntity> type = (EntityType<LivingEntity>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
            PlayerMorph.handleSwap(context.getPlayer(), ShapeType.from(type, v));
        } else {
            String uuid = data.getString("uuid").orElseThrow();
            PlayerMorph.handleSwap(context.getPlayer(), UUID.fromString(uuid));
        }
    }

    public static void sendDeleteShapePacket(ShapeType<?> type) {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("is_entity", true);
        compound.putString("id", EntityType.getKey(type.getEntityType()).toString());
        compound.putInt("variant", type.getVariantData());

        ModernNetworking.sendToServer(NetworkHandler.DELETE_SHAPE, compound);
    }

    public static void sendDeleteShapePacket(UUID uuid) {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("is_entity", false);
        compound.putString("uuid", uuid.toString());

        ModernNetworking.sendToServer(NetworkHandler.DELETE_SHAPE, compound);
    }

    private static void handleResetSkinPacket(ModernNetworking.Context context, CompoundTag data) {
        if (Remorphed.foundSkinShifter) {
            SkinShifterCompat.setSkin((ServerPlayer) context.getPlayer(), null);
        }
    }

    @SuppressWarnings("ConstantValue")
    private static void handlePermissionCheckPacket(ModernNetworking.@NotNull Context context, @NotNull CompoundTag tag) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        String permission = tag.getString("permission").orElse("");

        // Only check permissions if permissions are enabled
        boolean hasPermission = !Remorphed.CONFIG.usePermissions || PermissionManager.hasPermission(player, permission);

        // Send response back to client
        CompoundTag response = new CompoundTag();
        response.putString("permission", permission);
        response.putBoolean("hasPermission", hasPermission);
        ModernNetworking.sendToPlayer(player, PERMISSION_RESPONSE, response);
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
        compound.putIntArray("playerUUID", UUIDUtil.uuidToIntArray(playerProfile.id()));

        ModernNetworking.sendToServer(NetworkHandler.MORPH_REQUEST, compound);
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private static void handleMorphRequestPacket(ModernNetworking.@NotNull Context context, CompoundTag compound) {
        ((ServerPlayer) context.getPlayer()).level().getServer().execute(() -> {
            // check if player is blacklisted
            if (Walkers.isPlayerBlacklisted(context.getPlayer().getUUID()) && Walkers.CONFIG.blacklistPreventsMorphing) {
                context.getPlayer().sendSystemMessage(Component.translatable("walkers.player_blacklisted"));
                return;
            }

            if (compound.contains("playerUUID") && Remorphed.foundSkinShifter) {
                UUID targetSkinUUID = UUIDUtil.uuidFromIntArray(compound.getIntArray("playerUUID").orElseThrow());
                if (!Objects.equals(targetSkinUUID, SkinShifterCompat.getCurrentSkin(context.getPlayer()))) {
                    Remorphed.spawnWalkersParticles(context.getPlayer());
                }
                SkinShifterCompat.setSkin((ServerPlayer) context.getPlayer(), targetSkinUUID);
                PlayerMorph.handleSwap(context.getPlayer(), targetSkinUUID);
            } else {
                Identifier typeId = Identifier.parse(compound.getString("id").orElseThrow());
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
        packet.putIntArray("playerUUID", UUIDUtil.uuidToIntArray(playerProfile.id()));
        packet.putBoolean("favorite", favorite);
        ModernNetworking.sendToServer(FAVORITE_UPDATE, packet);
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private static void handleFavoriteRequestPacket(ModernNetworking.Context context, @NotNull CompoundTag packet) {
        boolean favorite = packet.getBooleanOr("favorite", false);

        if (packet.contains("playerUUID")) {
            UUID skinId = UUIDUtil.uuidFromIntArray(packet.getIntArray("playerUUID").orElseThrow());

            ((ServerPlayer) context.getPlayer()).level().getServer().execute(() -> {
                if (favorite) {
                    PlayerMorph.getFavoriteSkinIds(context.getPlayer()).add(skinId);
                } else {
                    PlayerMorph.getFavoriteSkinIds(context.getPlayer()).remove(skinId);
                }
                // re-sync favorites
                sendFavoriteSync((ServerPlayer) context.getPlayer());
            });
        } else {
            EntityType<? extends LivingEntity> entityType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(packet.getString("id").orElseThrow())).map(Holder::value).orElse(null);
            int variant = packet.getIntOr("variant", -1);

            ((ServerPlayer) context.getPlayer()).level().getServer().execute(() -> {
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

    public static void sendDefaultVariantSync(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        PlayerMorph.getDefaultVariants(player).forEach((type, shape) -> {
            CompoundTag e = new CompoundTag();
            e.putString("entity_id", EntityType.getKey(type).toString());
            e.putInt("variant", shape.getVariantData());
            list.add(e);
        });
        tag.put("DefaultVariants", list);
        ModernNetworking.sendToPlayer(player, DEFAULT_VARIANT_SYNC, tag);
    }
}
