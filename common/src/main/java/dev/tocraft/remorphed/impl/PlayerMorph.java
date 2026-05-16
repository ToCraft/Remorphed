package dev.tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerMorph {

    public static Map<EntityType<?>, Integer> getUnlockedShapes(Player player) {
        return provider(player).remorphed$getUnlockedShapes();
    }

    public static void addKill(Player player, EntityType<?> type) {
        provider(player).remorphed$addKill(type);
    }

    public static int getKills(Player player, ShapeType<?> type) {
        return provider(player).remorphed$getKills(type.getEntityType());
    }

    public static int getKills(Player player, EntityType<?> type) {
        return provider(player).remorphed$getKills(type);
    }

    public static Set<EntityType<?>> getFavoriteShapes(Player player) {
        return provider(player).remorphed$getFavoriteShapes();
    }

    public static Map<UUID, Integer> getUnlockedSkinIds(Player player) {
        return provider(player).remorphed$getUnlockedSkins();
    }

    public static void addPlayerKill(Player player, UUID uuid) {
        provider(player).remorphed$addKill(uuid);
    }

    public static int getPlayerKills(Player player, UUID uuid) {
        return provider(player).remorphed$getKills(uuid);
    }

    public static Set<GameProfile> getFavoriteSkins(Player player) {
        return getFavoriteSkinIds(player).stream()
                .map(id -> Remorphed.getGameProfile(player, id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public static Set<UUID> getFavoriteSkinIds(Player player) {
        return provider(player).remorphed$getFavoriteSkins();
    }

    public static void handleSwap(Player player, EntityType<?> type) {
        provider(player).remorphed$handleSwap(type);
    }

    public static void handleSwap(Player player, UUID skinId) {
        provider(player).remorphed$handleSwap(skinId);
    }

    public static int getCounter(Player player, EntityType<?> type) {
        return provider(player).remorphed$getCounter(type);
    }

    public static int getCounter(Player player, UUID skinId) {
        return provider(player).remorphed$getCounter(skinId);
    }

    public static Map<EntityType<?>, Integer> getShapeCounter(Player player) {
        return provider(player).remorphed$getShapeCounter();
    }

    public static Map<UUID, Integer> getSkinCounter(Player player) {
        return provider(player).remorphed$getSkinCounter();
    }

    private static RemorphedPlayerDataProvider provider(Player player) {
        return (RemorphedPlayerDataProvider) player;
    }

    public static Map<EntityType<?>, ShapeType<?>> getDefaultVariants(Player player) {
        return provider(player).remorphed$getDefaultVariants();
    }

    public static void setDefaultVariant(Player player, EntityType<?> entityType, ShapeType<?> shapeType) {
        provider(player).remorphed$setDefaultVariant(entityType, shapeType);
    }
}