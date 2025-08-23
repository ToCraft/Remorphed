package dev.tocraft.remorphed;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tocraft.remorphed.mixin.client.accessor.GuiGraphicsAccessor;
import dev.tocraft.remorphed.screen.render.GuiShapeRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import dev.tocraft.craftedcore.event.client.ClientPlayerEvents;
import dev.tocraft.craftedcore.event.client.ClientTickEvents;
import dev.tocraft.craftedcore.registration.KeyBindingRegistry;
import dev.tocraft.remorphed.handler.client.ClientPlayerRespawnHandler;
import dev.tocraft.remorphed.network.ClientNetworking;
import dev.tocraft.remorphed.tick.KeyPressHandler;

@Environment(EnvType.CLIENT)
public class RemorphedClient {
    public static final KeyMapping MENU_KEY = new KeyMapping("key.remorphed_menu", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, "key.categories.remorphed");

    public void initialize() {
        KeyBindingRegistry.register(MENU_KEY);

        // Register event handlers
        ClientTickEvents.CLIENT_PRE.register(new KeyPressHandler());
        ClientNetworking.registerPacketHandlers();

        ClientPlayerEvents.CLIENT_PLAYER_RESPAWN.register(new ClientPlayerRespawnHandler());
    }

    public static void renderEntityInInventory(
            int id,
            GuiGraphics guiGraphics,
            int x1,
            int y1,
            int x2,
            int y2,
            float scale,
            Vector3f translation,
            Quaternionf rotation,
            @Nullable Quaternionf overrideCameraAngle,
            LivingEntity entity
    ) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(entity, 1.0F);
        entityRenderState.hitboxesRenderState = null;

        GuiGraphicsAccessor accessor = ((GuiGraphicsAccessor) guiGraphics);
        accessor.getGuiRenderState().submitPicturesInPictureState(new GuiShapeRenderState(id, entityRenderState, translation, rotation, overrideCameraAngle, x1, y1, x2, y2, scale, accessor.getScissorStack().peek()));
    }
}
