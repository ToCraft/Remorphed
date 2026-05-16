package dev.tocraft.remorphed.impl;

import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface RemorphedPlayerDataProvider {

    // Kills keyed by EntityType — variants are never tracked separately
    Map<EntityType<? extends LivingEntity>, Integer> remorphed$getUnlockedShapes();
    void remorphed$addKill(EntityType<? extends LivingEntity> type);
    int remorphed$getKills(EntityType<? extends LivingEntity> type);

    Set<ShapeType<?>> remorphed$getFavoriteShapes();

    Map<UUID, Integer> remorphed$getUnlockedSkins();
    void remorphed$addKill(UUID skinId);
    int remorphed$getKills(UUID skinId);
    Set<UUID> remorphed$getFavoriteSkins();

    // Counter keyed by EntityType for the same reason
    int remorphed$getCounter(EntityType<? extends LivingEntity> type);
    int remorphed$getCounter(UUID skinId);

    void remorphed$handleSwap(ShapeType<? extends LivingEntity> to);
    void remorphed$handleSwap(UUID skinId);

    Map<EntityType<? extends LivingEntity>, Integer> remorphed$getShapeCounter();
    Map<UUID, Integer> remorphed$getSkinCounter();

    @org.jetbrains.annotations.Nullable ShapeType<?> remorphed$getPreviousShape();
    void remorphed$setPreviousShape(@org.jetbrains.annotations.Nullable ShapeType<?> shape);
}