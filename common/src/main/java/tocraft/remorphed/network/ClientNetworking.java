package tocraft.remorphed.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tocraft.craftedcore.client.CraftedCoreClient;
import tocraft.craftedcore.network.ModernNetworking;
import tocraft.craftedcore.network.client.ClientNetworking.ApplicablePacket;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.walkers.api.variant.ShapeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientNetworking {
    public static void registerPacketHandlers() {
        ModernNetworking.registerReceiver(ModernNetworking.Side.S2C, NetworkHandler.UNLOCKED_SYNC,
                ClientNetworking::handleUnlockedSyncPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.S2C, NetworkHandler.FAVORITE_SYNC, ClientNetworking::handleFavoriteSyncPacket);
    }

    @SuppressWarnings("unchecked")
    public static void handleUnlockedSyncPacket(ModernNetworking.Context context, CompoundTag compound) {
        final UUID uuid = compound.getUUID("uuid");
        final Map<ShapeType<?>, Integer> unlockedShapes = new HashMap<>();
        if (compound.contains("UnlockedShapes") && compound.get("UnlockedShapes") instanceof ListTag list) {
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

            if (syncTarget != null) {
                PlayerMorph.getUnlockedShapes(player).clear();
                PlayerMorph.getUnlockedShapes(player).putAll(unlockedShapes);
            }
        });
    }


    private static void handleFavoriteSyncPacket(ModernNetworking.Context context, CompoundTag tag) {
        ClientNetworking.runOrQueue(context, player -> {
            PlayerMorph.getFavorites(player).clear();
            ListTag idList = tag.getList("FavoriteShapes", Tag.TAG_COMPOUND);
            idList.forEach(compound -> PlayerMorph.getFavorites(player).add(ShapeType.from((CompoundTag) compound)));
        });
    }

    public static void runOrQueue(ModernNetworking.Context context, ApplicablePacket packet) {
        if (context.getPlayer() == null) {
            CraftedCoreClient.getSyncPacketQueue().add(packet);
        } else {
            context.queue(() -> packet.apply(context.getPlayer()));
        }
    }
}
