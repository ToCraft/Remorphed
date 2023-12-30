package tocraft.remorphed.config;

import tocraft.craftedcore.config.Config;
import tocraft.craftedcore.config.annotions.Synchronize;

public class RemorphedConfig implements Config {
    public int killToUnlock = 1;
    public boolean autoTransform = false;
    @Synchronize
    public boolean lockTransform = false;
}
