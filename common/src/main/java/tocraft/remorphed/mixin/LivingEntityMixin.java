package tocraft.remorphed.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    private LivingEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Inject(method = "die", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (!((Object) this instanceof Player) && damageSource.getEntity() instanceof ServerPlayer killer) {
            ShapeType<?> type = ShapeType.from((LivingEntity) (Object) this);
            ((RemorphedPlayerDataProvider) killer).remorphed$addKill(type);

            if (Remorphed.CONFIG.autoTransform && ((RemorphedPlayerDataProvider) killer).remorphed$getKills(type) >= Remorphed.CONFIG.killToUnlock) {
                PlayerShapeChanger.change2ndShape(killer, type);
                PlayerShape.updateShapes(killer, type.create(killer.level()));
            }
        }
    }
}
