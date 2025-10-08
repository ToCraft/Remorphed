package dev.tocraft.remorphed.permission.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * NeoForge implementation of the PermissionManager using NeoForge PermissionAPI
 */
@SuppressWarnings("unused")
public class PermissionManagerImpl {

    // Cache for permission nodes to avoid recreating them
    private static final ConcurrentMap<String, PermissionNode<Boolean>> PERMISSION_NODES = new ConcurrentHashMap<>();

    public static boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission) {
        // Get or create permission node
        PermissionNode<Boolean> node = PERMISSION_NODES.get(permission);

        // Check permission using NeoForge PermissionAPI
        return node != null ? PermissionAPI.getPermission(player, node) : player.hasPermissions(2);
    }

    private static void createNode(@NotNull String permission) {
        PERMISSION_NODES.computeIfAbsent(permission,
                key -> new PermissionNode<>("remorphed", key, PermissionTypes.BOOLEAN,
                        (player, playerUUID, context) -> player != null && player.hasPermissions(2)));
    }

    public static void registerNodesEvent(PermissionGatherEvent.Nodes event) {
        // Core permissions
        PermissionManagerImpl.createNode("morph");
        PermissionManagerImpl.createNode("bypass.lock");

        // Command permissions (basic)
        PermissionManagerImpl.createNode("command.addShape");
        PermissionManagerImpl.createNode("command.removeShape");
        PermissionManagerImpl.createNode("command.clearShapes");
        PermissionManagerImpl.createNode("command.hasShape");
        PermissionManagerImpl.createNode("command.addSkin");
        PermissionManagerImpl.createNode("command.removeSkin");
        PermissionManagerImpl.createNode("command.clearSkins");
        PermissionManagerImpl.createNode("command.hasSkin");

        // Command permissions (.self variants)
        PermissionManagerImpl.createNode("command.addShape.self");
        PermissionManagerImpl.createNode("command.removeShape.self");
        PermissionManagerImpl.createNode("command.clearShapes.self");
        PermissionManagerImpl.createNode("command.hasShape.self");
        PermissionManagerImpl.createNode("command.addSkin.self");
        PermissionManagerImpl.createNode("command.removeSkin.self");
        PermissionManagerImpl.createNode("command.clearSkins.self");
        PermissionManagerImpl.createNode("command.hasSkin.self");

        // Command permissions (.others variants)
        PermissionManagerImpl.createNode("command.addShape.others");
        PermissionManagerImpl.createNode("command.removeShape.others");
        PermissionManagerImpl.createNode("command.clearShapes.others");
        PermissionManagerImpl.createNode("command.hasShape.others");
        PermissionManagerImpl.createNode("command.addSkin.others");
        PermissionManagerImpl.createNode("command.removeSkin.others");
        PermissionManagerImpl.createNode("command.clearSkins.others");
        PermissionManagerImpl.createNode("command.hasSkin.others");

        // Entity type permissions for all registered entities
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            ResourceLocation key = EntityType.getKey(entityType);
            PermissionManagerImpl.createNode("type." + key);
        });


        // register all
        event.addNodes(PERMISSION_NODES.values().toArray(PermissionNode[]::new));
    }
}
