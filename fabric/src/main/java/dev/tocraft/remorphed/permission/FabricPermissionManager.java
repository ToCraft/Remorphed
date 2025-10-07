package dev.tocraft.remorphed.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Clean Fabric implementation of the PermissionManager using Fabric Permissions API
 * Based on the clean approach used in OldSchoolJail
 */
public class FabricPermissionManager implements PermissionManager {
    
    public FabricPermissionManager() {
        // No initialization needed - permissions are discovered naturally when first checked
    }
    
    @Override
    public boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission) {
        // Try to use Fabric Permissions API if available
        try {
            return Permissions.check(player, permission, 2);
        } catch (NoClassDefFoundError | Exception e) {
            // Permissions API not available, fall back to OP level 2
            return player.hasPermissions(2);
        }
    }
}
