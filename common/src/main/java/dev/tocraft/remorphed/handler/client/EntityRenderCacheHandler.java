package dev.tocraft.remorphed.handler.client;

import dev.tocraft.craftedcore.event.client.ClientTickEvents;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.screen.EntityRenderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Handles pre-loading entity render states when the player joins a world.
 * This ensures the menu opens instantly without visual loading delays.
 */
public class EntityRenderCacheHandler implements ClientTickEvents.Client {

    private boolean wasInWorld = false;
    private boolean hasPreloaded = false;
    private int ticksInWorld = 0;

    @Override
    public void tick(Minecraft client) {
        boolean isInWorld = client.level != null && client.player != null;

        // Detect transition from not-in-world to in-world (world join)
        if (!wasInWorld && isInWorld) {
            // Reset state for new world
            hasPreloaded = false;
            ticksInWorld = 0;
        }

        // Pre-load after being in world for a few ticks (ensures world is fully loaded)
        if (isInWorld && !hasPreloaded) {
            ticksInWorld++;

            // Wait 20 ticks (1 second) after joining to ensure everything is loaded
            if (ticksInWorld >= 20) {
                hasPreloaded = true;
                preloadForPlayer(client.player);
            }
        }

        wasInWorld = isInWorld;
    }

    /**
     * Pre-loads all entity and player skin render states for the given player.
     * This runs on the main client thread.
     *
     * @param player The player to pre-load entities for
     */
    private void preloadForPlayer(LocalPlayer player) {
        if (player == null) {
            return;
        }

        try {
            // Pre-load entities and player skins on main thread
            EntityRenderCache.preloadEntities(player);
            EntityRenderCache.preloadPlayerSkins(player);
        } catch (Exception e) {
            Remorphed.LOGGER.error("[Remorphed] Failed to pre-load entity render cache", e);
        }
    }
}
