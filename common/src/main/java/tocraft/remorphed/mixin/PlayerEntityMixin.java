package tocraft.remorphed.mixin;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.variant.ShapeType;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RemorphedPlayerDataProvider {
	@Unique
	private Set<ShapeType<? extends LivingEntity>> unlockedShapes = new HashSet<ShapeType<? extends LivingEntity>>();
	@Unique
	private String UNLOCKED_SHAPES = "UnlockedShapes";

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void serverTick(CallbackInfo info) {

    }

	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	private void readNbt(CompoundTag tag, CallbackInfo info) {
    	readData(tag.getCompound(Remorphed.MODID));
	}

	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	private void writeNbt(CompoundTag tag, CallbackInfo info) {
		tag.put(Remorphed.MODID, writeData(new CompoundTag()));
	}
	
	@Unique
	private CompoundTag writeData(CompoundTag tag) {
		ListTag list = new ListTag();
		unlockedShapes.forEach(shape -> {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
			entryTag.putInt("variant", shape.getVariantData());
			list.add(entryTag);
		});
		if (list != null)
			tag.put(UNLOCKED_SHAPES, list);
		return tag;
	}

	@Unique
	public void readData(CompoundTag tag) {
		unlockedShapes.clear();
		
		if ((ListTag) tag.get(UNLOCKED_SHAPES) != null) {
			ListTag list = (ListTag) tag.get(UNLOCKED_SHAPES);
			list.forEach(entry -> {
				if (entry instanceof CompoundTag) {
					ResourceLocation typeId = new ResourceLocation(((CompoundTag) entry).getString("id"));
					int typeVariantId = ((CompoundTag) entry).getInt("variant");
					
					unlockedShapes.add(ShapeType.from(BuiltInRegistries.ENTITY_TYPE.get(typeId), typeVariantId));
				}
			});
		}
	}
	
	
	@Unique
	@Override
	public void setUnlockedShapes(Set<ShapeType<? extends LivingEntity>> types) {
		unlockedShapes = types;
	};
	
	@Unique
	@Override
	public Set<ShapeType<? extends LivingEntity>> getUnlockedShapes() {
		return unlockedShapes;
	};
	
	@Unique
	@Override
	public void addUnlockShape(ShapeType<? extends LivingEntity> type) {
		unlockedShapes.add(type);
	};
	
	@Unique
	@Override
	public void removeUnlockedShape(ShapeType<? extends LivingEntity> type) {
		unlockedShapes.remove(type);
	};
}
