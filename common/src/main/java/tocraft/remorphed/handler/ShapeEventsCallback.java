package tocraft.remorphed.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.events.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class ShapeEventsCallback implements ShapeEvents.UnlockShapeCallback {
    @Override
    public InteractionResult unlock(ServerPlayer player, ShapeType<?> type) {
        if (type != null) {
            // check if the walkers unlock mechanic should be used
            if (!Remorphed.CONFIG.lockTransform && Remorphed.CONFIG.unlockFriendlyNormal) {
                LivingEntity entityToBeUnlocked = type.create(player.level());
                if (!(entityToBeUnlocked instanceof Enemy)) {
                    PlayerMorph.getUnlockedShapes(player).put(type, Remorphed.getKillToUnlock(type.getEntityType()));
                }
            }
            // check if entity is unlocked by remorphed, prevents native unlock mechanic by walkers
            else if (!Remorphed.canUseShape(player, type)) {
                if (!type.getEntityType().equals(EntityType.WOLF) || !Walkers.hasSpecialShape(player.getUUID())) {
                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
