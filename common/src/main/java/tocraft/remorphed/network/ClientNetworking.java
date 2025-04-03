package tocraft.remorphed.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tocraft.craftedcore.client.CraftedCoreClient;
import tocraft.craftedcore.network.ModernNetworking;
import tocraft.craftedcore.network.client.ClientNetworking.ApplicablePacket;
import tocraft.remorphed.Remorphed;
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
    public static void handleUnlockedSyncPacket(ModernNetworking.Context context, @NotNull CompoundTag compound) {
        final UUID uuid = compound.getUUID("uuid");
        final Map<ShapeType<?>, Integer> unlockedShapes = new HashMap<>();
        if (compound.contains("UnlockedShapes")) {
            compound.getList("UnlockedShapes", Tag.TAG_COMPOUND).forEach(entryTag -> {
                EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(((CompoundTag) entryTag).getString("id"))).map(Holder::value).orElse(null);
                int variant = ((CompoundTag) entryTag).getInt("variant");
                int killAmount = ((CompoundTag) entryTag).getInt("killAmount");
                unlockedShapes.put(ShapeType.from(eType, variant), killAmount);
            });
        }
        final Map<UUID, Integer> unlockedSkins = new HashMap<>();
        if (compound.contains("UnlockedSkins")) {
            compound.getList("UnlockedSkins", Tag.TAG_COMPOUND).forEach(entryTag -> {
                UUID unlockedSkinUUID = ((CompoundTag) entryTag).getUUID("uuid");
                int killAmount = ((CompoundTag) entryTag).getInt("killAmount");
                unlockedSkins.put(unlockedSkinUUID, killAmount);
            });
        }

        final Map<ShapeType<?>, Integer> shapeCounter = new HashMap<>();
        final Map<UUID, Integer> skinCounter = new HashMap<>();
        if (compound.contains("MorphCounter")) {
            compound.getList("MorphCounter", Tag.TAG_COMPOUND).forEach(entry -> {
                boolean isSkin = ((CompoundTag) entry).getBoolean("isSkin");
                int count = ((CompoundTag) entry).getInt("counter");
                if (isSkin) {
                    UUID skinId = ((CompoundTag) entry).getUUID("uuid");
                    skinCounter.put(skinId, count);
                } else {
                    ResourceLocation typeId = ResourceLocation.parse(((CompoundTag) entry).getString("id"));
                    int typeVariantId = ((CompoundTag) entry).getInt("variant");
                    shapeCounter.put(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), typeVariantId), count);
                }
            });
        }

        Remorphed.LOGGER.warn(shapeCounter.toString());

        runOrQueue(context, player -> {
            @Nullable
            Player syncTarget = player.getCommandSenderWorld().getPlayerByUUID(uuid);

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
            ListTag shapeIds = tag.getList("FavoriteShapes", Tag.TAG_COMPOUND);
            ListTag skinIds = tag.getList("FavoriteSkins", Tag.TAG_INT_ARRAY);
            shapeIds.forEach(compound -> PlayerMorph.getFavoriteShapes(player).add(ShapeType.from((CompoundTag) compound)));
            skinIds.forEach(skinId -> PlayerMorph.getFavoriteSkinIds(player).add(NbtUtils.loadUUID(skinId)));
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
