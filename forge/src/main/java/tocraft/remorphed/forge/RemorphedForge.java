package tocraft.remorphed.forge;

import net.minecraftforge.fml.common.Mod;
import tocraft.remorphed.Remorphed;

@Mod(Remorphed.MODID)
public class RemorphedForge {

    public RemorphedForge() {
        new Remorphed().initialize();
    }
}
