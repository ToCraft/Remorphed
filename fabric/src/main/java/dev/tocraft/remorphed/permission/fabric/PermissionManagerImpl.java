package dev.tocraft.remorphed.permission.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric implementation of the PermissionManager using Fabric Permissions API
 */
@SuppressWarnings("unused")
public class PermissionManagerImpl {
    public static void initialize() {
    }
    
    public static boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission) {
        if (FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) {
            // Use Fabric Permissions API
            // Default to false (no permission) if no permission plugin is handling it
            return Permissions.check(player, permission, false);
        } else {
            // Fallback: check if player has operator permissions (level 2+)
            return player.hasPermissions(2);
        }
    }
}
