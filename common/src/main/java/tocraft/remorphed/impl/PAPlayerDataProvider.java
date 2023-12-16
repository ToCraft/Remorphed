package tocraft.remorphed.impl;

import java.util.List;

import net.minecraft.core.BlockPos;

public interface PAPlayerDataProvider {
	
	void setPotion(String potion);
	String getPotion();
	void setStructures(List<BlockPos> structures);
	List<BlockPos> getStructures();
	void setCooldown(int cooldown);
	int getCooldown();
}
