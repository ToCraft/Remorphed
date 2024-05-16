package tocraft.remorphed.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.event.common.EntityEvents;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

public class LivingDeathHandler implements EntityEvents.LivingDeath {
    @Override
    public InteractionResult die(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof Player) && source.getEntity() instanceof ServerPlayer killer) {
            ShapeType<?> type = ShapeType.from(entity);
            if (type != null) {
                ((RemorphedPlayerDataProvider) killer).remorphed$addKill(type);

                if (Remorphed.CONFIG.autoTransform && ((RemorphedPlayerDataProvider) killer).remorphed$getKills(type) >= Remorphed.getKillToUnlock(type.getEntityType())) {
                    PlayerShapeChanger.change2ndShape(killer, type);
                    PlayerShape.updateShapes(killer, type.create(killer.level));
                }
            }
        }

        return InteractionResult.PASS;
    }
}
