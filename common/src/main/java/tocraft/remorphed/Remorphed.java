package tocraft.remorphed;

import net.fabricmc.api.EnvType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.event.common.CommandEvents;
import tocraft.craftedcore.event.common.EntityEvents;
import tocraft.craftedcore.event.common.PlayerEvents;
import tocraft.craftedcore.network.ModernNetworking;
import tocraft.craftedcore.patched.CEntity;
import tocraft.craftedcore.patched.Identifier;
import tocraft.craftedcore.patched.TComponent;
import tocraft.craftedcore.platform.PlatformData;
import tocraft.craftedcore.platform.PlayerProfile;
import tocraft.craftedcore.platform.VersionChecker;
import tocraft.remorphed.command.RemorphedCommand;
import tocraft.remorphed.config.RemorphedConfig;
import tocraft.remorphed.handler.LivingDeathHandler;
import tocraft.remorphed.handler.PlayerRespawnHandler;
import tocraft.remorphed.handler.ShapeEventsCallback;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.events.ShapeEvents;
import tocraft.walkers.api.platform.ApiLevel;
import tocraft.walkers.api.variant.ShapeType;

import java.util.*;

public class Remorphed {

    public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
    public static final String MODID = "remorphed";
    public static final RemorphedConfig CONFIG = ConfigLoader.read(MODID, RemorphedConfig.class);
    public static boolean displayVariantsInMenu = true;
    public static boolean displayTraitsInMenu = true;
    public static final boolean foundSkinShifter = PlatformData.isModLoaded("skinshifter");

    public void initialize() {
        ShapeEvents.UNLOCK_SHAPE.register(new ShapeEventsCallback());
        if (!CONFIG.unlockFriendlyNormal) {
            ApiLevel.setApiLevel(ApiLevel.MORPHING_AND_VARIANTS_MENU_ONLY);
        }

        // add DarkShadow_2k to devs (for creating the special shape icon and concepts)
        Walkers.devs.add(UUID.fromString("74b6d9b3-c8c1-40db-ab82-ccc290d1aa03"));

        VersionChecker.registerModrinthChecker(MODID, "remorphed", TComponent.literal("Remorphed"));

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

    public static boolean canUseEveryShape(Player player) {
        return player.isCreative() && CONFIG.creativeUnlockAll;
    }

    public static boolean canUseShape(Player player, ShapeType<?> type) {
        return canUseEveryShape(player) || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(type.getEntityType()) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(type.getEntityType()));
    }

    public static List<ShapeType<?>> getUnlockedShapes(Player player) {
        if (canUseEveryShape(player)) {
            return ShapeType.getAllTypes(CEntity.level(player));
        } else if (Walkers.CONFIG.unlockEveryVariant) {
            List<ShapeType<?>> unlocked = new ArrayList<>();
            for (ShapeType<?> shapeType : ShapeType.getAllTypes(CEntity.level(player))) {
                if (!unlocked.contains(shapeType) && canUseShape(player, shapeType)) unlocked.add(shapeType);
            }
            return unlocked;
        } else {
            return new ArrayList<>(PlayerMorph.getUnlockedShapes(player).keySet().stream().filter(type -> canUseShape(player, type)).toList());
        }
    }

    public static List<PlayerProfile> getUnlockedSkins(Player player) {
        return new ArrayList<>(PlayerMorph.getUnlockedSkinIds(player).keySet().stream().filter(skinId -> (PlayerMorph.getPlayerKills(player, skinId) >= CONFIG.killToUnlockPlayers || CONFIG.killToUnlockPlayers == 0) && CONFIG.killToUnlockPlayers != -1).map(PlayerProfile::ofId).filter(Objects::nonNull).toList());
    }

    public static int getKillToUnlock(EntityType<?> entityType) {
        String id = Walkers.getEntityTypeRegistry().getKey(entityType).toString();
        if (Remorphed.CONFIG.killToUnlockByType.containsKey(id)) return Remorphed.CONFIG.killToUnlockByType.get(id);
        else return Remorphed.CONFIG.killToUnlock;
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
                compound.putString("id", Walkers.getEntityTypeRegistry().getKey(shape.getEntityType()).toString());
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
                compound.putUUID("uuid", skinId);
                compound.putInt("killAmount", killAmount);
                skinsList.add(compound);
            }
        });
        if (!skinsList.isEmpty()) compoundTag.put("UnlockedSkins", skinsList);

        compoundTag.putUUID("uuid", changed.getUUID());
        ModernNetworking.sendToPlayer(packetTarget, NetworkHandler.UNLOCKED_SYNC, compoundTag);
    }

    public static ResourceLocation id(String name) {
        return Identifier.parse(MODID, name);
    }
}
