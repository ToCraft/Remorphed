package tocraft.remorphed.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, NetworkHandler.SHAPE_REQUEST, NetworkHandler::handleShapeRequestPacket);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, FAVORITE_UPDATE, NetworkHandler::handleFavoriteRequestPacket);
    }

    public static <T extends LivingEntity> void sendSwap2ndShapeRequest(@NotNull ShapeType<T> type) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        CompoundTag compound = new CompoundTag();
        compound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()).toString());
        compound.putInt("variant", type.getVariantData());

        buf.writeNbt(compound);

        NetworkManager.sendToServer(NetworkHandler.SHAPE_REQUEST, buf);
    }

    @SuppressWarnings({"ALL"})
    private static void handleShapeRequestPacket(FriendlyByteBuf packet, NetworkManager.PacketContext context) {
        CompoundTag compound = packet.readNbt();

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
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeNbt(tag);
        NetworkManager.sendToPlayer(player, NetworkHandler.FAVORITE_SYNC, packet);
    }

    public static void sendFavoriteRequest(ShapeType<? extends LivingEntity> type, boolean favorite) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()));
        packet.writeInt(type.getVariantData());
        packet.writeBoolean(favorite);
        NetworkManager.sendToServer(FAVORITE_UPDATE, packet);
    }

    @SuppressWarnings("ALL")
    private static void handleFavoriteRequestPacket(FriendlyByteBuf packet, NetworkManager.PacketContext context) {
        EntityType<? extends LivingEntity> entityType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(packet.readResourceLocation());
        int variant = packet.readInt();
        boolean favorite = packet.readBoolean();
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
