package dev.tocraft.remorphed.impl;

import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface RemorphedPlayerDataProvider {

    // Kills keyed by EntityType — variants are never tracked separately
    Map<EntityType<?>, Integer> remorphed$getUnlockedShapes();
    void remorphed$addKill(EntityType<?> type);
    int remorphed$getKills(EntityType<?> type);

    Set<EntityType<?>> remorphed$getFavoriteShapes();

    Map<UUID, Integer> remorphed$getUnlockedSkins();
    void remorphed$addKill(UUID skinId);
    int remorphed$getKills(UUID skinId);
    Set<UUID> remorphed$getFavoriteSkins();

    int remorphed$getCounter(EntityType<?> type);
    int remorphed$getCounter(UUID skinId);

    void remorphed$handleSwap(EntityType<?> to);
    void remorphed$handleSwap(UUID skinId);

    Map<EntityType<?>, Integer> remorphed$getShapeCounter();
    Map<UUID, Integer> remorphed$getSkinCounter();

    Map<EntityType<?>, ShapeType<?>> remorphed$getDefaultVariants();
    void remorphed$setDefaultVariant(EntityType<?> entityType, ShapeType<?> shapeType);
}