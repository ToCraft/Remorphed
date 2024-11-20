package tocraft.remorphed.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.event.common.EntityEvents;
import tocraft.craftedcore.patched.CEntity;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

public class LivingDeathHandler implements EntityEvents.LivingDeath {
    @Override
    public InteractionResult die(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof Player) && source.getEntity() instanceof ServerPlayer killer) {
            ShapeType<?> type = ShapeType.from(entity);
            if (type != null && (!Walkers.CONFIG.blacklistPreventsUnlocking || !Walkers.isPlayerBlacklisted(killer.getUUID()))) {
                PlayerMorph.addKill(killer, type);

                if (Remorphed.CONFIG.autoTransform && PlayerMorph.getKills(killer, type) >= Remorphed.getKillToUnlock(type.getEntityType())) {
                    PlayerShapeChanger.change2ndShape(killer, type);
                    PlayerShape.updateShapes(killer, type.create(CEntity.level(killer)));
                }
            }
        } else if (entity instanceof Player && source.getEntity() instanceof ServerPlayer killer) {
            PlayerMorph.addPlayerKill(killer, entity.getUUID());
        }

        return InteractionResult.PASS;
    }
}
