package dev.tocraft.remorphed.handler.client;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.tocraft.craftedcore.event.client.ClientTickEvents;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.screen.EntityPreloadScreen;
import dev.tocraft.remorphed.screen.EntityRenderCache;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    private void startPreRendering(LocalPlayer player) {
        List<ShapeType<?>> currentUnlockedShapes = Remorphed.getUnlockedShapes(player);
        List<GameProfile> unlockedSkins = Remorphed.getUnlockedSkins(player);

        // Apply the SAME filtering logic as RemorphedMenu lines 118-126
        // This filters to one variant per entity type for the CURRENT mode (survival/creative)
        List<ShapeType<?>> currentFilteredShapes = new ArrayList<>();
        Set<net.minecraft.world.entity.EntityType<?>> seenTypes = new HashSet<>();
        for (ShapeType<?> shapeType : currentUnlockedShapes) {
            if (seenTypes.add(shapeType.getEntityType())) {
                currentFilteredShapes.add(shapeType);
            }
        }

        // Gather entities for the current filtered list (for correct ID mapping)
        List<Mob> entitiesToRender = new ArrayList<>();

        for (ShapeType<?> type : currentFilteredShapes) {
            EntityRenderCache.CachedEntityData cached = EntityRenderCache.getCachedEntity(type);
            if (cached != null && cached.entity() instanceof Mob mob) {
                entitiesToRender.add(mob);
            }
        }

        if (!entitiesToRender.isEmpty()) {
            // Open invisible pre-render screen with shape types for ID calculation
            /*Minecraft.getInstance().setScreen(new EntityPreloadScreen(
                    entitiesToRender,
                    unlockedSkins
            ));*/
        }
    }
}
