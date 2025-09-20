package dev.tocraft.remorphed.permission;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * NeoForge implementation of the PermissionManager using NeoForge PermissionAPI
 */
public class NeoForgePermissionManager implements PermissionManager {
    
    // Cache for permission nodes to avoid recreating them
    private static final ConcurrentMap<String, PermissionNode<Boolean>> PERMISSION_NODES = new ConcurrentHashMap<>();
    
    public NeoForgePermissionManager() {
        // Register all permission nodes when the manager is created
        registerAllPermissionNodes();
    }
    
    private void registerAllPermissionNodes() {
        // Core permissions
        registerPermissionNode("remorphed.menu");
        registerPermissionNode("remorphed.morph");
        registerPermissionNode("remorphed.creative");
        registerPermissionNode("remorphed.bypass.lock");
        
        // Command permissions
        registerPermissionNode("remorphed.command.addShape");
        registerPermissionNode("remorphed.command.removeShape");
        registerPermissionNode("remorphed.command.clearShapes");
        registerPermissionNode("remorphed.command.hasShape");
        registerPermissionNode("remorphed.command.addSkin");
        registerPermissionNode("remorphed.command.removeSkin");
        registerPermissionNode("remorphed.command.clearSkins");
        registerPermissionNode("remorphed.command.hasSkin");
        
        // Register entity type permissions for all registered entities
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            ResourceLocation key = EntityType.getKey(entityType);
            if (key != null) {
                registerPermissionNode("remorphed.type." + key.toString());
            }
        });
        
        // Dynamic configuration permissions (0-20 range for common use cases)
        for (int i = 0; i <= 20; i++) {
            registerPermissionNode("remorphed.unlockKills." + i);
            registerPermissionNode("remorphed.playerUnlockKills." + i);
            registerPermissionNode("remorphed.killValue." + i);
            registerPermissionNode("remorphed.playerKillValue." + i);
        }
    }
    
    private void registerPermissionNode(String permission) {
        PermissionNode<Boolean> node = new PermissionNode<>("remorphed", permission, PermissionTypes.BOOLEAN, 
            (player, playerUUID, context) -> player != null && player.hasPermissions(2));
        
        PERMISSION_NODES.put(permission, node);
        
        // Note: NeoForge PermissionAPI doesn't have explicit registration
        // Permissions are registered when they're first checked
    }
    
    @Override
    public boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission) {
        // Get or create permission node
        PermissionNode<Boolean> node = PERMISSION_NODES.computeIfAbsent(permission, 
            key -> new PermissionNode<>("remorphed", key, PermissionTypes.BOOLEAN, 
                (player1, playerUUID, context) -> player1 != null && player1.hasPermissions(2)));
        
        // Check permission using NeoForge PermissionAPI
        return PermissionAPI.getPermission(player, node);
    }
}
