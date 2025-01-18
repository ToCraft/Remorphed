package tocraft.remorphed.config;

import tocraft.craftedcore.config.Config;
import tocraft.craftedcore.config.annotions.Comment;
import tocraft.craftedcore.config.annotions.Synchronize;
import tocraft.remorphed.Remorphed;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("CanBeFinal")
public class RemorphedConfig implements Config {
    @Synchronize
    public boolean creativeUnlockAll = true;
    public int killToUnlock = 1;
    public int killToUnlockPlayers = 2;
    public boolean autoTransform = false;
    @Synchronize
    public boolean lockTransform = false;
    @Comment("Whether friendly mobs should be unlocked by the woodwalkers mechanism instead")
    public boolean unlockFriendlyNormal = false;
    public Map<String, Integer> killToUnlockByType = new HashMap<>() {
        {
            put("minecraft:ender_dragon", 2);
            put("minecraft:wither", 2);
        }
    };

    @Override
    public String getName() {
        return Remorphed.MODID;
    }
}
