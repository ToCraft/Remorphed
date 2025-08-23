package dev.tocraft.remorphed.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class SearchWidget extends EditBox {
    public SearchWidget(float x, float y, float width, float height) {
        super(Minecraft.getInstance().font, (int) x, (int) y, (int) width, (int) height, Component.nullToEmpty("Search Bar"));
    }
}
