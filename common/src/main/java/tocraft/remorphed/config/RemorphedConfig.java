package tocraft.remorphed.config;

import net.minecraft.world.entity.EntityType;
import tocraft.craftedcore.config.Config;
import tocraft.craftedcore.config.annotions.Synchronize;

import java.util.HashMap;
import java.util.Map;

public class RemorphedConfig implements Config {
    public int killToUnlock = 1;
    public boolean autoTransform = false;
    @Synchronize
    public boolean lockTransform = false;
    public boolean unlockFriendlyNormal = false;
    public Map<String, Integer> killToUnlockByType = new HashMap<>() {
        {
            put("minecraft:ender_dragon", 2);
            put("minecraft:wither", 2);
        }
    };
}
