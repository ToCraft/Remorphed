package tocraft.remorphed.impl;

import java.util.Map;

import net.minecraft.world.entity.LivingEntity;
import tocraft.walkers.api.variant.ShapeType;

public interface RemorphedPlayerDataProvider {
	
	void setUnlockedShapes(Map<ShapeType<? extends LivingEntity>, Integer> types);
	Map<ShapeType<? extends LivingEntity>, Integer> getUnlockedShapes();
	
	void addUnlockShape(ShapeType<? extends LivingEntity> type);
}
