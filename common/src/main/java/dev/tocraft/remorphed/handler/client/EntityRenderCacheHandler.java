package dev.tocraft.remorphed.handler.client;

import com.mojang.authlib.GameProfile;
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

/**
 * Handles pre-loading entity instances when the player joins a world.
 * Entity instances are cached so subsequent menu opens are instant.
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
     * Pre-loads entity instances for the given player.
     * This runs on the main client thread.
     *
     * @param player The player to pre-load entities for
     */
    private void preloadForPlayer(LocalPlayer player) {
        if (player == null) {
            return;
        }

        try {
            // Cache entity instances
            EntityRenderCache.preloadEntities(player);
            EntityRenderCache.preloadPlayerSkins(player);

            // Start background pre-rendering
            startPreRendering(player);
        } catch (Exception e) {
            // Silent - don't spam logs
        }
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
            Minecraft.getInstance().setScreen(new EntityPreloadScreen(
                    entitiesToRender,
                    unlockedSkins
            ));
        }
    }
}
