package tocraft.remorphed.impl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.platform.PlayerProfile;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    public static Set<PlayerProfile> getFavoriteSkins(Player player) {
        return getFavoriteSkinIds(player).stream().map(PlayerProfile::ofId).collect(Collectors.toSet());
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
}
