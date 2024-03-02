package tocraft.remorphed.impl;

import net.minecraft.world.entity.LivingEntity;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Map;
import java.util.Set;

public interface RemorphedPlayerDataProvider {

    Map<ShapeType<? extends LivingEntity>, Integer> remorphed$getUnlockedShapes();

    void remorphed$setUnlockedShapes(Map<ShapeType<? extends LivingEntity>, Integer> types);

    void remorphed$addKill(ShapeType<? extends LivingEntity> type);

    int remorphed$getKills(ShapeType<? extends LivingEntity> type);

    Set<ShapeType<?>> remorphed$getFavorites();
}
