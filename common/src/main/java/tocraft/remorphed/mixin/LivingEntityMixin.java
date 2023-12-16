package tocraft.remorphed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.variant.ShapeType;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    private LivingEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }
    
    @Inject(method = "die", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
    	if (!((Object) this instanceof Player) && damageSource.getEntity() instanceof Player killer) {
    		((RemorphedPlayerDataProvider) killer).addUnlockShape(ShapeType.from((LivingEntity) (Object) this));
    	}
    }
}
