package dev.tocraft.remorphed.neoforge;

import net.neoforged.fml.common.Mod;
import dev.tocraft.remorphed.Remorphed;

@SuppressWarnings("unused")
@Mod(Remorphed.MODID)
public class RemorphedNeoForge {

    public RemorphedNeoForge() {
        new Remorphed().initialize();
    }
}
