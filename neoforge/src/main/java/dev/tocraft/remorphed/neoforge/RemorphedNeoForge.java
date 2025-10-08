package dev.tocraft.remorphed.neoforge;

import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.permission.neoforge.PermissionManagerImpl;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

@SuppressWarnings("unused")
@Mod(Remorphed.MODID)
public class RemorphedNeoForge {

    public RemorphedNeoForge() {
        NeoForge.EVENT_BUS.addListener(PermissionGatherEvent.Nodes.class, PermissionManagerImpl::registerNodesEvent);
        new Remorphed().initialize();
    }
}
