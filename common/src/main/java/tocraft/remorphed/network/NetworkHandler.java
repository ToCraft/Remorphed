package tocraft.remorphed.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import tocraft.craftedcore.network.NetworkManager;
import tocraft.remorphed.Remorphed;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

public class NetworkHandler {
	static ResourceLocation SHAPE_REQUEST = Remorphed.id("request");
	
	public static void registerPacketReceiver() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, NetworkHandler.SHAPE_REQUEST, (buf, context) -> context.getPlayer().getServer().execute(() -> {								
			// check if player is blacklisted
			if (Walkers.CONFIG.playerUUIDBlacklist.contains(context.getPlayer().getUUID())) {
				context.getPlayer().displayClientMessage(Component.translatable("walkers.player_blacklisted"), true);
				return;
			}

			ResourceLocation typeId = buf.readResourceLocation();
			int typeVariant = buf.readInt();
			
			EntityType<?> eType = BuiltInRegistries.ENTITY_TYPE.get(typeId);
			
			// make the default ShapeType null, doing it this way, it's ensured that invalid 2ndShapes won't cause crashes.
			@Nullable
			ShapeType<LivingEntity> type = ShapeType.from(eType, typeVariant);;;

			// update Player
			PlayerShapeChanger.change2ndShape((ServerPlayer) context.getPlayer(), type);

			// Refresh player dimensions
			context.getPlayer().refreshDimensions();
		}));
	}
	
	public static <T extends LivingEntity> void sendSwapRequest(@NotNull ShapeType<T> type) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

		buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()));
		buf.writeInt(type.getVariantData());
		
		NetworkManager.sendToServer(NetworkHandler.SHAPE_REQUEST, buf);
	}
}
