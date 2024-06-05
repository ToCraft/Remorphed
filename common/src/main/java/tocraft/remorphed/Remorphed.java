package tocraft.remorphed;

import net.fabricmc.api.EnvType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
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
import tocraft.craftedcore.platform.PlatformData;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Remorphed {

    public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
    public static final String MODID = "remorphed";
    public static final RemorphedConfig CONFIG = ConfigLoader.read(MODID, RemorphedConfig.class);
    public static boolean displayVariantsInMenu = true;
    public static boolean displayTraitsInMenu = true;

    public void initialize() {
        ShapeEvents.UNLOCK_SHAPE.register(new ShapeEventsCallback());
        if (!CONFIG.unlockFriendlyNormal) {
            ApiLevel.setApiLevel(ApiLevel.MORPHING_AND_VARIANTS_MENU_ONLY);
        }

        // add DarkShadow_2k to devs (for creating the special shape icon and concepts)
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

    public static boolean canUseEveryShape(Player player) {
        return player.isCreative() && CONFIG.creativeUnlockAll;
    }

    public static boolean canUseShape(Player player, ShapeType<?> type) {
        return canUseEveryShape(player) || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(type.getEntityType()) <= 0 || PlayerMorph.getKills(player, type) >= Remorphed.getKillToUnlock(type.getEntityType()));
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

    public static int getKillToUnlock(EntityType<?> entityType) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
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

        ListTag list = new ListTag();

        unlockedShapes.forEach((shape, killAmount) -> {
            if (killAmount > 0 && shape != null) {
                CompoundTag compound = new CompoundTag();
                compound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
                compound.putInt("variant", shape.getVariantData());
                compound.putInt("killAmount", killAmount);
                list.add(compound);
            }
        });

        if (!unlockedShapes.isEmpty()) compoundTag.put("UnlockedShapes", list);

        compoundTag.putUUID("uuid", changed.getUUID());
        ModernNetworking.sendToPlayer(packetTarget, NetworkHandler.UNLOCKED_SYNC, compoundTag);
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }
}
