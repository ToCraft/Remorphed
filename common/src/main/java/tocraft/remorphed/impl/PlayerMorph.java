package tocraft.remorphed.impl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Map;
import java.util.Set;

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

    public static Set<ShapeType<?>> getFavorites(Player player) {
        return ((RemorphedPlayerDataProvider) player).remorphed$getFavorites();
    }
}
