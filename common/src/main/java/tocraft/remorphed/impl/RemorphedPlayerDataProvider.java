package tocraft.remorphed.impl;

import net.minecraft.world.entity.LivingEntity;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Map;
import java.util.Set;

/**
 * @see PlayerMorph
 */
public interface RemorphedPlayerDataProvider {

    Map<ShapeType<? extends LivingEntity>, Integer> remorphed$getUnlockedShapes();

    void remorphed$addKill(ShapeType<? extends LivingEntity> type);

    int remorphed$getKills(ShapeType<? extends LivingEntity> type);

    Set<ShapeType<?>> remorphed$getFavorites();
}
