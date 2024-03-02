package tocraft.remorphed.screen.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.variant.ShapeType;

@Environment(EnvType.CLIENT)
public class EntityWidget<T extends LivingEntity> extends AbstractButton {

    private final ShapeType<T> type;
    private final T entity;
    private final int size;
    private final RemorphedScreen parent;
    private boolean crashed;
    private boolean isFavorite;
    private final boolean isCurrent;

    public EntityWidget(float x, float y, float width, float height, ShapeType<T> type, T entity, RemorphedScreen parent, boolean isFavorite, boolean current) {
        super((int) x, (int) y, (int) width, (int) height, Component.nullToEmpty("")); // int x, int y, int width, int height, message
        this.type = type;
        this.entity = entity;
        size = (int) (25 * (1 / (Math.max(entity.getBbHeight(), entity.getBbWidth()))));
        entity.setGlowingTag(true);
        this.parent = parent;
        this.isFavorite = isFavorite;
        this.isCurrent = current;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = mouseX >= (double) this.x && mouseX < (double) (this.x + this.width) && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height);
        if (bl && Minecraft.getInstance().player != null) {
            // switch to new shape
            if (button == 0 && !type.equals(ShapeType.from(PlayerShape.getCurrentShape(Minecraft.getInstance().player)))) {
                // Update 2nd Shape
                NetworkHandler.sendSwap2ndShapeRequest(type);
                // close active screen handler
                parent.onClose();
            }
            // Add to favorites
            else if (button == 1) {
                isFavorite = !isFavorite;
                NetworkHandler.sendFavoriteRequest(type, isFavorite);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderButton(PoseStack context, int mouseX, int mouseY, float delta) {
        if (!crashed) {
            // Some entities (namely Aether mobs) crash when rendered in a GUI.
            // Unsure as to the cause, but this try/catch should prevent the game from entirely dipping out.
            try {
                // ARGH
                InventoryScreen.renderEntityInInventory(this.x + this.getWidth() / 2, (int) (this.y + this.getHeight() * .75f), size, -10, -10, entity);
            } catch (Exception ignored) {
                crashed = true;
                MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
                immediate.endBatch();
                EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                entityRenderDispatcher.setRenderShadow(true);
                RenderSystem.getModelViewStack().popPose();
                Lighting.setupFor3DItems();
            }

            // Render selected outline
            if (isCurrent) {
                RenderSystem.setShaderTexture(0, Remorphed.id("textures/gui/selected.png"));
                GuiComponent.blit(context, x, y, getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
            }

            // Render selected outline
            if (isCurrent) {
                RenderSystem.setShaderTexture(0, Remorphed.id("textures/gui/selected.png"));
                GuiComponent.blit(context, x, y, getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
            }
            // Render favorite
            if (isFavorite) {
                RenderSystem.setShaderTexture(0, Remorphed.id("textures/gui/favorite.png"));
                GuiComponent.blit(context, x, y, getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
            }
        }

        if (isHoveredOrFocused())
            renderToolTip(context, mouseX, mouseY);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        Screen currentScreen = Minecraft.getInstance().screen;

        if (currentScreen != null) {
            currentScreen.renderTooltip(poseStack, type.createTooltipText(entity), mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {

    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }
}
