package tocraft.remorphed.events;

import net.minecraft.server.level.ServerPlayer;
import tocraft.craftedcore.events.Event.Result;
import tocraft.remorphed.Remorphed;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class UnlockShapeCallback implements ShapeEvents.UnlockShapeCallback {
	@Override
	public Result unlock(ServerPlayer player, ShapeType<?> type) {
		// check if entity is unlocked by remorphed, prevents native unlocks by walkers
		if (type != null && !Remorphed.canUseShape(player, type))
			return Result.interruptFalse();
		else {
			return Result.pass();
		}
	}
}
