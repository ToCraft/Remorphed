package tocraft.remorphed.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import tocraft.craftedcore.events.Event.Result;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class UnlockShapeCallback implements ShapeEvents.UnlockShapeCallback {
    @Override
    public Result unlock(ServerPlayer player, ShapeType<?> type) {
        // check if the walkers unlock mechanic should be used
        if (!Remorphed.CONFIG.lockTransform && Remorphed.CONFIG.unlockFriendlyNormal) {
            LivingEntity entityToBeUnlocked = type.create(player.level());
            if (!(entityToBeUnlocked instanceof Enemy))
                ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().put(type, Remorphed.CONFIG.killToUnlock);
        }
        // check if entity is unlocked by remorphed, prevents native unlock mechanic by walkers
        else if (!Remorphed.canUseShape(player, type))
            return Result.interruptFalse();

        return Result.pass();
    }
}
