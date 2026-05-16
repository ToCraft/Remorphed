package dev.tocraft.remorphed.screen;

import com.mojang.authlib.GameProfile;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.RemorphedClient;
import dev.tocraft.walkers.screen.hud.VariantMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Invisible screen that pre-renders entities off-screen to force texture/model loading.
 * Renders in batches to avoid lag.
 * Uses the same ID calculation as RemorphedMenu for GuiShapeRenderer cache compatibility.
 */
@Environment(EnvType.CLIENT)
public class EntityPreloadScreen extends Screen {
    private final List<Mob> entitiesToRender;
    private final List<GameProfile> skinsToRender;
    private int currentIndex = 0;
    private int finishedTicks = 0; // Ticks since finishing rendering
    private static final int ENTITIES_PER_FRAME = 10; // Render 10 entities per frame
    private static final int WAIT_AFTER_RENDER = 5; // Wait 5 ticks after rendering for textures to load

    public EntityPreloadScreen(List<Mob> entities, List<GameProfile> skins) {
        super(Component.literal("Preloading"));
        this.entitiesToRender = entities;
        this.skinsToRender = skins;
    }

    @Override
    protected void init() {
        super.init();
        // Don't add any widgets - keep it completely empty
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        // Don't call super.render() - no background, no widgets, nothing visible

        // Render a batch of entities far off-screen
        int rendered = 0;
        while (currentIndex < entitiesToRender.size() && rendered < ENTITIES_PER_FRAME) {
            Mob entity = entitiesToRender.get(currentIndex);

            // Calculate the list index (offset by skin count)
            int listIndex = skinsToRender.size() + currentIndex;

            // Calculate grid position (same as RemorphedMenu)
            int row = listIndex / Remorphed.CONFIG.shapes_per_row;
            int col = listIndex % Remorphed.CONFIG.shapes_per_row;

            try {
                // Render at reasonable size but off-screen to force texture loading
                int size = (int) (Remorphed.CONFIG.entity_size * (1 / (Math.max(entity.getBbHeight(), entity.getBbWidth()))));

                VariantMenu.renderEntityOnScreen(
                        guiGraphics,
                        -10000, -10000,  // Off-screen
                        -9900, -9900,
                        size,
                        new Vector3f(),
                        new Quaternionf().rotationXYZ(0.43633232F, (float) Math.PI, (float) Math.PI),
                        null,
                        entity
                );
            } catch (Exception e) {
                // Silent - entity will load on-demand if pre-render fails
            }

            currentIndex++;
            rendered++;
        }

        // Wait a few ticks after rendering completes to let textures fully load
        if (currentIndex >= entitiesToRender.size()) {
            finishedTicks++;
            if (finishedTicks >= WAIT_AFTER_RENDER) {
                Minecraft.getInstance().setScreen(new RemorphedMenu());
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Don't render ANY background - keep it completely transparent/invisible
    }
}
