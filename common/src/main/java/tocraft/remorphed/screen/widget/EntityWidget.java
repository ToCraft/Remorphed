package tocraft.remorphed.screen.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.walkers.api.variant.ShapeType;
import tocraft.walkers.traits.ShapeTrait;
import tocraft.walkers.traits.TraitRegistry;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityWidget<T extends LivingEntity> extends ShapeWidget {

    private final ShapeType<T> type;
    private final T entity;
    private final int size;

    public EntityWidget(int x, int y, int width, int height, ShapeType<T> type, @NotNull T entity, Screen parent, boolean isFavorite, boolean current, int availability) {
        super(x, y, width, height, parent, isFavorite, current, availability); // int x, int y, int width, int height, message
        this.size = (int) (Remorphed.CONFIG.entity_size * (1 / (Math.max(entity.getBbHeight(), entity.getBbWidth()))));
        this.type = type;
        this.entity = entity;
        entity.setGlowingTag(true);
        setTooltip(Tooltip.create(ShapeType.createTooltipText(entity)));
    }

    @Override
    protected void sendFavoriteRequest(boolean isFavorite) {
        NetworkHandler.sendFavoriteRequest(type, isFavorite);
    }

    @Override
    protected void sendSwap2ndShapeRequest() {
        NetworkHandler.sendSwap2ndShapeRequest(type);
    }

    @Override
    protected void renderShape(GuiGraphics guiGraphics) {
        if (Remorphed.displayDataInMenu) {
            int iconS = width / 5;


            // Render Trait Icons first
            int blitOffset = 0;
            int rowIndex = 0;
            List<ResourceLocation> renderedTraits = new ArrayList<>();
            List<ShapeTrait<T>> traits = TraitRegistry.getAll(entity);
            for (ShapeTrait<T> trait : traits) {
                if (trait != null && trait.getIcon() != null && (!renderedTraits.contains(trait.getId()) || trait.iconMightDiffer())) {
                    guiGraphics.blitSprite(RenderType::guiTextured, trait.getIcon(), getX() + rowIndex, getY() + blitOffset, iconS, iconS);
                    // prevent infinite amounts of traits to be rendered
                    if (blitOffset >= getHeight() - iconS) {
                        rowIndex += iconS;
                        blitOffset = 0;
                    } else {
                        blitOffset += iconS;
                    }
                    if (rowIndex >= getWidth() - iconS) {
                        break;
                    }
                    renderedTraits.add(trait.getId());
                }
            }
        }

        // Some entities (namely Aether mobs) crash when rendered in a GUI.
        // Unsure as to the cause, but this try/catch should prevent the game from entirely dipping out.
        try {
            // ARGH
            InventoryScreen.renderEntityInInventory(guiGraphics, getX() + (float) this.getWidth() / 2, (int) (getY() + this.getHeight() * .75f), size, new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), null, entity);
        } catch (Exception e) {
            Remorphed.LOGGER.error("Error while rendering {}", ShapeType.createTooltipText(entity).getString(), e);
            setCrashed();
            MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
            immediate.endBatch();
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            entityRenderDispatcher.setRenderShadow(true);
            RenderSystem.getModelViewStack().popMatrix();
            Lighting.setupFor3DItems();
        }
    }

}
