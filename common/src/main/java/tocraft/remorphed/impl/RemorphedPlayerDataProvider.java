package tocraft.remorphed.impl;

import net.minecraft.world.entity.LivingEntity;
import tocraft.walkers.api.variant.ShapeType;

import java.util.Map;

public interface RemorphedPlayerDataProvider {

    Map<ShapeType<? extends LivingEntity>, Integer> getUnlockedShapes();

    void setUnlockedShapes(Map<ShapeType<? extends LivingEntity>, Integer> types);

    void addKill(ShapeType<? extends LivingEntity> type);

    int getKills(ShapeType<? extends LivingEntity> type);
}
