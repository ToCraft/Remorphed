package dev.tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerMorph {

    public static Map<EntityType<? extends @NotNull LivingEntity>, Integer> getUnlockedShapes(Player player) {
        return provider(player).remorphed$getUnlockedShapes();
    }

    public static void addKill(Player player, ShapeType<? extends LivingEntity> type) {
        provider(player).remorphed$addKill(type.getEntityType());
    }

    public static int getKills(Player player, ShapeType<? extends LivingEntity> type) {
        return provider(player).remorphed$getKills(type.getEntityType());
    }

    public static int getKills(Player player, EntityType<?> type) {
        return provider(player).remorphed$getKills((EntityType<? extends LivingEntity>) type);
    }

    public static Set<ShapeType<?>> getFavoriteShapes(Player player) {
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

    public static void handleSwap(Player player, ShapeType<? extends LivingEntity> type) {
        provider(player).remorphed$handleSwap(type);
    }

    public static void handleSwap(Player player, UUID skinId) {
        provider(player).remorphed$handleSwap(skinId);
    }

    public static int getCounter(Player player, ShapeType<? extends LivingEntity> type) {
        return provider(player).remorphed$getCounter(type.getEntityType());
    }

    public static int getCounter(Player player, UUID skinId) {
        return provider(player).remorphed$getCounter(skinId);
    }

    public static Map<EntityType<? extends LivingEntity>, Integer> getShapeCounter(Player player) {
        return provider(player).remorphed$getShapeCounter();
    }

    public static Map<UUID, Integer> getSkinCounter(Player player) {
        return provider(player).remorphed$getSkinCounter();
    }

    private static RemorphedPlayerDataProvider provider(Player player) {
        return (RemorphedPlayerDataProvider) player;
    }
}