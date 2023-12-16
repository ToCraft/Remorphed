package tocraft.remorphed.impl;

import java.util.Set;

import net.minecraft.world.entity.LivingEntity;
import tocraft.walkers.api.variant.ShapeType;

public interface RemorphedPlayerDataProvider {
	
	void setUnlockedShapes(Set<ShapeType<? extends LivingEntity>> types);
	Set<ShapeType<? extends LivingEntity>> getUnlockedShapes();
	
	void addUnlockShape(ShapeType<? extends LivingEntity> type);
	void removeUnlockedShape(ShapeType<? extends LivingEntity> type);
}
