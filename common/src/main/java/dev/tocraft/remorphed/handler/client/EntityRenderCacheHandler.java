package dev.tocraft.remorphed.handler.client;

import dev.tocraft.craftedcore.event.client.ClientTickEvents;
import dev.tocraft.remorphed.screen.EntityRenderCache;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Handles pre-loading entity instances when the player joins a world.
 * Entity instances are cached so subsequent menu opens are instant.
 */
public class EntityRenderCacheHandler implements ClientTickEvents.Client {

    private boolean wasInWorld = false;
    private boolean hasStartedPreload = false;
    private int ticksInWorld = 0;

    // Incremental entity loading state
    private List<ShapeType<?>> pendingShapes = null;
    private int pendingIndex = 0;
    private static final int ENTITIES_PER_TICK = 3; // Tune this — lower = smoother, slower

    @Override
    public void tick(Minecraft client) {
        boolean isInWorld = client.level != null && client.player != null;

        if (!wasInWorld && isInWorld) {
            hasStartedPreload = false;
            ticksInWorld = 0;
            pendingShapes = null;
            pendingIndex = 0;
        }

        if (isInWorld && !hasStartedPreload) {
            ticksInWorld++;
            if (ticksInWorld >= 20) {
                hasStartedPreload = true;
                // Kick off skin loading immediately (non-blocking callbacks)
                EntityRenderCache.preloadPlayerSkins(client.player);
                // Initialize the incremental entity queue
                pendingShapes = ShapeType.getAllTypes(client.player.level());
                pendingIndex = 0;
            }
        }

        // Process a small batch of entities per tick
        if (pendingShapes != null && pendingIndex < pendingShapes.size() && isInWorld) {
            int limit = Math.min(pendingIndex + ENTITIES_PER_TICK, pendingShapes.size());
            while (pendingIndex < limit) {
                ShapeType<?> type = pendingShapes.get(pendingIndex);
                if (!EntityRenderCache.isCached(type)) {
                    EntityRenderCache.cacheEntity(type, client.player);
                }
                pendingIndex++;
            }

            // All done
            if (pendingIndex >= pendingShapes.size()) {
                pendingShapes = null;
            }
        }

        wasInWorld = isInWorld;
    }
}
