package tocraft.remorphed;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.platform.VersionChecker;
import tocraft.remorphed.command.RemorphedCommand;
import tocraft.remorphed.config.RemorphedConfig;
import tocraft.remorphed.events.ShapeSwapCallback;
import tocraft.remorphed.events.UnlockShapeCallback;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Remorphed {

    public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
    public static final String MODID = "remorphed";
    public static final RemorphedConfig CONFIG = ConfigLoader.read(MODID, RemorphedConfig.class);
    private static final String MAVEN_URL = "https://maven.tocraft.dev/public/dev/tocraft/remorphed/maven-metadata.xml";
    public static boolean displayVariantsInMenu = true;

    public void initialize() {
        // add DarkShadow_2k to devs (for creating the special shape icon and concepts)
        Walkers.devs.add(UUID.fromString("74b6d9b3-c8c1-40db-ab82-ccc290d1aa03"));

        try {
            VersionChecker.registerMavenChecker(MODID, new URL(MAVEN_URL), Component.literal("Remorphed"));
        } catch (MalformedURLException ignored) {
        }

        if (Platform.getEnvironment() == Env.CLIENT) new RemorphedClient().initialize();

        NetworkHandler.registerPacketReceiver();

        ShapeEvents.UNLOCK_SHAPE.register(new UnlockShapeCallback());
        ShapeEvents.SWAP_SHAPE.register(new ShapeSwapCallback());
        CommandRegistrationEvent.EVENT.register(new RemorphedCommand());

        // allow unlocking friendly mobs via the "normal" method
        Walkers.CONFIG.unlockOverridesCurrentShape = Remorphed.CONFIG.unlockFriendlyNormal;
        Walkers.CONFIG.save();

        // Sync favorites
        PlayerEvent.PLAYER_JOIN.register(NetworkHandler::sendFavoriteSync);
    }

    public static boolean canUseEveryShape(Player player) {
        return player.isCreative() && CONFIG.creativeUnlockAll;
    }

    public static boolean canUseShape(Player player, ShapeType<?> type) {
        return canUseEveryShape(player) || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.getKillToUnlock(type.getEntityType()) <= 0 || ((RemorphedPlayerDataProvider) player).remorphed$getKills(type) >= Remorphed.getKillToUnlock(type.getEntityType()));
    }

    public static List<ShapeType<?>> getUnlockedShapes(Player player) {
        if (canUseEveryShape(player)) {
            return ShapeType.getAllTypes(player.level());
        } else {
            return new ArrayList<>(((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().keySet().stream().filter(type -> canUseShape(player, type)).toList());
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
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        CompoundTag compoundTag = new CompoundTag();

        // serialize current shape data to tag if it exists
        Map<ShapeType<?>, Integer> unlockedShapes = ((RemorphedPlayerDataProvider) changed).remorphed$getUnlockedShapes();

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

        packet.writeUUID(changed.getUUID());
        packet.writeNbt(compoundTag);
        NetworkManager.sendToPlayer(packetTarget, NetworkHandler.UNLOCKED_SYNC, packet);
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }
}
