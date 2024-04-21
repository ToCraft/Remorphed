package tocraft.remorphed.events;

import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.variant.ShapeType;

public class ShapeEventsCallback {
    public EventResult event(ServerPlayer player, ShapeType<?> type) {
        // check if the walkers unlock mechanic should be used
        if (!Remorphed.CONFIG.lockTransform && Remorphed.CONFIG.unlockFriendlyNormal) {
            LivingEntity entityToBeUnlocked = type.create(player.level());
            if (!(entityToBeUnlocked instanceof Enemy))
                ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().put(type, Remorphed.getKillToUnlock(type.getEntityType()));
        }
        // check if entity is unlocked by remorphed, prevents native unlock mechanic by walkers
        else if (!Remorphed.canUseShape(player, type)) {
            if (!Walkers.hasSpecialShape(player.getUUID()) || !type.getEntityType().equals(EntityType.WOLF))
                return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }
}
