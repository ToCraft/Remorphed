package tocraft.remorphed.network;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tocraft.craftedcore.client.CraftedCoreClient;
import tocraft.craftedcore.network.client.ClientNetworking.ApplicablePacket;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.api.variant.ShapeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientNetworking {
    public static void registerPacketHandlers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, NetworkHandler.UNLOCKED_SYNC,
                ClientNetworking::handleUnlockedSyncPacket);
    }

    @SuppressWarnings("unchecked")
    public static void handleUnlockedSyncPacket(FriendlyByteBuf packet, NetworkManager.PacketContext context) {
        final UUID uuid = packet.readUUID();
        final CompoundTag compound = packet.readNbt();
        final Map<ShapeType<?>, Integer> unlockedShapes = new HashMap<>();
        if (compound != null && compound.contains("UnlockedShapes") && compound.get("UnlockedShapes") instanceof ListTag list) {
            list.forEach(entryTag -> {
                EntityType<? extends LivingEntity> eType = (EntityType<? extends LivingEntity>) Registry.ENTITY_TYPE.get(new ResourceLocation(((CompoundTag) entryTag).getString("id")));
                int variant = ((CompoundTag) entryTag).getInt("variant");
                int killAmount = ((CompoundTag) entryTag).getInt("killAmount");
                unlockedShapes.put(ShapeType.from(eType, variant), killAmount);
            });
        }

        runOrQueue(context, player -> {
            @Nullable
            Player syncTarget = player.getCommandSenderWorld().getPlayerByUUID(uuid);

            if (syncTarget != null)
                ((RemorphedPlayerDataProvider) syncTarget).remorphed$setUnlockedShapes(unlockedShapes);
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
