package tocraft.remorphed.screen.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.remorphed.screen.RemorphedScreen;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.variant.ShapeType;
import tocraft.walkers.skills.ShapeSkill;
import tocraft.walkers.skills.SkillRegistry;

import java.util.ArrayList;
import java.util.List;

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
        setTooltip(Tooltip.create(ShapeType.createTooltipText(entity)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
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
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!crashed) {
            if (Remorphed.displaySkillsInMenu) {
                // Render Skill Icons first
                int blitOffset = 0;
                int rowIndex = 0;
                List<ResourceLocation> renderedSkills = new ArrayList<>();
                for (ShapeSkill<T> skill : SkillRegistry.getAll(entity)) {
                    if (!renderedSkills.contains(skill.getId()) && skill.getIcon() != null) {
                        context.blit(getX() + rowIndex, getY() + blitOffset, 0, 18, 18, skill.getIcon());
                        // prevent infinite skills to be rendered
                        if (blitOffset >= getHeight() - 18) {
                            rowIndex += 18;
                            blitOffset = 0;
                        } else {
                            blitOffset += 18;
                        }
                        if (rowIndex >= getWidth() - 18) {
                            break;
                        }
                        renderedSkills.add(skill.getId());
                    }
                }
            }

            // Some entities (namely Aether mobs) crash when rendered in a GUI.
            // Unsure as to the cause, but this try/catch should prevent the game from entirely dipping out.
            try {
                // ARGH
                InventoryScreen.renderEntityInInventory(context, getX() + (float) this.getWidth() / 2, (int) (getY() + this.getHeight() * .75f), size, new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), null, entity);
            } catch (Exception e) {
                Remorphed.LOGGER.error("Error while rendering " + ShapeType.createTooltipText(entity).getString(), e);
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
                context.blit(Remorphed.id("textures/gui/selected.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
            }
            // Render favorite
            if (isFavorite) {
                context.blit(Remorphed.id("textures/gui/favorite.png"), getX(), getY(), getWidth(), getHeight(), 0, 0, 48, 32, 48, 32);
            }
        }
    }

    @Override
    public void onPress() {

    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
