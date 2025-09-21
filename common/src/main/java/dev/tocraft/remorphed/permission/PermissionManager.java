package dev.tocraft.remorphed.permission;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * Cross-platform permission manager interface for ReMorphed mod.
 * Implementations handle platform-specific permission checks.
 */
public interface PermissionManager {
    
    /**
     * Check if a player has a specific permission node
     * @param player The player to check
     * @param permission The permission node to check
     * @return true if the player has the permission
     */
    boolean hasPermission(@NotNull ServerPlayer player, @NotNull String permission);
    
    /**
     * Check if a player has permission to access the ReMorphed menu
     * @param player The player to check
     * @return true if the player can access the menu
     */
    default boolean canAccessMenu(@NotNull ServerPlayer player) {
        return hasPermission(player, "remorphed.menu");
    }
    
    /**
     * Check if a player has permission to use a specific command
     * @param player The player to check
     * @param command The command name (e.g., "addShape", "removeShape")
     * @return true if the player can use the command
     */
    default boolean canUseCommand(@NotNull ServerPlayer player, @NotNull String command) {
        return hasPermission(player, "remorphed.command." + command);
    }
    
    /**
     * Check if a player has permission to use a command on themselves
     * @param player The player to check
     * @param command The command name (e.g., "addShape", "removeShape")
     * @return true if the player can use the command on themselves
     */
    default boolean canUseCommandOnSelf(@NotNull ServerPlayer player, @NotNull String command) {
        return hasPermission(player, "remorphed.command." + command + ".self") || 
               hasPermission(player, "remorphed.command." + command);
    }
    
    /**
     * Check if a player has permission to use a command on others
     * @param player The player to check
     * @param command The command name (e.g., "addShape", "removeShape")
     * @return true if the player can use the command on others
     */
    default boolean canUseCommandOnOthers(@NotNull ServerPlayer player, @NotNull String command) {
        return hasPermission(player, "remorphed.command." + command + ".others") || 
               hasPermission(player, "remorphed.command." + command);
    }
    
    /**
     * Check if a player has permission to use a command on a specific target
     * @param executor The player executing the command
     * @param target The target player
     * @param command The command name (e.g., "addShape", "removeShape")
     * @return true if the player can use the command on the target
     */
    default boolean canUseCommandOnTarget(@NotNull ServerPlayer executor, @NotNull ServerPlayer target, @NotNull String command) {
        if (executor.getUUID().equals(target.getUUID())) {
            // Using command on self
            return canUseCommandOnSelf(executor, command);
        } else {
            // Using command on others
            return canUseCommandOnOthers(executor, command);
        }
    }
    
    /**
     * Check if a player has permission to morph into a specific entity type
     * @param player The player to check
     * @param entityType The entity type to check
     * @return true if the player can morph into this entity type
     */
    default boolean canMorphIntoType(@NotNull ServerPlayer player, @NotNull EntityType<?> entityType) {
        String entityKey = EntityType.getKey(entityType).toString();
        return hasPermission(player, "remorphed.type." + entityKey);
    }
    
    /**
     * Get the kill requirement override for a player and entity type
     * @param player The player to check
     * @param entityType The entity type
     * @param defaultKills The default kill requirement
     * @return The overridden kill requirement or default if no override
     */
    default int getKillRequirement(@NotNull ServerPlayer player, @NotNull EntityType<?> entityType, int defaultKills) {
        // Check for specific overrides from permissions
        for (int i = 0; i <= 20; i++) { // Check reasonable range
            if (hasPermission(player, "remorphed.unlockKills." + i)) {
                return i;
            }
        }
        return defaultKills;
    }
    
    /**
     * Get the player kill requirement override for a player
     * @param player The player to check
     * @param defaultKills The default player kill requirement
     * @return The overridden player kill requirement or default if no override
     */
    default int getPlayerKillRequirement(@NotNull ServerPlayer player, int defaultKills) {
        // Check for specific overrides from permissions
        for (int i = 0; i <= 20; i++) { // Check reasonable range
            if (hasPermission(player, "remorphed.playerUnlockKills." + i)) {
                return i;
            }
        }
        return defaultKills;
    }
    
    /**
     * Get the kill value override for a player and entity type
     * @param player The player to check
     * @param entityType The entity type
     * @param defaultValue The default kill value
     * @return The overridden kill value or default if no override
     */
    default int getKillValue(@NotNull ServerPlayer player, @NotNull EntityType<?> entityType, int defaultValue) {
        // Check for specific overrides from permissions
        for (int i = 0; i <= 20; i++) { // Check reasonable range
            if (hasPermission(player, "remorphed.killValue." + i)) {
                return i;
            }
        }
        return defaultValue;
    }
    
    /**
     * Get the player kill value override for a player
     * @param player The player to check
     * @param defaultValue The default player kill value
     * @return The overridden player kill value or default if no override
     */
    default int getPlayerKillValue(@NotNull ServerPlayer player, int defaultValue) {
        // Check for specific overrides from permissions
        for (int i = 0; i <= 20; i++) { // Check reasonable range
            if (hasPermission(player, "remorphed.playerKillValue." + i)) {
                return i;
            }
        }
        return defaultValue;
    }
    
    /**
     * Check if a player can bypass creative mode restrictions
     * @param player The player to check
     * @return true if the player can use creative permissions
     */
    default boolean canUseCreativeMode(@NotNull ServerPlayer player) {
        return hasPermission(player, "remorphed.creative");
    }
    
    /**
     * Check if a player can bypass transform lock
     * @param player The player to check
     * @return true if the player can bypass transform lock
     */
    default boolean canBypassTransformLock(@NotNull ServerPlayer player) {
        return hasPermission(player, "remorphed.bypass.lock");
    }
    
    /**
     * Check if a player has basic morphing permission
     * @param player The player to check
     * @return true if the player can morph
     */
    default boolean canMorph(@NotNull ServerPlayer player) {
        return hasPermission(player, "remorphed.morph");
    }
}
