package dev.tocraft.remorphed.handler;

import dev.tocraft.craftedcore.event.common.EntityEvents;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.remorphed.permission.PermissionRegistry;
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
                // Check if player has permission to unlock this entity type (only if permissions are enabled)
                boolean canUnlock = !Remorphed.CONFIG.usePermissions || PermissionRegistry.getInstance().canMorphIntoType(killer, type.getEntityType());
                if (canUnlock) {
                    PlayerMorph.addKill(killer, type);

                    if (Remorphed.CONFIG.autoTransform && PlayerMorph.getKills(killer, type) >= Remorphed.getKillToUnlock(type.getEntityType())) {
                        PlayerShapeChanger.change2ndShape(killer, type);
                        PlayerShape.updateShapes(killer, type.create(killer.level(), killer));
                    }
                }
            }
        } else if (entity instanceof Player && source.getEntity() instanceof ServerPlayer killer) {
            // Check if player has permission to unlock player skins (requires SkinShifter)
            if (Remorphed.foundSkinShifter) {
                // For now, we'll allow player kills if the player has any skin-related permission
                // This could be made more specific in the future if needed
                // Only check permissions if permissions are enabled
                boolean canUnlockPlayer = !Remorphed.CONFIG.usePermissions || 
                    PermissionRegistry.getInstance().hasPermission(killer, "remorphed.command.addSkin") || 
                    PermissionRegistry.getInstance().hasPermission(killer, "remorphed.*");
                if (canUnlockPlayer) {
                    PlayerMorph.addPlayerKill(killer, entity.getUUID());
                }
            } else {
                // If SkinShifter isn't installed, don't track player kills
                PlayerMorph.addPlayerKill(killer, entity.getUUID());
            }
        }

        return InteractionResult.PASS;
    }
}
