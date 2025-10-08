package dev.tocraft.remorphed.permission.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Clean Fabric implementation of the PermissionManager using Fabric Permissions API
 * Based on the clean approach used in OldSchoolJail
 */
@SuppressWarnings("unused")
public class PermissionManagerImpl {
    public static boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission) {
        // Try to use Fabric Permissions API if available
        try {
            return Permissions.check(player, permission, 2);
        } catch (Throwable e) {
            // Permissions API not available, fall back to OP level 2
            return player.hasPermissions(2);
        }
    }
}
