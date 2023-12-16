package tocraft.remorphed.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.client.CraftedCoreClient;
import tocraft.craftedcore.network.NetworkManager;
import tocraft.craftedcore.network.client.ClientNetworking.ApplicablePacket;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.variant.ShapeType;

public class ClientNetworking {
	public static void registerPacketHandlers() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, NetworkHandler.UNLOCKED_SYNC,
				ClientNetworking::handleUnlockedSyncPacket);
	}
		
	public static void handleUnlockedSyncPacket(FriendlyByteBuf packet, NetworkManager.PacketContext context) {
		final UUID uuid = packet.readUUID();
		final CompoundTag compound = packet.readNbt();
		final Map<ShapeType<?>, Integer> unlockedShapes = new HashMap<ShapeType<?>, Integer>();
		if (compound.get("UnlockedShapes") instanceof ListTag list) {
			list.forEach(entryTag -> {
				EntityType<?> eType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(((CompoundTag) entryTag).getString("id")));
				int variant = ((CompoundTag) entryTag).getInt("variant");
				int killAmount = ((CompoundTag) entryTag).getInt("killAmount");
				unlockedShapes.put(ShapeType.from(eType, variant), killAmount);
			});
		}
		
		runOrQueue(context, player -> {
			@Nullable
			Player syncTarget = player.getCommandSenderWorld().getPlayerByUUID(uuid);
			
			if (syncTarget != null)
				((RemorphedPlayerDataProvider) syncTarget).setUnlockedShapes(unlockedShapes);
		});
	}

	public static void runOrQueue(NetworkManager.PacketContext context, ApplicablePacket packet) {
		if (context.getPlayer() == null) {
			CraftedCoreClient.getSyncPacketQueue().add(packet);
		} else {
			context.queue(() -> packet.apply(context.getPlayer()));
		}
	}
}
