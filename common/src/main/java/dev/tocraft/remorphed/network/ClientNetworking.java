package dev.tocraft.remorphed.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.tocraft.craftedcore.client.CraftedCoreClient;
import dev.tocraft.craftedcore.network.ModernNetworking;
import dev.tocraft.craftedcore.network.client.ClientNetworking.ApplicablePacket;
import dev.tocraft.remorphed.impl.PlayerMorph;
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
    public static void handleUnlockedSyncPacket(ModernNetworking.Context context, @NotNull CompoundTag compound) {
        final UUID uuid = UUIDUtil.uuidFromIntArray(compound.getIntArray("uuid").orElseThrow());
        final Map<ShapeType<?>, Integer> unlockedShapes = new HashMap<>();
        if (compound.contains("UnlockedShapes")) {
            compound.getListOrEmpty("UnlockedShapes").forEach(entryTag -> {
                EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(((CompoundTag) entryTag).getString("id").orElseThrow())).map(Holder::value).orElse(null);
                int variant = ((CompoundTag) entryTag).getIntOr("variant", -1);
                int killAmount = ((CompoundTag) entryTag).getIntOr("killAmount", 0);
                unlockedShapes.put(ShapeType.from(eType, variant), killAmount);
            });
        }
        final Map<UUID, Integer> unlockedSkins = new HashMap<>();
        if (compound.contains("UnlockedSkins")) {
            compound.getListOrEmpty("UnlockedSkins").forEach(entryTag -> {
                UUID unlockedSkinUUID = UUIDUtil.uuidFromIntArray(((CompoundTag) entryTag).getIntArray("uuid").orElseThrow());
                int killAmount = ((CompoundTag) entryTag).getIntOr("killAmount", 0);
                unlockedSkins.put(unlockedSkinUUID, killAmount);
            });
        }

        final Map<ShapeType<?>, Integer> shapeCounter = new HashMap<>();
        final Map<UUID, Integer> skinCounter = new HashMap<>();
        if (compound.contains("MorphCounter")) {
            compound.getListOrEmpty("MorphCounter").forEach(entry -> {
                boolean isSkin = ((CompoundTag) entry).getBoolean("isSkin").orElseThrow();
                int count = ((CompoundTag) entry).getIntOr("counter", 0);
                if (isSkin) {
                    UUID skinId = UUIDUtil.uuidFromIntArray(((CompoundTag) entry).getIntArray("uuid").orElseThrow());
                    skinCounter.put(skinId, count);
                } else {
                    ResourceLocation typeId = ResourceLocation.parse(((CompoundTag) entry).getString("id").orElseThrow());
                    int typeVariantId = ((CompoundTag) entry).getIntOr("variant", -1);
                    shapeCounter.put(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), typeVariantId), count);
                }
            });
        }

        runOrQueue(context, player -> {
            @Nullable
            Player syncTarget = player.level().getPlayerByUUID(uuid);

            if (syncTarget != null) {
                PlayerMorph.getUnlockedShapes(player).clear();
                PlayerMorph.getUnlockedShapes(player).putAll(unlockedShapes);
                PlayerMorph.getUnlockedSkinIds(player).clear();
                PlayerMorph.getUnlockedSkinIds(player).putAll(unlockedSkins);
                PlayerMorph.getShapeCounter(player).clear();
                PlayerMorph.getShapeCounter(player).putAll(shapeCounter);
                PlayerMorph.getSkinCounter(player).clear();
                PlayerMorph.getSkinCounter(player).putAll(skinCounter);
            }
        });
    }


    private static void handleFavoriteSyncPacket(ModernNetworking.Context context, CompoundTag tag) {
        ClientNetworking.runOrQueue(context, player -> {
            PlayerMorph.getFavoriteShapes(player).clear();
            PlayerMorph.getFavoriteSkinIds(player).clear();
            ListTag shapeIds = tag.getListOrEmpty("FavoriteShapes");
            ListTag skinIds = tag.getListOrEmpty("FavoriteSkins");
            shapeIds.forEach(compound -> PlayerMorph.getFavoriteShapes(player).add(ShapeType.from((CompoundTag) compound)));
            skinIds.forEach(skinId -> PlayerMorph.getFavoriteSkinIds(player).add(UUIDUtil.uuidFromIntArray(skinId.asIntArray().orElseThrow())));
        });
    }

    public static void runOrQueue(ModernNetworking.@NotNull Context context, ApplicablePacket packet) {
        if (context.getPlayer() == null) {
            CraftedCoreClient.getSyncPacketQueue().add(packet);
        } else {
            context.queue(() -> packet.apply(context.getPlayer()));
        }
    }
}
