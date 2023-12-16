package tocraft.remorphed.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import tocraft.craftedcore.network.NetworkManager;
import tocraft.remorphed.Remorphed;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

public class NetworkHandler {
	public static ResourceLocation SHAPE_REQUEST = Remorphed.id("unlock_request");
	public static ResourceLocation UNLOCKED_SYNC = Remorphed.id("unlocked_sync");
	
	public static void registerPacketReceiver() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, NetworkHandler.SHAPE_REQUEST, (buf, context) -> {
			CompoundTag compound = buf.readNbt(); 
			
			context.getPlayer().getServer().execute(() -> {			
				// check if player is blacklisted
				if (Walkers.CONFIG.playerUUIDBlacklist.contains(context.getPlayer().getUUID())) {
					context.getPlayer().displayClientMessage(Component.translatable("walkers.player_blacklisted"), true);
					return;
				}

				ResourceLocation typeId = new ResourceLocation(compound.getString("id"));
				int typeVariant = compound.getInt("variant");
				
				EntityType<?> eType = BuiltInRegistries.ENTITY_TYPE.get(typeId);
				
				// make the default ShapeType null, doing it this way, it's ensured that invalid 2ndShapes won't cause crashes.
				@Nullable
				ShapeType<LivingEntity> type = ShapeType.from(eType, typeVariant);;;

				// update Player
				PlayerShapeChanger.change2ndShape((ServerPlayer) context.getPlayer(), type);
				PlayerShape.updateShapes((ServerPlayer) context.getPlayer(), type, type.create(context.getPlayer().level()));

				// Refresh player dimensions
				context.getPlayer().refreshDimensions();
			});			
		});
	}
	
	public static <T extends LivingEntity> void sendUnlockRequest(@NotNull ShapeType<T> type) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

		CompoundTag compound = new CompoundTag();
		compound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type.getEntityType()).toString());
		compound.putInt("variant", type.getVariantData());
		
		buf.writeNbt(compound);
		
		NetworkManager.sendToServer(NetworkHandler.SHAPE_REQUEST, buf);
	}
}
