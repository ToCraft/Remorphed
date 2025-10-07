package dev.tocraft.remorphed.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Client-side cache for permission results to avoid repeated server requests
 */
@Environment(EnvType.CLIENT)
public class ClientPermissionCache {

    private static final ConcurrentMap<String, Boolean> permissionCache = new ConcurrentHashMap<>();

    /**
     * Set a permission result in the cache
     *
     * @param permission    The permission node
     * @param hasPermission Whether the player has the permission
     */
    public static void setPermission(String permission, boolean hasPermission) {
        permissionCache.put(permission, hasPermission);
    }

    /**
     * Get a cached permission result
     *
     * @param permission The permission node
     * @return The cached permission result, or null if not cached
     */
    public static Boolean getPermission(String permission) {
        return permissionCache.get(permission);
    }

    /**
     * Check if a permission is cached
     *
     * @param permission The permission node
     * @return true if the permission is in the cache
     */
    public static boolean hasPermission(String permission) {
        return permissionCache.containsKey(permission);
    }

    /**
     * Clear the permission cache (called when player disconnects/reconnects)
     */
    public static void clearCache() {
        permissionCache.clear();
    }

    /**
     * Remove a specific permission from cache
     *
     * @param permission The permission to remove
     */
    public static void removePermission(String permission) {
        permissionCache.remove(permission);
    }
}
