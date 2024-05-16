package tocraft.remorphed;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import tocraft.craftedcore.event.client.ClientPlayerEvents;
import tocraft.craftedcore.event.client.ClientTickEvents;
import tocraft.craftedcore.registration.KeyBindingRegistry;
import tocraft.remorphed.handler.client.ClientPlayerRespawnHandler;
import tocraft.remorphed.network.ClientNetworking;
import tocraft.remorphed.tick.KeyPressHandler;

@Environment(EnvType.CLIENT)
public class RemorphedClient {
    public static final KeyMapping MENU_KEY = new KeyMapping("key.remorphed_menu", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.remorphed");

    public void initialize() {
        KeyBindingRegistry.register(MENU_KEY);

        // Register event handlers
        ClientTickEvents.CLIENT_PRE.register(new KeyPressHandler());
        ClientNetworking.registerPacketHandlers();

        ClientPlayerEvents.CLIENT_PLAYER_RESPAWN.register(new ClientPlayerRespawnHandler());
    }
}
