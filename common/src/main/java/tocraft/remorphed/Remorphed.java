package tocraft.remorphed;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.events.common.CommandEvents;
import tocraft.craftedcore.network.NetworkManager;
import tocraft.craftedcore.platform.Platform;
import tocraft.craftedcore.platform.VersionChecker;
import tocraft.remorphed.command.RemorphedCommand;
import tocraft.remorphed.config.RemorphedConfig;
import tocraft.remorphed.events.ShapeSwapCallback;
import tocraft.remorphed.events.UnlockShapeCallback;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Remorphed {

    public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
    public static final String MODID = "remorphed";
    public static final RemorphedConfig CONFIG = ConfigLoader.read(MODID, RemorphedConfig.class);
    public static String VERSION_URL = "https://raw.githubusercontent.com/ToCraft/Remorphed/1.20.2/gradle.properties";
    public static List<String> devs = new ArrayList<>();
    public static boolean displayVariantsInMenu = true;

    static {
        devs.add("1f63e38e-4059-4a4f-b7c4-0fac4a48e744");
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static boolean canUseShape(Player player, ShapeType<?> type) {
        return player.isCreative() || !Remorphed.CONFIG.lockTransform && (type == null || ((RemorphedPlayerDataProvider) player).getKills(type) >= Remorphed.CONFIG.killToUnlock);
    }

    public static boolean canUseAnyShape(Player player) {
        boolean canUseShapes = player.isCreative();

        for (ShapeType<? extends LivingEntity> shape : ((RemorphedPlayerDataProvider) player).getUnlockedShapes().keySet()) {
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
        Map<ShapeType<?>, Integer> unlockedShapes = ((RemorphedPlayerDataProvider) changed).getUnlockedShapes();

        ListTag list = new ListTag();

        unlockedShapes.forEach((shape, killAmount) -> {
            CompoundTag compound = new CompoundTag();
            compound.putString("id", Registry.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
            compound.putInt("variant", shape.getVariantData());
            compound.putInt("killAmount", killAmount);
            list.add(compound);
        });

        if (list != null)
            compoundTag.put("UnlockedShapes", list);

        packet.writeUUID(changed.getUUID());
        packet.writeNbt(compoundTag);
        NetworkManager.sendToPlayer(packetTarget, NetworkHandler.UNLOCKED_SYNC, packet);
    }

    public void initialize() {
        VersionChecker.registerChecker(MODID, VERSION_URL, new TextComponent("Remorphed"));

        if (Platform.getDist().isClient())
            new RemorphedClient().initialize();

        NetworkHandler.registerPacketReceiver();

        ShapeEvents.UNLOCK_SHAPE.register(new UnlockShapeCallback());
        ShapeEvents.SWAP_SHAPE.register(new ShapeSwapCallback());
        CommandEvents.REGISTRATION.register(new RemorphedCommand());
    }
}
