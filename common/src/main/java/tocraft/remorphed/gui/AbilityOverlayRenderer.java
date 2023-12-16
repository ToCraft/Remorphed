package tocraft.remorphed.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import tocraft.craftedcore.events.client.ClientGuiEvents;
import tocraft.craftedcore.gui.TimerOverlayRenderer;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.PAPlayerDataProvider;

public class AbilityOverlayRenderer {

    public static void register() {
        ClientGuiEvents.RENDER_HUD.register((matrices, delta) -> {
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            
            if(((PAPlayerDataProvider) player).getPotion().isBlank()) {
                return;
            }

             TimerOverlayRenderer.register(matrices, ((PAPlayerDataProvider) player).getCooldown(), Remorphed.CONFIG.cooldownTicks, Items.SPLASH_POTION);
        });
    }
}
