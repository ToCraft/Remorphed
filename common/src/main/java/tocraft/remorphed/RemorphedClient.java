package tocraft.remorphed;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import tocraft.craftedcore.events.client.ClientTickEvents;
import tocraft.craftedcore.registration.client.KeyMappingRegistry;
import tocraft.remorphed.network.ClientNetworking;
import tocraft.remorphed.tick.KeyPressHandler;

public class RemorphedClient {
    public static final KeyMapping MENU_KEY = new KeyMapping("key.remorphed_menu", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.remorphed");

    public void initialize() {
        KeyMappingRegistry.register(MENU_KEY);

        // Register event handlers
        ClientTickEvents.CLIENT_PRE.register(new KeyPressHandler());

        ClientNetworking.registerPacketHandlers();
    }
}
