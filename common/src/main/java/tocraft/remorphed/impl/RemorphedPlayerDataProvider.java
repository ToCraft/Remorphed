package tocraft.remorphed.impl;

import net.minecraft.world.entity.LivingEntity;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @see PlayerMorph
 */
public interface RemorphedPlayerDataProvider {

    Map<ShapeType<? extends LivingEntity>, Integer> remorphed$getUnlockedShapes();

    void remorphed$addKill(ShapeType<? extends LivingEntity> type);

    int remorphed$getKills(ShapeType<? extends LivingEntity> type);

    Set<ShapeType<?>> remorphed$getFavoriteShapes();

    Map<UUID, Integer> remorphed$getUnlockedSkins();

    void remorphed$addKill(UUID skinId);

    int remorphed$getKills(UUID skinId);

    Set<UUID> remorphed$getFavoriteSkins();
}
