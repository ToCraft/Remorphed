package dev.tocraft.remorphed.network;

import dev.tocraft.craftedcore.client.CraftedCoreClient;
import dev.tocraft.craftedcore.network.ModernNetworking;
import dev.tocraft.craftedcore.network.client.ClientNetworking.ApplicablePacket;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientNetworking {
    public static void registerPacketHandlers() {
        ModernNetworking.registerReceiver(ModernNetworking.Side.S2C, NetworkHandler.UNLOCKED_SYNC,
                ClientNetworking::handleUnlockedSyncPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.S2C, NetworkHandler.FAVORITE_SYNC, ClientNetworking::handleFavoriteSyncPacket);
        ModernNetworking.registerReceiver(ModernNetworking.Side.S2C, NetworkHandler.PERMISSION_RESPONSE, PermissionCheckPacket::handlePermissionResponse);
        ModernNetworking.registerReceiver(ModernNetworking.Side.S2C, NetworkHandler.DEFAULT_VARIANT_SYNC, ClientNetworking::handleDefaultVariantSyncPacket);
    }

    @SuppressWarnings("unchecked")
    public static void handleUnlockedSyncPacket(ModernNetworking.Context context, @NotNull CompoundTag compound) {
        final UUID uuid = UUIDUtil.uuidFromIntArray(compound.getIntArray("uuid").orElseThrow());
        final Map<EntityType<? extends @NotNull LivingEntity>, Integer> unlockedShapes = new HashMap<>();
        if (compound.contains("UnlockedShapes")) {
            compound.getListOrEmpty("UnlockedShapes").forEach(entryTag -> {
                EntityType<? extends @NotNull LivingEntity> eType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(((CompoundTag) entryTag).getString("id").orElseThrow())).map(Holder::value).orElseThrow();
                int killAmount = ((CompoundTag) entryTag).getIntOr("killAmount", 0);
                unlockedShapes.put(eType, killAmount);
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

        final Map<EntityType<? extends LivingEntity>, Integer> shapeCounter = new HashMap<>();
        final Map<UUID, Integer> skinCounter = new HashMap<>();
        if (compound.contains("MorphCounter")) {
            compound.getListOrEmpty("MorphCounter").forEach(entry -> {
                boolean isSkin = ((CompoundTag) entry).getBoolean("isSkin").orElseThrow();
                int count = ((CompoundTag) entry).getIntOr("counter", 0);
                if (isSkin) {
                    UUID skinId = UUIDUtil.uuidFromIntArray(((CompoundTag) entry).getIntArray("uuid").orElseThrow());
                    skinCounter.put(skinId, count);
                } else {
                    Identifier typeId = Identifier.parse(((CompoundTag) entry).getString("id").orElseThrow());
                    shapeCounter.put((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), count);
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
            shapeIds.forEach(idTag -> {
                if (idTag instanceof StringTag(String value)) {
                    Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(value)).map(Holder.Reference::value);
                    type.ifPresent(entityType -> PlayerMorph.getFavoriteShapes(player).add(entityType));
                }
            });
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

    private static void handleDefaultVariantSyncPacket(ModernNetworking.Context context, CompoundTag tag) {
        Map<EntityType<? extends LivingEntity>, ShapeType<?>> defaults = new HashMap<>();
        tag.getListOrEmpty("DefaultVariants").forEach(entry -> {
            if (entry instanceof CompoundTag e) {
                Identifier entityId = Identifier.parse(e.getString("entity_id").orElseThrow());
                Optional<Holder.Reference<@NotNull EntityType<?>>> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                Optional<Integer> variant = e.getInt("variant");
                if (type.isPresent() && variant.isPresent()) {
                    EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) type.get().value();
                    ShapeType<?> shape = ShapeType.from(eType, variant.get());
                    if (shape != null) {
                        defaults.put(eType, shape);
                    }
                }
            }
        });

        runOrQueue(context, player -> {
            PlayerMorph.getDefaultVariants(player).clear();
            PlayerMorph.getDefaultVariants(player).putAll(defaults);
        });
    }
}
