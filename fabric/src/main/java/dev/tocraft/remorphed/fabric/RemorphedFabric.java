package dev.tocraft.remorphed.fabric;

import net.fabricmc.api.ModInitializer;
import dev.tocraft.remorphed.Remorphed;

public class RemorphedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        new Remorphed().initialize();
    }
}
