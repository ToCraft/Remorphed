package tocraft.remorphed.events;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import tocraft.craftedcore.events.Event.Result;
import tocraft.remorphed.Remorphed;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class ShapeSwapCallback implements ShapeEvents.ShapeSwapCallback {
	@Override
	public Result swap(ServerPlayer player, @Nullable LivingEntity to) {
		// check if entity is unlocked by remorphed, prevents native unlocks by walkers
		if (!Remorphed.canUseShape(player, ShapeType.from(to)))
			return Result.interruptFalse();
		else
			return Result.pass();
	}
}
