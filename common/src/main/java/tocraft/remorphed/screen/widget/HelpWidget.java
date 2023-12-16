package tocraft.remorphed.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import tocraft.remorphed.screen.RemorphedHelpScreen;

public class HelpWidget extends Button {

    public HelpWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.nullToEmpty("?"), (widget) -> {
            Minecraft.getInstance().setScreen(new RemorphedHelpScreen());
        }, null);
    }
}
