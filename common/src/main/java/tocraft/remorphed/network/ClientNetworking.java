package tocraft.remorphed.network;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tocraft.craftedcore.client.CraftedCoreClient;
import tocraft.craftedcore.network.client.ClientNetworking.ApplicablePacket;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.variant.ShapeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientNetworking {
    public static void registerPacketHandlers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, NetworkHandler.UNLOCKED_SYNC,
                ClientNetworking::handleUnlockedSyncPacket);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, NetworkHandler.FAVORITE_SYNC, ClientNetworking::handleFavoriteSyncPacket);
    }

    @SuppressWarnings("unchecked")
    public static void handleUnlockedSyncPacket(FriendlyByteBuf packet, NetworkManager.PacketContext context) {
        final UUID uuid = packet.readUUID();
        final CompoundTag compound = packet.readNbt();
        final Map<ShapeType<?>, Integer> unlockedShapes = new HashMap<>();
        if (compound != null && compound.contains("UnlockedShapes") && compound.get("UnlockedShapes") instanceof ListTag list) {
            list.forEach(entryTag -> {
                EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(((CompoundTag) entryTag).getString("id")));
                int variant = ((CompoundTag) entryTag).getInt("variant");
                int killAmount = ((CompoundTag) entryTag).getInt("killAmount");
                unlockedShapes.put(ShapeType.from(eType, variant), killAmount);
            });
        }

        runOrQueue(context, player -> {
            @Nullable
            Player syncTarget = player.getCommandSenderWorld().getPlayerByUUID(uuid);

            if (syncTarget != null)
                ((RemorphedPlayerDataProvider) syncTarget).remorphed$setUnlockedShapes(unlockedShapes);
        });
    }


    private static void handleFavoriteSyncPacket(FriendlyByteBuf packet, NetworkManager.PacketContext context) {
        CompoundTag tag = packet.readNbt();

        if (tag != null) {
            ClientNetworking.runOrQueue(context, player -> {
                RemorphedPlayerDataProvider data = (RemorphedPlayerDataProvider) player;
                data.remorphed$getFavorites().clear();
                ListTag idList = tag.getList("FavoriteShapes", Tag.TAG_COMPOUND);
                idList.forEach(compound -> data.remorphed$getFavorites().add(ShapeType.from((CompoundTag) compound)));
            });
        }
    }

    public static void runOrQueue(NetworkManager.PacketContext context, ApplicablePacket packet) {
        if (context.getPlayer() == null) {
            CraftedCoreClient.getSyncPacketQueue().add(packet);
        } else {
            context.queue(() -> packet.apply(context.getPlayer()));
        }
    }
}
