package dev.tocraft.remorphed.neoforge;

import dev.tocraft.craftedcore.permission.neoforge.PermissionCheckerImpl;
import dev.tocraft.remorphed.Remorphed;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Mod(Remorphed.MODID)
public class RemorphedNeoForge {
    public RemorphedNeoForge() {
        NeoForge.EVENT_BUS.addListener(PermissionGatherEvent.Nodes.class, RemorphedNeoForge::registerNodesEvent);
        new Remorphed().initialize();
    }

    public static void registerNodesEvent(PermissionGatherEvent.@NotNull Nodes event) {
        List<PermissionNode<@NotNull Boolean>> nodes = new ArrayList<>();

        // Core permissions
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "morph"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "bypass.lock"));

        // Command permissions (basic)
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.addShape"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.removeShape"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.clearShapes"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.hasShape"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.addSkin"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.removeSkin"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.clearSkins"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.hasSkin"));

        // Command permissions (.self variants)
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.addShape.self"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.removeShape.self"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.clearShapes.self"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.hasShape.self"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.addSkin.self"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.removeSkin.self"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.clearSkins.self"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.hasSkin.self"));

        // Command permissions (.others variants)
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.addShape.others"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.removeShape.others"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.clearShapes.others"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.hasShape.others"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.addSkin.others"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.removeSkin.others"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.clearSkins.others"));
        nodes.add(PermissionCheckerImpl.createNode("remorphed", "command.hasSkin.others"));

        // Entity type permissions for all registered entities
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            Identifier key = EntityType.getKey(entityType);
            nodes.add(PermissionCheckerImpl.createNode("remorphed", "type." + key));
        });

        // register all
        event.addNodes(nodes.toArray(PermissionNode[]::new));
    }
}
