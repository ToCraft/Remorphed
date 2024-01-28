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
import net.minecraft.world.entity.LivingEntity;
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
import java.util.Map;

public class Remorphed {

    public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
    public static final String MODID = "remorphed";
    public static final RemorphedConfig CONFIG = ConfigLoader.read(MODID, RemorphedConfig.class);
    private static final String MAVEN_URL = "https://maven.tocraft.dev/public/dev/tocraft/remorphed/maven-metadata.xml";
    public static boolean displayVariantsInMenu = true;

    public void initialize() {
        try {
            VersionChecker.registerMavenChecker(MODID, new URL(MAVEN_URL), Component.literal("Remorphed"));
        } catch (MalformedURLException ignored) {
        }

        if (Platform.getEnvironment() == Env.CLIENT)
            new RemorphedClient().initialize();

        NetworkHandler.registerPacketReceiver();

        ShapeEvents.UNLOCK_SHAPE.register(new UnlockShapeCallback());
        ShapeEvents.SWAP_SHAPE.register(new ShapeSwapCallback());
        CommandRegistrationEvent.EVENT.register(new RemorphedCommand());

        PlayerEvent.PLAYER_JOIN.register(player -> {
            // allow unlocking friendly mobs via the "normal" method
            Walkers.CONFIG.unlockOveridesCurrentShape = Remorphed.CONFIG.unlockFriendlyNormal;
            // Sync favorites
            NetworkHandler.sendFavoriteSync(player);
        });
    }

    public static boolean canUseShape(Player player, ShapeType<?> type) {
        return player.isCreative() || !Remorphed.CONFIG.lockTransform && (type == null || Remorphed.CONFIG.killToUnlock <= 0 || ((RemorphedPlayerDataProvider) player).remorphed$getKills(type) >= Remorphed.CONFIG.killToUnlock);
    }

    public static boolean canUseAnyShape(Player player) {
        boolean canUseShapes = player.isCreative() || Remorphed.CONFIG.killToUnlock <= 0;

        for (ShapeType<? extends LivingEntity> shape : ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().keySet()) {
            canUseShapes = canUseShapes ? canUseShapes : canUseShape(player, shape);
        }

        return canUseShapes;
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
            if (killAmount > 0) {
                CompoundTag compound = new CompoundTag();
                compound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
                compound.putInt("variant", shape.getVariantData());
                compound.putInt("killAmount", killAmount);
                list.add(compound);
            }
        });

        if (!unlockedShapes.isEmpty())
            compoundTag.put("UnlockedShapes", list);

        packet.writeUUID(changed.getUUID());
        packet.writeNbt(compoundTag);
        NetworkManager.sendToPlayer(packetTarget, NetworkHandler.UNLOCKED_SYNC, packet);
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }
}
