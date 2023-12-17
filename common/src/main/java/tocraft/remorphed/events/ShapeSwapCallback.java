package tocraft.remorphed.events;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import tocraft.craftedcore.events.Event.Result;
import tocraft.remorphed.Remorphed;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.impl.PlayerDataProvider;

public class ShapeSwapCallback implements ShapeEvents.ShapeSwapCallback {
	@Override
	public Result swap(ServerPlayer player, @Nullable LivingEntity to) {
		// check if entity is unlocked by remorphed, prevents native unlocks by walkers
		if (Remorphed.transformationIsLocked(player) && !((PlayerDataProvider) player).walkers$get2ndShape().getEntityType().equals(to.getType()))
			return Result.interruptFalse();
		else
			return Result.pass();
	}
}
