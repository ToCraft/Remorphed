package tocraft.remorphed.config;

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
    public Map<String, Integer> killToUnlockByType = new HashMap<>();
}
