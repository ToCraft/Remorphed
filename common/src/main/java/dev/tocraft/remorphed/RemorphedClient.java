package dev.tocraft.remorphed;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tocraft.craftedcore.event.client.ClientPlayerEvents;
import dev.tocraft.craftedcore.event.client.ClientTickEvents;
import dev.tocraft.craftedcore.registration.KeyBindingRegistry;
import dev.tocraft.remorphed.handler.client.ClientDisconnectHandler;
import dev.tocraft.remorphed.handler.client.ClientPlayerRespawnHandler;
import dev.tocraft.remorphed.handler.client.EntityRenderCacheHandler;
import dev.tocraft.remorphed.network.ClientNetworking;
import dev.tocraft.remorphed.tick.KeyPressHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RemorphedClient {
    public static final KeyMapping.Category REMORPHED_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("remorphed", "category"));
    public static final KeyMapping MENU_KEY = new KeyMapping("key.remorphed_menu", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, REMORPHED_CATEGORY);

    public void initialize() {
        KeyBindingRegistry.register(MENU_KEY);

        // Register event handlers
        ClientTickEvents.CLIENT_PRE.register(new KeyPressHandler());
        ClientTickEvents.CLIENT_PRE.register(new ClientDisconnectHandler());
        ClientTickEvents.CLIENT_PRE.register(new EntityRenderCacheHandler());
        ClientNetworking.registerPacketHandlers();

        ClientPlayerEvents.CLIENT_PLAYER_RESPAWN.register(new ClientPlayerRespawnHandler());
    }
}
