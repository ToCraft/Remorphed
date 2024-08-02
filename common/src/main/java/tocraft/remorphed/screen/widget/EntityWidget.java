package tocraft.remorphed.screen.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
//#if MC>=1201
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
//#if MC>1182
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.gui.components.Tooltip;
//#else
//$$ import net.minecraft.client.gui.screens.Screen;
//#endif
import tocraft.craftedcore.patched.client.CGraphics;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.network.NetworkHandler;
import tocraft.remorphed.screen.RemorphedScreen;
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

    public EntityWidget(float x, float y, float width, float height, ShapeType<T> type, T entity, RemorphedScreen parent, boolean isFavorite, boolean current) {
        super((int) x, (int) y, (int) width, (int) height, parent, isFavorite, current); // int x, int y, int width, int height, message
        this.size = (int) (25 * (1 / (Math.max(entity.getBbHeight(), entity.getBbWidth()))));
        this.type = type;
        this.entity = entity;
        entity.setGlowingTag(true);
        //#if MC>1182
        setTooltip(Tooltip.create(ShapeType.createTooltipText(entity)));
        //#endif
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
    //#if MC>1194
    protected void renderShape(GuiGraphics guiGraphics) {
    //#else
    //$$ protected void renderShape(PoseStack guiGraphics) {
    //#endif
        if (Remorphed.displayTraitsInMenu) {
            // Render Trait Icons first
            int blitOffset = 0;
            int rowIndex = 0;
            List<ResourceLocation> renderedTraits = new ArrayList<>();
            for (ShapeTrait<T> trait : TraitRegistry.getAll(entity)) {
                if (trait != null && trait.getIcon() != null && (!renderedTraits.contains(trait.getId()) || trait.iconMightDiffer())) {
                    CGraphics.blit(guiGraphics, getX() + rowIndex, getY() + blitOffset, 0, 18, 18, trait.getIcon());
                    // prevent infinite traits to be rendered
                    if (blitOffset >= getHeight() - 18) {
                        rowIndex += 18;
                        blitOffset = 0;
                    } else {
                        blitOffset += 18;
                    }
                    if (rowIndex >= getWidth() - 18) {
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
            //#if MC>1201
            InventoryScreen.renderEntityInInventory(guiGraphics, getX() + (float) this.getWidth() / 2, (int) (getY() + this.getHeight() * .75f), size, new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), null, entity);
            //#elseif MC>1182
            //$$ InventoryScreen.renderEntityInInventory(guiGraphics, getX() + this.getWidth() / 2, (int) (getY() + this.getHeight() * .75f), size, new Quaternionf().rotationXYZ((float) Math.PI, 0, 0), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI), entity);
            //#else
            //$$ InventoryScreen.renderEntityInInventory(x + this.getWidth() / 2, (int) (y + this.getHeight() * .75f), size, -10, -10, entity);
            //#endif
        } catch (Exception e) {
            Remorphed.LOGGER.error("Error while rendering {}", ShapeType.createTooltipText(entity).getString(), e);
            setCrashed();
            MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
            immediate.endBatch();
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            entityRenderDispatcher.setRenderShadow(true);
            //#if MC>=1205
            RenderSystem.getModelViewStack().popMatrix();
            //#else
            //$$ RenderSystem.getModelViewStack().popPose();
            //#endif
            Lighting.setupFor3DItems();
        }
    }

    //#if MC<=1184
    //$$ @Override
    //$$ public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
    //$$     Screen currentScreen = Minecraft.getInstance().screen;
    //$$
    //$$     if (currentScreen != null) {
    //$$         currentScreen.renderTooltip(poseStack, ShapeType.createTooltipText(entity), mouseX, mouseY);
    //$$     }
    //$$ }
    //#endif
}
