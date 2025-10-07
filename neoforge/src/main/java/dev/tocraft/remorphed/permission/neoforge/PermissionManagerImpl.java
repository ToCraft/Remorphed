package dev.tocraft.remorphed.permission.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
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
        PermissionNode<Boolean> node = PERMISSION_NODES.computeIfAbsent(permission, 
            key -> new PermissionNode<>("remorphed", key, PermissionTypes.BOOLEAN, 
                (player1, playerUUID, context) -> player1 != null && player1.hasPermissions(2)));
        
        // Check permission using NeoForge PermissionAPI
        return PermissionAPI.getPermission(player, node);
    }
}
