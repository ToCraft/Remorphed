package dev.tocraft.remorphed;

import com.mojang.authlib.GameProfile;
import dev.tocraft.skinshifter.data.SkinPlayerData;
import net.fabricmc.api.EnvType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tocraft.walkers.Walkers;
import tocraft.walkers.api.events.ShapeEvents;
import tocraft.walkers.api.platform.ApiLevel;
import tocraft.walkers.api.variant.ShapeType;

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
        ShapeEvents.UNLOCK_SHAPE.register(new UnlockShapeCallback());
        ShapeEvents.SWAP_SHAPE.register(new SwapShapeCallback());
        if (!CONFIG.unlockFriendlyNormal) {
            ApiLevel.setApiLevel(ApiLevel.MORPHING_AND_VARIANTS_MENU_ONLY);
        }

        // add DarkShadow_2k to devs (for creating the special shape icon and concepts)
        //noinspection UnstableApiUsage
        Walkers.devs.add(UUID.fromString("74b6d9b3-c8c1-40db-ab82-ccc290d1aa03"));

        VersionChecker.registerModrinthChecker(MODID, "remorphed", Component.literal("Remorphed"));

        if (PlatformData.getEnv() == EnvType.CLIENT) new RemorphedClient().initialize();

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
        return player.isCreative() && CONFIG.creativeUnlockAll;
    }

    public static boolean canUseShape(Player player, ShapeType<?> type) {
        return canUseEveryShape(player) || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(type.getEntityType()) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(type.getEntityType()));
    }

    public static boolean canUseShape(Player player, EntityType<?> type) {
        return canUseEveryShape(player) || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(type) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(type));
    }

    public static List<ShapeType<?>> getUnlockedShapes(Player player) {
        if (canUseEveryShape(player)) {
            return ShapeType.getAllTypes(player.level());
        } else if (Walkers.CONFIG.unlockEveryVariant) {
            List<ShapeType<?>> unlocked = new ArrayList<>();
            for (ShapeType<?> shapeType : ShapeType.getAllTypes(player.level())) {
                if (!unlocked.contains(shapeType) && canUseShape(player, shapeType)) unlocked.add(shapeType);
            }
            return unlocked;
        } else {
            return new ArrayList<>(PlayerMorph.getUnlockedShapes(player).keySet().stream().filter(type -> canUseShape(player, type)).toList());
        }
    }

    @Contract("_ -> new")
    public static @NotNull List<GameProfile> getUnlockedSkins(Player player) {
        return new ArrayList<>(PlayerMorph.getUnlockedSkinIds(player).keySet().stream().filter(skinId -> (PlayerMorph.getPlayerKills(player, skinId) >= CONFIG.killToUnlockPlayers || CONFIG.killToUnlockPlayers == 0) && CONFIG.killToUnlockPlayers != -1).map(id -> SkinPlayerData.getSkinProfile(id).getNow(Optional.empty()).orElse(null)).filter(Objects::nonNull).toList());
    }

    public static int getKillToUnlock(EntityType<?> type) {
        return Remorphed.CONFIG.killToUnlockByType.getOrDefault(EntityType.getKey(type).toString(), Remorphed.CONFIG.killToUnlock);

    }

    public static int getKillValue(EntityType<?> type) {
        return Remorphed.CONFIG.killValueByType.getOrDefault(EntityType.getKey(type).toString(), Remorphed.CONFIG.killValue);
    }

    public static void sync(ServerPlayer player) {
        sync(player, player);
    }

    public static void sync(ServerPlayer changed, ServerPlayer packetTarget) {
        CompoundTag compoundTag = new CompoundTag();

        // serialize current shape data to tag if it exists
        Map<ShapeType<?>, Integer> unlockedShapes = PlayerMorph.getUnlockedShapes(changed);
        ListTag shapesList = new ListTag();
        unlockedShapes.forEach((shape, killAmount) -> {
            if (killAmount > 0 && shape != null) {
                CompoundTag compound = new CompoundTag();
                compound.putString("id", EntityType.getKey(shape.getEntityType()).toString());
                compound.putInt("variant", shape.getVariantData());
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
                entryTag.putString("id", EntityType.getKey(type.getEntityType()).toString());
                entryTag.putInt("variant", type.getVariantData());
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

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
