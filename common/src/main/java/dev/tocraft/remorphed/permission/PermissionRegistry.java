package dev.tocraft.remorphed.permission;

import dev.tocraft.craftedcore.platform.PlatformData;
import dev.tocraft.remorphed.Remorphed;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Central registry for permission management across platforms
 */
public class PermissionRegistry {
    
    @Nullable
    private static PermissionManager instance;
    
    /**
     * Initialize the permission manager for the current platform
     */
    public static void initialize() {
        if (instance != null) {
            return; // Already initialized
        }
        
        try {
            // Try to load Fabric permission manager first
            try {
                Class<?> fabricClass = Class.forName("dev.tocraft.remorphed.permission.FabricPermissionManager");
                instance = (PermissionManager) fabricClass.getDeclaredConstructor().newInstance();
                Remorphed.LOGGER.info("Initialized Fabric permission manager");
                return;
            } catch (ClassNotFoundException ignored) {
                // Fabric not available, try NeoForge
            }
            
            // Try to load NeoForge permission manager
            try {
                Class<?> neoForgeClass = Class.forName("dev.tocraft.remorphed.permission.NeoForgePermissionManager");
                instance = (PermissionManager) neoForgeClass.getDeclaredConstructor().newInstance();
                Remorphed.LOGGER.info("Initialized NeoForge permission manager");
                return;
            } catch (ClassNotFoundException ignored) {
                // NeoForge not available either
            }
            
            // Neither platform-specific implementation found, use default
            instance = new DefaultPermissionManager();
            Remorphed.LOGGER.warn("Using fallback permission manager - no platform-specific implementation found");
            
        } catch (Exception e) {
            // Fallback to default implementation
            instance = new DefaultPermissionManager();
            Remorphed.LOGGER.warn("Failed to load platform-specific permission manager, using fallback: {}", e.getMessage());
        }
    }
    
    /**
     * Get the current permission manager instance
     * @return the permission manager instance
     */
    @NotNull
    public static PermissionManager getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }
    
    /**
     * Default permission manager that falls back to operator permissions
     */
    private static class DefaultPermissionManager implements PermissionManager {
        @Override
        public boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission) {
            // Fallback: check if player has operator permissions (level 2+)
            return player.hasPermissions(2);
        }
    }
}
