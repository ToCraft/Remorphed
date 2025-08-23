package dev.tocraft.remorphed.fabric;

import dev.tocraft.remorphed.Remorphed;
import net.fabricmc.api.ModInitializer;

public class RemorphedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        new Remorphed().initialize();
    }
}
