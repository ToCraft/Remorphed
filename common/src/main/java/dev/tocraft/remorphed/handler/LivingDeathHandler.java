package dev.tocraft.remorphed.handler;

import dev.tocraft.craftedcore.event.common.EntityEvents;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.walkers.Walkers;
import dev.tocraft.walkers.api.PlayerShape;
import dev.tocraft.walkers.api.PlayerShapeChanger;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class LivingDeathHandler implements EntityEvents.LivingDeath {
    @Override
    public InteractionResult die(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof Player) && source.getEntity() instanceof ServerPlayer killer) {
            ShapeType<?> type = ShapeType.from(entity);
            if (type != null && (!Walkers.CONFIG.blacklistPreventsUnlocking || !Walkers.isPlayerBlacklisted(killer.getUUID()))) {
                PlayerMorph.addKill(killer, type);

                if (Remorphed.CONFIG.autoTransform && PlayerMorph.getKills(killer, type) >= Remorphed.getKillToUnlock(type.getEntityType())) {
                    PlayerShapeChanger.change2ndShape(killer, type);
                    PlayerShape.updateShapes(killer, type.create(killer.level(), killer));
                }
            }
        } else if (entity instanceof Player && source.getEntity() instanceof ServerPlayer killer) {
            PlayerMorph.addPlayerKill(killer, entity.getUUID());
        }

        return InteractionResult.PASS;
    }
}
