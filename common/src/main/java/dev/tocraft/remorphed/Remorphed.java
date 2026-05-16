package dev.tocraft.remorphed;

import com.mojang.authlib.GameProfile;
import dev.tocraft.craftedcore.config.ConfigLoader;
import dev.tocraft.craftedcore.event.common.CommandEvents;
import dev.tocraft.craftedcore.event.common.EntityEvents;
import dev.tocraft.craftedcore.event.common.PlayerEvents;
import dev.tocraft.craftedcore.network.ModernNetworking;
import dev.tocraft.craftedcore.platform.PlatformData;
import dev.tocraft.craftedcore.platform.VersionChecker;
import dev.tocraft.remorphed.command.RemorphedCommand;
import dev.tocraft.remorphed.config.RemorphedConfig;
import dev.tocraft.remorphed.handler.LivingDeathHandler;
import dev.tocraft.remorphed.handler.PlayerRespawnHandler;
import dev.tocraft.remorphed.handler.SwapShapeCallback;
import dev.tocraft.remorphed.handler.UnlockShapeCallback;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.remorphed.network.NetworkHandler;
import dev.tocraft.remorphed.permission.PermissionManager;
import dev.tocraft.walkers.Walkers;
import dev.tocraft.walkers.api.events.ShapeEvents;
import dev.tocraft.walkers.api.platform.ApiLevel;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Remorphed {
    @ApiStatus.Internal
    public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
    public static final String MODID = "remorphed";
    public static final RemorphedConfig CONFIG = ConfigLoader.register(MODID);
    public static boolean displayDataInMenu = CONFIG.show_traits_by_default;
    @ApiStatus.Internal
    public static final boolean foundSkinShifter = PlatformData.isModLoaded("skinshifter");

    public void initialize() {
        Walkers.CONFIG.unlockEveryVariant = CONFIG.allow_variant_selection;

        ShapeEvents.UNLOCK_SHAPE.register(new UnlockShapeCallback());
        ShapeEvents.SWAP_SHAPE.register(new SwapShapeCallback());
        if (!CONFIG.unlockFriendlyNormal) {
            if (CONFIG.allow_variant_selection) {
                ApiLevel.setApiLevel(ApiLevel.MORPHING_AND_VARIANTS_MENU_ONLY);
            } else {
                ApiLevel.setApiLevel(ApiLevel.MORPHING_ONLY);
            }
        } else if (!CONFIG.allow_variant_selection) {
            ApiLevel.setApiLevel(ApiLevel.MORPHING_AND_VARIANTS_MENU_ONLY);
        }

        // add DarkShadow_2k to devs (for creating the special shape icon and concepts)
        //noinspection UnstableApiUsage
        Walkers.devs.add(UUID.fromString("74b6d9b3-c8c1-40db-ab82-ccc290d1aa03"));

        // add LugFong (FugLong) to devs (for adding permisisons and updating menu rendering)
        //noinspection UnstableApiUsage
        Walkers.devs.add(UUID.fromString("d014c6c7-1c15-46b7-94e1-a5dd28f9425e"));

        VersionChecker.registerModrinthChecker(MODID, "remorphed", Component.literal("Remorphed"));

        if (PlatformData.getEnv() == PlatformData.Env.CLIENT) new RemorphedClient().initialize();

        NetworkHandler.registerPacketReceiver();

        CommandEvents.REGISTRATION.register(new RemorphedCommand());
        EntityEvents.LIVING_DEATH.register(new LivingDeathHandler());
        PlayerEvents.PLAYER_RESPAWN.register(new PlayerRespawnHandler());

        // allow unlocking friendly mobs via the "normal" method
        Walkers.CONFIG.unlockOverridesCurrentShape = Remorphed.CONFIG.unlockFriendlyNormal;
        Walkers.CONFIG.save();

        // Sync favorites
        PlayerEvents.PLAYER_JOIN.register(NetworkHandler::sendFavoriteSync);
    }

    public static boolean canUseEveryShape(@NotNull Player player) {
        // Always use config-based creative logic (creative permission removed)
        return player.isCreative() && CONFIG.creativeUnlockAll;
    }

    public static boolean canUseShape(Player player, ShapeType<?> type) {
        // If permissions are enabled, use permission-based logic
        if (CONFIG.usePermissions && player instanceof ServerPlayer serverPlayer) {
            // Check basic morph permission
            if (!PermissionManager.canMorph(serverPlayer)) {
                return false;
            }

            // Check entity type specific permission - this takes precedence over creative permissions
            if (type != null && !PermissionManager.canMorphIntoType(serverPlayer, type.getEntityType())) {
                return false;
            }

            // Check bypass transform lock permission
            if (Remorphed.CONFIG.lockTransform && !PermissionManager.canBypassTransformLock(serverPlayer)) {
                return false;
            }

            // Check if player can use all shapes (config-based creative logic)
            if (canUseEveryShape(player)) {
                return true;
            }

            // For permission-enabled players without creative permission, check kill requirements
            return !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(player, type.getEntityType()) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(player, type.getEntityType()));
        }

        // If permissions are disabled, use original config-based behavior
        return canUseEveryShape(player) || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(player, type.getEntityType()) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(player, type.getEntityType()));
    }

    public static boolean canUseShape(Player player, EntityType<?> type) {
        // If permissions are enabled, use permission-based logic
        if (CONFIG.usePermissions && player instanceof ServerPlayer serverPlayer) {
            // Check basic morph permission
            if (!PermissionManager.canMorph(serverPlayer)) {
                return false;
            }

            // Check entity type specific permission - this takes precedence over creative permissions
            if (type != null && !PermissionManager.canMorphIntoType(serverPlayer, type)) {
                return false;
            }

            // Check bypass transform lock permission
            if (Remorphed.CONFIG.lockTransform && !PermissionManager.canBypassTransformLock(serverPlayer)) {
                return false;
            }

            // Check if player can use all shapes (config-based creative logic)
            if (canUseEveryShape(player)) {
                return true;
            }

            // For permission-enabled players without creative permission, check kill requirements
            return !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(player, type) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(player, type));
        }

        // If permissions are disabled, use original config-based behavior
        return canUseEveryShape(player) || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(player, type) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(player, type));
    }

    public static List<ShapeType<?>> getUnlockedShapes(Player player) {
        // Always filter by canUseShape to respect entity type permissions
        List<ShapeType<?>> unlocked = new ArrayList<>();
        for (ShapeType<?> shapeType : ShapeType.getAllTypes(player.level())) {
            if (!unlocked.contains(shapeType) && canUseShape(player, shapeType)) {
                unlocked.add(shapeType);
            }
        }
        return unlocked;
    }

    @Contract("_ -> new")
    public static @NotNull List<GameProfile> getUnlockedSkins(Player player) {
        int killRequirement = getPlayerKillRequirement(player);
        return new ArrayList<>(PlayerMorph.getUnlockedSkinIds(player).keySet().stream().filter(skinId -> (PlayerMorph.getPlayerKills(player, skinId) >= killRequirement || killRequirement == 0) && killRequirement != -1).map(id -> getGameProfile(player, id)).filter(Optional::isPresent).map(Optional::get).toList());
    }

    public static int getKillToUnlock(EntityType<?> type) {
        return Remorphed.CONFIG.killToUnlockByType.getOrDefault(EntityType.getKey(type).toString(), Remorphed.CONFIG.killToUnlock);
    }

    public static int getKillToUnlock(Player player, EntityType<?> type) {
        return getKillToUnlock(type);
    }

    public static int getKillValue(EntityType<?> type) {
        return Remorphed.CONFIG.killValueByType.getOrDefault(EntityType.getKey(type).toString(), Remorphed.CONFIG.killValue);
    }

    public static int getPlayerKillRequirement(Player player) {
        return CONFIG.killToUnlockPlayers;
    }

    public static void sync(ServerPlayer player) {
        sync(player, player);
    }

    public static void sync(ServerPlayer changed, ServerPlayer packetTarget) {
        CompoundTag compoundTag = new CompoundTag();

        // serialize current shape data to tag if it exists
        Map<EntityType<? extends LivingEntity>, Integer> unlockedShapes = PlayerMorph.getUnlockedShapes(changed);
        ListTag shapesList = new ListTag();
        unlockedShapes.forEach((shape, killAmount) -> {
            if (killAmount > 0 && shape != null) {
                CompoundTag compound = new CompoundTag();
                compound.putString("id", EntityType.getKey(shape).toString());
                compound.putInt("killAmount", killAmount);
                shapesList.add(compound);
            }
        });
        if (!shapesList.isEmpty()) compoundTag.put("UnlockedShapes", shapesList);

        Map<UUID, Integer> unlockedSkins = PlayerMorph.getUnlockedSkinIds(changed);
        ListTag skinsList = new ListTag();
        unlockedSkins.forEach((skinId, killAmount) -> {
            if (killAmount > 0 && skinId != null) {
                CompoundTag compound = new CompoundTag();
                compound.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                compound.putInt("killAmount", killAmount);
                skinsList.add(compound);
            }
        });
        if (!skinsList.isEmpty()) compoundTag.put("UnlockedSkins", skinsList);

        ListTag morphCounter = new ListTag();
        PlayerMorph.getShapeCounter(changed).forEach((type, count) -> {
            if (count > 0 && type != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putBoolean("isSkin", false);
                entryTag.putString("id", EntityType.getKey(type).toString());
                entryTag.putInt("counter", count);
                morphCounter.add(entryTag);
            }
        });
        PlayerMorph.getSkinCounter(changed).forEach((skinId, count) -> {
            if (count > 0 && skinId != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putBoolean("isSkin", true);
                entryTag.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                entryTag.putInt("counter", count);
                morphCounter.add(entryTag);
            }
        });
        if (!morphCounter.isEmpty()) {
            compoundTag.put("MorphCounter", morphCounter);
        }

        compoundTag.putIntArray("uuid", UUIDUtil.uuidToIntArray(changed.getUUID()));
        ModernNetworking.sendToPlayer(packetTarget, NetworkHandler.UNLOCKED_SYNC, compoundTag);
    }

    public static @NotNull Optional<GameProfile> getGameProfile(Player owner, UUID uuid) {
        // get ProfileResolver depending on Env
        final ProfileResolver profileResolver = switch (PlatformData.getEnv()) {
            case CLIENT -> Minecraft.getInstance().services().profileResolver();
            case SERVER -> ((ServerPlayer) owner).level().getServer().services().profileResolver();
        };

        return profileResolver.fetchById(uuid);
    }

    @Contract("_ -> new")
    public static @NotNull Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MODID, name);
    }

    public static void spawnWalkersParticles(Player player) {
        // spawn particles
        if (Walkers.CONFIG.emit_particles && !player.isSpectator() && player.level() instanceof ServerLevel l) {
            l.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 25, .5, 1.0, .5, .75);
            l.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 25, .5, 1.0, .5, .75);
            l.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1, player.getZ(), 25, .5, 1.0, .5, .7);
        }
    }
}
