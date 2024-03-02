package tocraft.remorphed.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import tocraft.remorphed.Remorphed;

@Environment(EnvType.CLIENT)
public class RemorphedHelpScreen extends Screen {

    public RemorphedHelpScreen() {
        super(new TextComponent(""));
        super.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
    }

    @SuppressWarnings("resource")
	@Override
    public void render(PoseStack context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        context.pushPose();
        context.scale(0.75f, 0.75f, 0.75f);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.welcome"), 15, 15, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.credits"), 15, 30, 0xffffff);

        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.support_label").withStyle(ChatFormatting.BOLD), 15, 60, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.support_description"), 15, 75, 0xffffff);

        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.ability_label").withStyle(ChatFormatting.BOLD), 15, 100, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.ability_description_1"), 15, 115, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.ability_description_2"), 15, 130, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.ability_description_3"), 15, 145, 0xffffff);

        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.config_label").withStyle(ChatFormatting.BOLD), 15, 175, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.config_description"), 15, 190, 0xffffff);

        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.credits_label").withStyle(ChatFormatting.BOLD), 15, 220, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.credits_general"), 15, 235, 0xffffff);
        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.credits_translators"), 15, 250, 0xffffff);

        Minecraft.getInstance().font.drawShadow(context, new TranslatableComponent(Remorphed.MODID + ".help.return").withStyle(ChatFormatting.ITALIC), 15, height + 60, 0xffffff);

        context.popPose();

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        onClose();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
