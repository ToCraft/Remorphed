package dev.tocraft.remorphed.screen;

import dev.tocraft.craftedcore.gui.LongTextWidget;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.walkers.WalkersClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RemorphedHelpScreen extends Screen {
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    protected LongTextWidget list;

    public RemorphedHelpScreen() {
        super(Component.literal("REMORPHED"));
    }

    protected void init() {
        this.addTitle();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addTitle() {
        this.layout.addTitleHeader(this.title, this.font);
    }

    protected void addContents() {
        this.list = this.layout.addToContents(new LongTextWidget(0, this.layout.getHeaderHeight(), this.width, this.layout.getContentHeight(), true, Remorphed.CONFIG.row_width));

        // Add Text
        this.list.addText(Component.translatable("remorphed.help.welcome"));
        this.list.addText(Component.translatable("remorphed.help.credits").append("\n"));

        this.list.addText(Component.translatable("remorphed.help.deleting_shapes_label").withStyle(ChatFormatting.BOLD));
        this.list.addText(Component.translatable("remorphed.help.deleting_shapes_description").append("\n"));

        this.list.addText(Component.translatable("remorphed.help.variants_label").withStyle(ChatFormatting.BOLD));
        Component keyStr = WalkersClient.VARIANTS_MENU_KEY.getTranslatedKeyMessage();
        this.list.addText(Component.translatable("remorphed.help.variants_description", keyStr, keyStr).append("\n"));

        this.list.addText(Component.translatable("remorphed.help.ability_label").withStyle(ChatFormatting.BOLD));
        this.list.addText(Component.translatable("remorphed.help.ability_description_1"));
        this.list.addText(Component.translatable("remorphed.help.ability_description_2"));
        this.list.addText(Component.translatable("remorphed.help.ability_description_3").append("\n"));

        this.list.addText(Component.translatable("remorphed.help.config_label").withStyle(ChatFormatting.BOLD));
        this.list.addText(Component.translatable("remorphed.help.config_description").append("\n"));

        this.list.addText(Component.translatable("remorphed.help.support_label").withStyle(ChatFormatting.BOLD));
        this.list.addText(Component.translatable("remorphed.help.support_description").append("\n"));

        this.list.addText(Component.translatable("remorphed.help.credits_label").withStyle(ChatFormatting.BOLD));
        this.list.addText(Component.translatable("remorphed.help.credits_general"));
        this.list.addText(Component.translatable("remorphed.help.credits_translators").append("\n"));

        this.list.addText(Component.translatable("remorphed.help.return").withStyle(ChatFormatting.ITALIC));
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> Minecraft.getInstance().setScreen(new RemorphedMenu())).width(200).build());
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractTransparentBackground(graphics);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        int keyCode = event.key();
        if (keyCode < 257 && keyCode != 32) {
            onClose();
            return true;
        } else {
            return super.keyPressed(event);
        }
    }

    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSize(this.width, layout);
        }
    }
}
