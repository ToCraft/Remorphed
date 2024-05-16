package tocraft.remorphed.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Set;

public class NetworkHandler {
    public static final ResourceLocation SHAPE_REQUEST = Remorphed.id("unlock_request");
    public static final ResourceLocation UNLOCKED_SYNC = Remorphed.id("unlocked_sync");
    public static final ResourceLocation FAVORITE_SYNC = Remorphed.id("favorite_sync");
    public static final ResourceLocation FAVORITE_UPDATE = Remorphed.id("favorite_update");

    public static void registerPacketReceiver() {
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, NetworkHandler.SHAPE_REQUEST, NetworkHandler::handleShapeRequestPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.C2S, FAVORITE_UPDATE, NetworkHandler::handleFavoriteRequestPacket);
    }

    public static <T extends LivingEntity> void sendSwap2ndShapeRequest(@NotNull ShapeType<T> type) {
        CompoundTag compound = new CompoundTag();
        compound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()).toString());
        compound.putInt("variant", type.getVariantData());

        ModernNetworking.sendToServer(NetworkHandler.SHAPE_REQUEST, compound);
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private static void handleShapeRequestPacket(ModernNetworking.Context context, CompoundTag compound) {
        context.getPlayer().getServer().execute(() -> {
            // check if player is blacklisted
            if (Walkers.CONFIG.playerUUIDBlacklist.contains(context.getPlayer().getUUID())) {
                context.getPlayer().displayClientMessage(Component.translatable("walkers.player_blacklisted"), true);
                return;
            }

            ResourceLocation typeId = new ResourceLocation(compound.getString("id"));
            int typeVariant = compound.getInt("variant");

            EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId);

            // make the default ShapeType null, doing it this way, it's ensured that invalid 2ndShapes won't cause crashes.
            @Nullable
            ShapeType<? extends LivingEntity> type = ShapeType.from(eType, typeVariant);
            // update Player
            boolean result = PlayerShapeChanger.change2ndShape((ServerPlayer) context.getPlayer(), type);
            if (result && type != null)
                PlayerShape.updateShapes((ServerPlayer) context.getPlayer(), type.create(context.getPlayer().level()));

            // Refresh player dimensions
            context.getPlayer().refreshDimensions();
        });
    }

    public static void sendFavoriteSync(ServerPlayer player) {
        Set<ShapeType<?>> favorites = ((RemorphedPlayerDataProvider) player).remorphed$getFavorites();
        CompoundTag tag = new CompoundTag();
        ListTag idList = new ListTag();
        favorites.forEach(type -> idList.add(type.writeCompound()));
        tag.put("FavoriteShapes", idList);

        // Create & send packet with NBT
        ModernNetworking.sendToPlayer(player, NetworkHandler.FAVORITE_SYNC, tag);
    }

    public static void sendFavoriteRequest(ShapeType<? extends LivingEntity> type, boolean favorite) {
        CompoundTag packet = new CompoundTag();
        packet.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()).toString());
        packet.putInt("variant", type.getVariantData());
        packet.putBoolean("favorite", favorite);
        ModernNetworking.sendToServer(FAVORITE_UPDATE, packet);
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private static void handleFavoriteRequestPacket(ModernNetworking.Context context, CompoundTag packet) {
        EntityType<? extends LivingEntity> entityType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(packet.getString("id")));
        int variant = packet.getInt("variant");
        boolean favorite = packet.getBoolean("favorite");
        RemorphedPlayerDataProvider playerData = (RemorphedPlayerDataProvider) context.getPlayer();

        context.getPlayer().getServer().execute(() -> {
            @Nullable ShapeType<?> type = ShapeType.from(entityType, variant);

            if (type != null) {
                if (favorite)
                    playerData.remorphed$getFavorites().add(type);
                else
                    playerData.remorphed$getFavorites().remove(type);
                // resync favorites
                sendFavoriteSync((ServerPlayer) context.getPlayer());
            }
        });
    }
}
