package tocraft.remorphed.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
//#if MC>=1201
import net.minecraft.client.gui.GuiGraphics;
//#endif
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import tocraft.craftedcore.patched.TComponent;
import tocraft.craftedcore.patched.client.CGraphics;
import tocraft.remorphed.Remorphed;

@Environment(EnvType.CLIENT)
public class RemorphedHelpScreen extends Screen {

    public RemorphedHelpScreen() {
        super(TComponent.literal(""));
        super.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
    }

    @Override
    //#if MC>1194
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            PoseStack matrices = context.pose();
    //#else
    //$$ public void render(PoseStack context, int mouseX, int mouseY, float delta) {
    //$$     PoseStack matrices = context;
    //#endif
        //#if MC>1201
        renderTransparentBackground(context);
        //#else
        //$$ renderBackground(context);
        //#endif

        matrices.pushPose();
        matrices.scale(0.75f, 0.75f, 0.75f);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.welcome"), 15, 15, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.credits"), 15, 30, 0xffffff, true);

        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.support_label").withStyle(ChatFormatting.BOLD), 15, 60, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.support_description"), 15, 75, 0xffffff, true);

        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.ability_label").withStyle(ChatFormatting.BOLD), 15, 100, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.ability_description_1"), 15, 115, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.ability_description_2"), 15, 130, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.ability_description_3"), 15, 145, 0xffffff, true);

        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.config_label").withStyle(ChatFormatting.BOLD), 15, 175, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.config_description"), 15, 190, 0xffffff, true);

        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.credits_label").withStyle(ChatFormatting.BOLD), 15, 220, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.credits_general"), 15, 235, 0xffffff, true);
        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.credits_translators"), 15, 250, 0xffffff, true);

        CGraphics.drawString(context, TComponent.translatable(Remorphed.MODID + ".help.return").withStyle(ChatFormatting.ITALIC), 15, height + 60, 0xffffff, true);

        matrices.popPose();

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
