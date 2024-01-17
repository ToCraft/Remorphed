package tocraft.remorphed.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import tocraft.craftedcore.events.Event.Result;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class UnlockShapeCallback implements ShapeEvents.UnlockShapeCallback {
    @Override
    public Result unlock(ServerPlayer player, ShapeType<?> type) {
        // check if entity is unlocked by remorphed, prevents native unlocks by walkers
        if (!Remorphed.canUseShape(player, type))
            return Result.interruptFalse();
        else if (Remorphed.CONFIG.unlockFriendlyNormal) {
            LivingEntity entityToBeUnlocked = type.create(player.level());
            if (entityToBeUnlocked instanceof TamableAnimal || entityToBeUnlocked instanceof NeutralMob)
                ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().put(type, Remorphed.CONFIG.killToUnlock);
        }

        return Result.pass();
    }
}
