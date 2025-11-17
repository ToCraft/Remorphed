package dev.tocraft.remorphed.screen;

import com.mojang.authlib.GameProfile;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.FakeClientPlayer;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pre-loading and caching of entity render states for the Remorphed menu.
 * This prevents the visual loading delay when opening the menu for the first time.
 *
 * The cache creates isolated, static snapshots of entity render states that won't be
 * affected by real-world entities near the player.
 */
@Environment(EnvType.CLIENT)
public class EntityRenderCache {

    // Static caches for entity and player render states
    private static final Map<ShapeType<?>, CachedEntityData> ENTITY_CACHE = new ConcurrentHashMap<>();
    private static final Map<GameProfile, CachedEntityData> PLAYER_CACHE = new ConcurrentHashMap<>();

    /**
     * Data container for cached entity information
     * NOTE: We store the actual entity, NOT the EntityRenderState, because
     * EntityRenderState objects are mutable and get updated by the rendering system.
     */
    public static class CachedEntityData {
        public final LivingEntity entity;

        public CachedEntityData(LivingEntity entity) {
            this.entity = entity;
        }
    }

    /**
     * Pre-loads ALL possible entity shapes for the given player.
     * This caches all entities regardless of unlock status to support creative mode switching.
     *
     * @param player The player context
     */
    public static void preloadEntities(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        // Cache ALL possible shapes, not just unlocked ones
        // This ensures entities are ready when switching to creative mode
        List<ShapeType<?>> allShapes = ShapeType.getAllTypes(player.level());

        for (ShapeType<?> type : allShapes) {
            // Skip if already cached (thread-safe check)
            if (ENTITY_CACHE.containsKey(type)) {
                continue;
            }

            try {
                // Create isolated entity instance
                Entity entity = type.create(minecraft.level, player);

                if (entity instanceof Mob mob) {
                    // Make entity completely static and isolated
                    prepareStaticEntity(mob);

                    // Cache the ENTITY itself (not render state!)
                    // Use putIfAbsent to avoid race conditions
                    ENTITY_CACHE.putIfAbsent(type, new CachedEntityData(mob));
                }
            } catch (Exception e) {
                Remorphed.LOGGER.warn("[Remorphed] Failed to pre-load entity for type {}: {}",
                    type.getEntityType().getDescriptionId(), e.getMessage());
            }
        }
    }

    /**
     * Pre-loads all unlocked player skins for the given player.
     * This should be called when the player joins a world.
     *
     * @param player The player whose unlocked skins to cache
     */
    public static void preloadPlayerSkins(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        List<GameProfile> unlockedSkins = Remorphed.getUnlockedSkins(player);

        for (GameProfile profile : unlockedSkins) {
            // Skip player's own skin
            if (profile.getId().equals(player.getUUID())) {
                continue;
            }

            // Skip if already cached (thread-safe check)
            if (PLAYER_CACHE.containsKey(profile)) {
                continue;
            }

            try {
                // Create isolated fake player instance
                FakeClientPlayer fakePlayer = new FakeClientPlayer(minecraft.level, profile);

                // Make player invulnerable (no setNoAi for player entities)
                fakePlayer.setInvulnerable(true);

                // Cache the ENTITY itself (not render state!)
                // Use putIfAbsent to avoid race conditions
                PLAYER_CACHE.putIfAbsent(profile, new CachedEntityData(fakePlayer));
            } catch (Exception e) {
                Remorphed.LOGGER.warn("[Remorphed] Failed to pre-load player skin for profile {}: {}",
                    profile.getName(), e.getMessage());
            }
        }
    }

    /**
     * Prepares an entity to be completely static and isolated from world state.
     *
     * @param mob The mob to prepare
     */
    private static void prepareStaticEntity(Mob mob) {
        // Disable AI and make invulnerable to prevent any updates
        mob.setNoAi(true);
        mob.setInvulnerable(true);

        // Fix slimes and magma cubes - set to smallest size
        if (mob instanceof Slime slime) {
            slime.setSize(1, true);
        } else if (mob instanceof MagmaCube magmaCube) {
            magmaCube.setSize(1, true);
        }

        // Enable glowing effect for menu display
        mob.setGlowingTag(true);
    }

    /**
     * Gets the cached render data for an entity type.
     * If not cached, returns null.
     *
     * @param type The shape type to get cached data for
     * @return The cached data, or null if not cached
     */
    @Nullable
    public static CachedEntityData getCachedEntity(ShapeType<?> type) {
        return ENTITY_CACHE.get(type);
    }

    /**
     * Gets the cached render data for a player skin.
     * If not cached, returns null.
     *
     * @param profile The game profile to get cached data for
     * @return The cached data, or null if not cached
     */
    @Nullable
    public static CachedEntityData getCachedPlayerSkin(GameProfile profile) {
        return PLAYER_CACHE.get(profile);
    }

    /**
     * Caches a single entity type on-demand.
     * Used when a player unlocks a new shape while in-world.
     *
     * @param type The shape type to cache
     * @param player The player context
     */
    public static void cacheEntity(ShapeType<?> type, Player player) {
        if (ENTITY_CACHE.containsKey(type)) {
            return; // Already cached
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        try {
            Entity entity = type.create(minecraft.level, player);

            if (entity instanceof Mob mob) {
                prepareStaticEntity(mob);
                // Use putIfAbsent to avoid race conditions
                ENTITY_CACHE.putIfAbsent(type, new CachedEntityData(mob));
            }
        } catch (Exception e) {
            Remorphed.LOGGER.warn("[Remorphed] Failed to cache entity on-demand: {}", type.getEntityType().getDescriptionId(), e);
        }
    }

    /**
     * Caches a single player skin on-demand.
     * Used when a player unlocks a new skin while in-world.
     *
     * @param profile The game profile to cache
     */
    public static void cachePlayerSkin(GameProfile profile) {
        if (PLAYER_CACHE.containsKey(profile)) {
            return; // Already cached
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        try {
            FakeClientPlayer fakePlayer = new FakeClientPlayer(minecraft.level, profile);
            fakePlayer.setInvulnerable(true);

            // Use putIfAbsent to avoid race conditions
            PLAYER_CACHE.putIfAbsent(profile, new CachedEntityData(fakePlayer));
        } catch (Exception e) {
            Remorphed.LOGGER.warn("[Remorphed] Failed to cache player skin on-demand: {}", profile.getName(), e);
        }
    }

    /**
     * Clears all cached entities.
     * This should be called when the player disconnects from a world.
     */
    public static void clearCache() {
        ENTITY_CACHE.clear();
        PLAYER_CACHE.clear();
    }

    /**
     * Gets the number of cached entities.
     *
     * @return The cache size
     */
    public static int getCachedEntityCount() {
        return ENTITY_CACHE.size();
    }

    /**
     * Gets the number of cached player skins.
     *
     * @return The cache size
     */
    public static int getCachedPlayerSkinCount() {
        return PLAYER_CACHE.size();
    }
}
