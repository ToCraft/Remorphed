package tocraft.remorphed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.events.common.PlayerEvents;
import tocraft.craftedcore.network.NetworkManager;
import tocraft.craftedcore.platform.Platform;
import tocraft.craftedcore.platform.VersionChecker;
import tocraft.remorphed.config.RemorphedConfig;
import tocraft.remorphed.events.UnlockShapeCallback;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class Remorphed {

	public static final Logger LOGGER = LoggerFactory.getLogger(Remorphed.class);
	public static final String MODID = "remorphed";
	public static String versionURL = "https://raw.githubusercontent.com/ToCraft/Remorphed/1.20.2/gradle.properties";
	public static final RemorphedConfig CONFIG = ConfigLoader.read(MODID, RemorphedConfig.class);
	public static List<String> devs = new ArrayList<>();
	static {
		devs.add("1f63e38e-4059-4a4f-b7c4-0fac4a48e744");
	}
	
	public void initialize() {
		PlayerEvents.PLAYER_JOIN.register(player -> {
			String newestVersion = VersionChecker.checkForNewVersion(versionURL);
			if (newestVersion != null && !Platform.getMod(MODID).getVersion().equals(newestVersion))
				player.sendSystemMessage(Component.translatable("ycdm.update", newestVersion));
		});			
		
		if (Platform.getDist().isClient())
			new RemorphedClient().initialize();
		
		NetworkHandler.registerPacketReceiver();
		
		ShapeEvents.UNLOCK_SHAPE.register(new UnlockShapeCallback());
	}

	public static ResourceLocation id(String name) {
		return new ResourceLocation(MODID, name);
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
			compound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
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
}
