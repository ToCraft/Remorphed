package dev.tocraft.remorphed.network;

import dev.tocraft.craftedcore.network.ModernNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;

/**
 * Network packet for checking permissions - client-side methods only
 */
public class PermissionCheckPacket {

    /**
     * Send a permission check request to the server
     *
     * @param permission The permission to check
     */
    @Environment(EnvType.CLIENT)
    public static void sendPermissionCheck(String permission) {
        CompoundTag tag = new CompoundTag();
        tag.putString("permission", permission);
        ModernNetworking.sendToServer(NetworkHandler.PERMISSION_CHECK, tag);
    }

    /**
     * Handle permission check response on client side
     *
     * @param context The network context
     * @param tag     The response data
     */
    @Environment(EnvType.CLIENT)
    public static void handlePermissionResponse(ModernNetworking.Context context, CompoundTag tag) {
        String permission = tag.getString("permission").orElse("");
        boolean hasPermission = tag.getBoolean("hasPermission").orElse(false);

        // Store the permission result for client-side use
        ClientPermissionCache.setPermission(permission, hasPermission);

    }
}
