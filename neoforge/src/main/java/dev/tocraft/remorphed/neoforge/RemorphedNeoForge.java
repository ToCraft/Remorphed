package dev.tocraft.remorphed.neoforge;

import dev.tocraft.remorphed.Remorphed;
import net.neoforged.fml.common.Mod;

@SuppressWarnings("unused")
@Mod(Remorphed.MODID)
public class RemorphedNeoForge {

    public RemorphedNeoForge() {
        new Remorphed().initialize();
    }
}
