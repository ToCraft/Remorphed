package tocraft.remorphed.impl;

import com.mojang.authlib.GameProfile;
import dev.tocraft.skinshifter.data.SkinPlayerData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tocraft.walkers.api.variant.ShapeType;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerMorph {
    public static Map<ShapeType<? extends LivingEntity>, Integer> getUnlockedShapes(Player player) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes();
    }

    public static void addKill(Player player, ShapeType<? extends LivingEntity> type) {
        ((RemorphedPlayerDataProvider) player).remorphed$addKill(type);
    }

    public static int getKills(Player player, ShapeType<? extends LivingEntity> type) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getKills(type);
    }

    public static int getKills(Player player, EntityType<?> type) {
        return getUnlockedShapes(player).entrySet().stream().filter(e -> e.getKey() != null && type.equals(e.getKey().getEntityType())).map(Map.Entry::getValue).reduce(0, Integer::sum);
    }

    public static Set<ShapeType<?>> getFavoriteShapes(Player player) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getFavoriteShapes();
    }

    public static Map<UUID, Integer> getUnlockedSkinIds(Player player) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedSkins();
    }

    public static void addPlayerKill(Player player, UUID uuid) {
        ((RemorphedPlayerDataProvider) player).remorphed$addKill(uuid);
    }

    public static int getPlayerKills(Player player, UUID uuid) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getKills(uuid);
    }

    public static Set<GameProfile> getFavoriteSkins(Player player) {
        return getFavoriteSkinIds(player).stream().map(p -> SkinPlayerData.getSkinProfile(p).getNow(Optional.empty()).orElse(null)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static Set<UUID> getFavoriteSkinIds(Player player) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getFavoriteSkins();
    }

    public static void handleSwap(Player player, ShapeType<? extends LivingEntity> type) {
        ((RemorphedPlayerDataProvider) player).remorphed$handleSwap(type);
    }

    public static void handleSwap(Player player, UUID skinId) {
        ((RemorphedPlayerDataProvider) player).remorphed$handleSwap(skinId);
    }

    public static int getCounter(Player player, ShapeType<? extends LivingEntity> type) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getCounter(type);
    }

    public static int getCounter(Player player, UUID skinId) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getCounter(skinId);
    }

    public static Map<ShapeType<?>, Integer> getShapeCounter(Player player) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getShapeCounter();
    }

    public static Map<UUID, Integer> getSkinCounter(Player player) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getSkinCounter();
    }
}
