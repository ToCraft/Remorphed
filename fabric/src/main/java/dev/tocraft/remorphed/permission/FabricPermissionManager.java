package dev.tocraft.remorphed.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Fabric implementation of the PermissionManager using Fabric Permissions API
 */
public class FabricPermissionManager implements PermissionManager {
    
    private static final Set<String> REGISTERED_PERMISSIONS = new HashSet<>();
    private static boolean initialized = false;
    
    public FabricPermissionManager() {
        // Register all permission nodes when the manager is created
        if (!initialized) {
            registerPermissionNodes();
            initialized = true;
        }
    }
    
    private void registerPermissionNodes() {
        // For Fabric, we need to "prime" the permissions by checking them once
        // This makes them discoverable by LuckPerms and other permission plugins
        CompletableFuture.runAsync(() -> {
            try {
                // Wait a bit for the server to fully start
                Thread.sleep(5000);
                
                // Create a set of all permissions to register
                Set<String> permissions = new HashSet<>();
                
                // Core permissions
                permissions.add("remorphed.morph");
                permissions.add("remorphed.bypass.lock");
                
                // Command permissions
                permissions.add("remorphed.command.addShape");
                permissions.add("remorphed.command.removeShape");
                permissions.add("remorphed.command.clearShapes");
                permissions.add("remorphed.command.hasShape");
                permissions.add("remorphed.command.addSkin");
                permissions.add("remorphed.command.removeSkin");
                permissions.add("remorphed.command.clearSkins");
                permissions.add("remorphed.command.hasSkin");
                
                // Entity type permissions for all registered entities
                BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
                    ResourceLocation key = EntityType.getKey(entityType);
                    if (key != null) {
                        permissions.add("remorphed.type." + key.toString());
                    }
                });
                
                
                // For Fabric, permissions are automatically registered when first checked
                // We'll just add them to our registered set for logging
                REGISTERED_PERMISSIONS.addAll(permissions);
                
                // Permissions registered - no need to log this as it's not actionable
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    @Override
    public boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission) {
        // Use Fabric Permissions API
        // Default to false (no permission) if no permission plugin is handling it
        return Permissions.check(player, permission, false);
    }
}
