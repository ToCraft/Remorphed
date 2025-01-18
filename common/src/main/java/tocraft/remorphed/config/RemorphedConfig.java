package tocraft.remorphed.config;

import tocraft.craftedcore.config.Config;
import tocraft.craftedcore.config.annotions.Comment;
import tocraft.craftedcore.config.annotions.Synchronize;
import tocraft.remorphed.Remorphed;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("CanBeFinal")
public class RemorphedConfig implements Config {
    @Comment("<<< General >>>")
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
    @Comment("\n<<< Menu Customization >>>")
    public boolean show_variants_by_default = false;
    public boolean show_traits_by_default = true;
    @Comment("scale the shapes in the menu")
    public int entity_size = 20;
    public int shapes_per_row = 6;
    @Comment("Whether the selected mob shall be displayed as the first mob")
    public boolean sort_selected = false;
    @Comment("Whether the menu should automatically focus on the selected shape")
    public boolean focus_selected = true;
    @Comment("The width of the rows in the menu")
    public int row_width = 330;

    @Override
    public String getName() {
        return Remorphed.MODID;
    }
}
