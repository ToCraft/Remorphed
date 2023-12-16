package tocraft.remorphed.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.PAPlayerDataProvider;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PAPlayerDataProvider {
	@Unique
	private String potion = "";
	@Unique
	private List<BlockPos> structures = new ArrayList<BlockPos>();
	@Unique
	private int cooldown = 0;
	
	// Stuff for giving potions
	private int distance = Remorphed.CONFIG.maxDistanceToStructure;
	private BlockPos nearest = null;

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
		tag.putInt("cooldown", cooldown);
		tag.putString("potion",  potion);
		ListTag list = new ListTag();
		structures.forEach(entry -> {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putInt("X", entry.getX());
			entryTag.putInt("Z", entry.getZ());
			list.add(entryTag);
		});
		if (list != null)
			tag.put("structures", list);
		return tag;
	}

	@Unique
	public void readData(CompoundTag tag) {
		structures.clear();
		cooldown = tag.getInt("cooldown");
		potion = tag.getString("potion");
		if ((ListTag) tag.get("structures") != null) {
			ListTag list = (ListTag) tag.get("structures");
			list.forEach(entry -> {
				if (entry instanceof CompoundTag) {
					int x = ((CompoundTag) entry).getInt("X");
					int z = ((CompoundTag) entry).getInt("Z");
					
					structures.add(new BlockPos(x, 0, z));
				}
			});
		}
	}
	
	private void reassignValues() {
		// Re-assign values to ensure everything works next time
		nearest = null;
		distance = Remorphed.CONFIG.maxDistanceToStructure;
	}
	
	@Unique
	@Override
	public void setPotion(String potion) {
		this.potion = potion;
	};
	
	@Unique
	@Override
	public String getPotion() {
		return potion;
	};
	
	@Unique
	@Override
	public void setStructures(List<BlockPos> structures) {
		this.structures = structures;
	};
	
	@Unique
	@Override
	public List<BlockPos> getStructures() {
		return structures;
	};
	
	@Unique
	@Override
	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	@Unique
	@Override
	public int getCooldown() {
		return cooldown;
	}
}
