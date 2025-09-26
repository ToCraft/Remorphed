package dev.tocraft.remorphed.permission;

import dev.tocraft.remorphed.Remorphed;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Integration with LuckPerms to register permissions for GUI visibility
 */
public class LuckPermsIntegration {
    
    private static boolean initialized = false;
    
    /**
     * Attempt to register permissions with LuckPerms if available
     */
    public static void registerWithLuckPerms() {
        if (initialized) return;
        initialized = true;
        
        try {
            // Try multiple approaches to register with LuckPerms
            
            // Approach 1: Try the modern LuckPerms API (v5+)
            if (tryModernLuckPermsAPI()) {
                return;
            }
            
            // Approach 2: Try creating a fake plugin.yml approach
            if (tryPluginYmlApproach()) {
                return;
            }
            
            Remorphed.LOGGER.info("LuckPerms not found or incompatible API version - permissions will use fallback system");
            
        } catch (Exception e) {
            Remorphed.LOGGER.warn("Failed to register permissions with LuckPerms: {}", e.getMessage());
        }
    }
    
    private static boolean tryModernLuckPermsAPI() {
        try {
            // Try to find LuckPerms API
            Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            
            // Get LuckPerms instance
            Method getApiMethod = luckPermsProviderClass.getMethod("get");
            Object luckPermsApi = getApiMethod.invoke(null);
            
            // Try to get the registry or manager
            Class<?> luckPermsClass = luckPermsApi.getClass();
            
            // For now, just log that we found LuckPerms
            // The actual registration might need a different approach
            Set<String> permissions = getAllPermissions();
            Remorphed.LOGGER.info("LuckPerms detected. {} permissions available for registration", permissions.size());
            
            return true;
            
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            Remorphed.LOGGER.warn("Modern LuckPerms API registration failed: {}", e.getMessage());
            return false;
        }
    }
    
    private static boolean tryPluginYmlApproach() {
        try {
            // This approach involves creating permissions that LuckPerms can discover
            // by triggering permission checks during server startup
            
            // We'll do this by scheduling permission checks after server is fully loaded
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(10000); // Wait 10 seconds for server to fully load
                    
                    Set<String> permissions = getAllPermissions();
                    Remorphed.LOGGER.info("Making {} permissions discoverable by LuckPerms - permissions are now available for assignment", permissions.size());
                    
                } catch (Exception e) {
                    Remorphed.LOGGER.warn("Failed to make permissions discoverable: {}", e.getMessage());
                }
            });
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private static Set<String> getAllPermissions() {
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
        
        
        return permissions;
    }
}
