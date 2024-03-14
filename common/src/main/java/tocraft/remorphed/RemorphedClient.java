package tocraft.remorphed;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import tocraft.remorphed.network.ClientNetworking;
import tocraft.remorphed.tick.KeyPressHandler;

@Environment(EnvType.CLIENT)
public class RemorphedClient {
    public static final KeyMapping MENU_KEY = new KeyMapping("key.remorphed_menu", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.remorphed");

    public void initialize() {
        KeyMappingRegistry.register(MENU_KEY);

        // Register event handlers
        ClientTickEvent.CLIENT_PRE.register(new KeyPressHandler());
        ClientNetworking.registerPacketHandlers();
    }
}
