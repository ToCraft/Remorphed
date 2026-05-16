package dev.tocraft.remorphed.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.network.NetworkHandler;
import dev.tocraft.walkers.api.variant.ShapeType;
import dev.tocraft.walkers.screen.hud.VariantMenu;
import dev.tocraft.walkers.traits.ShapeTrait;
import dev.tocraft.walkers.traits.TraitRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class EntityWidget<T extends LivingEntity> extends ShapeWidget {

    private final ShapeType<T> type;
    private final T entity;
    private final int size;
    private final List<String> exceptions = new ArrayList<>();

    public EntityWidget(int x, int y, int width, int height, ShapeType<T> type, @NotNull T entity, Screen parent, boolean isFavorite, boolean current, int availability) {
        super(x, y, width, height, parent, isFavorite, current, availability);
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
    protected void extractShape(GuiGraphicsExtractor guiGraphics) {
        if (Remorphed.displayDataInMenu) {
            final int iconS = 16; // traits are always 16x16 (since that's the item size)

            // Render Trait Icons first
            int row = 0;
            int column = 0;
            List<Identifier> renderedTraits = new ArrayList<>();
            List<ShapeTrait<T>> traits = TraitRegistry.getAll(entity);
            for (ShapeTrait<T> trait : traits) {
                if (trait != null && (!renderedTraits.contains(trait.getId()) || trait.iconMightDiffer())) {
                    try {
                        boolean bl = trait.renderIcon(RenderPipelines.GUI_TEXTURED, guiGraphics, getX() + column, getY() + row, iconS, iconS);
                        if (bl) {
                            // prevent traits outside of entity widget
                            if (row + iconS >= getHeight()) {
                                column += iconS;
                                row = 0;
                            } else {
                                row += iconS;
                            }
                            if (column + iconS >= getWidth()) {
                                break;
                            }
                            renderedTraits.add(trait.getId());
                        }
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (!msg.isBlank() && !exceptions.contains(msg)) {
                            Remorphed.LOGGER.error("Error while rendering trait icon {} for {}: {}", trait.getId(), EntityType.getKey(entity.getType()), ShapeType.createTooltipText(entity).getString(), e);
                            e.fillInStackTrace();
                            exceptions.add(msg);
                        }
                    }
                }
            }
        }

        // Some entities (namely Aether mobs) crash when rendered in a GUI.
        // Unsure as to the cause, but this try/catch should prevent the game from entirely dipping out.
        try {
            int leftPos = (int) (getX() + (float) this.getWidth() / 2);
            int topPos = (int) (getY() + this.getHeight() * .75f);
            int k = leftPos - 20;
            int l = topPos - 25;
            int m = leftPos + 20;
            int n = topPos + 35;
            VariantMenu.renderEntityOnScreen(guiGraphics, k, l, m, n, (float) size,
                    new Vector3f(), new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI),
                    null, entity);
        } catch (Exception e) {
            Remorphed.LOGGER.error("Error while rendering {}", ShapeType.createTooltipText(entity).getString(), e);
            setCrashed();
            MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
            immediate.endBatch();
            RenderSystem.getModelViewStack().popMatrix();
        }
    }

    @Override
    void sendDeleteShapePacket() {
        NetworkHandler.sendDeleteShapePacket(type);
    }
}
