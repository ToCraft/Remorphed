package tocraft.remorphed.screen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.remorphed.mixin.accessor.ScreenAccessor;
import tocraft.remorphed.screen.widget.EntityWidget;
import tocraft.remorphed.screen.widget.PlayerWidget;
import tocraft.remorphed.screen.widget.SearchWidget;
import tocraft.remorphed.screen.widget.SpecialShapeWidget;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.variant.ShapeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class RemorphedScreen extends Screen {
    private final List<ShapeType<?>> unlocked = new ArrayList<>();
    private final Map<ShapeType<?>, Mob> renderEntities = new LinkedHashMap<>();
    private final List<EntityWidget<?>> entityWidgets = new ArrayList<>();
    private final SearchWidget searchBar = createSearchBar();
    private final Button helpButton = createHelpButton();
    private final Button variantsButton = createVariantsButton();
    private final Button skillsButton = createSkillsButton();
    private final PlayerWidget playerButton = createPlayerButton();
    private final SpecialShapeWidget specialShapeButton = createSpecialShapeButton();
    private String lastSearchContents = "";
    private Thread populateUnlocked = null;

    public RemorphedScreen() {
        super(Component.literal(""));
        super.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
    }

    @Override
    public void init() {
        // don't initialize if the player is null
        if (minecraft == null) return;
        if (minecraft.player == null) {
            minecraft.setScreen(null);
            return;
        }

        addRenderableWidget(searchBar);
        addRenderableWidget(helpButton);
        addRenderableWidget(variantsButton);
        addRenderableWidget(skillsButton);
        addRenderableWidget(playerButton);
        if (Walkers.hasSpecialShape(minecraft.player.getUUID()))
            addRenderableWidget(specialShapeButton);

        populateUnlocked = new Thread(() -> {
            populateUnlockedRenderEntities(minecraft.player);

            ShapeType<? extends LivingEntity> currentShape = ShapeType.from(PlayerShape.getCurrentShape(minecraft.player));

            // handle favorites
            unlocked.sort((first, second) -> {
                if (first.equals(currentShape)) {
                    return -1;
                } else if (second.equals(currentShape)) {
                    return 1;
                } else {
                    boolean firstIsFav = ((RemorphedPlayerDataProvider) minecraft.player).remorphed$getFavorites().contains(first);
                    boolean secondIsFav = ((RemorphedPlayerDataProvider) minecraft.player).remorphed$getFavorites().contains(second);
                    if (firstIsFav == secondIsFav)
                        return 0;
                    if (firstIsFav)
                        return -1;
                    else return 1;
                }
            });

            // filter unlocked
            if (!unlocked.isEmpty() && !Remorphed.displayVariantsInMenu) {
                List<ShapeType<?>> newUnlocked = new ArrayList<>();
                for (ShapeType<?> shapeType : unlocked) {
                    if (shapeType.equals(currentShape) || !newUnlocked.stream().map(ShapeType::getEntityType).toList().contains(shapeType.getEntityType())) {
                        newUnlocked.add(shapeType);
                    }
                }

                unlocked.clear();
                unlocked.addAll(newUnlocked);
            }
        }, "cache entities");
        populateUnlocked.start();

        // implement search handler
        searchBar.setResponder(text -> {
            magicalSpecialHackyFocus(searchBar);

            // Only re-filter if the text contents changed
            if (!lastSearchContents.equals(text)) {
                ((ScreenAccessor) this).getSelectables().removeIf(button -> button instanceof EntityWidget);
                children().removeIf(button -> button instanceof EntityWidget);
                entityWidgets.clear();

                List<ShapeType<?>> filtered = unlocked
                        .stream()
                        .filter(type -> text.isEmpty() || ShapeType.createTooltipText(renderEntities.get(type)).getString().toUpperCase().contains(text.toUpperCase()) || EntityType.getKey(type.getEntityType()).toString().toUpperCase().contains(text.toUpperCase()))
                        .collect(Collectors.toList());

                populateEntityWidgets(filtered);
            }

            lastSearchContents = text;
        });
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (populateUnlocked != null && entityWidgets.isEmpty() && !populateUnlocked.isAlive()) {
            populateUnlocked = null;
            // add entity widgets
            populateEntityWidgets(unlocked);
        }

        renderTransparentBackground(context);

        searchBar.render(context, mouseX, mouseY, delta);
        helpButton.render(context, mouseX, mouseY, delta);
        variantsButton.render(context, mouseX, mouseY, delta);
        skillsButton.render(context, mouseX, mouseY, delta);
        playerButton.render(context, mouseX, mouseY, delta);
        if (Walkers.hasSpecialShape(minecraft.player.getUUID()))
            specialShapeButton.render(context, mouseX, mouseY, delta);

        double scaledFactor = this.minecraft.getWindow().getGuiScale();
        int top = 35;

        context.pose().pushPose();
        RenderSystem.enableScissor(
                (int) ((double) 0 * scaledFactor),
                (int) ((double) 0 * scaledFactor),
                (int) ((double) width * scaledFactor),
                (int) ((double) (this.height - top) * scaledFactor));

        for (EntityWidget<?> widget : entityWidgets) {
            if (widget.getY() + widget.getHeight() > top && widget.getY() < getWindow().getGuiScaledHeight()) {
                widget.render(context, mouseX, mouseY, delta);
            }
        }

        RenderSystem.disableScissor();

        context.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!entityWidgets.isEmpty()) {
            float firstPos = entityWidgets.get(0).getY();
            EntityWidget<?> lastWidget = entityWidgets.get(entityWidgets.size() - 1);

            // Top section should always have mobs, prevent scrolling the entire list down the screen
            if ((scrollY >= 0 && firstPos >= 35) || (scrollY <= 0 && lastWidget.getY() <= getWindow().getGuiScaledHeight() - lastWidget.getHeight())) {
                return false;
            }

            for (NarratableEntry button : ((ScreenAccessor) this).getSelectables()) {
                if (button instanceof EntityWidget<?> widget) {
                    widget.setPosition(widget.getX(), (int) (widget.getY() + scrollY * 10));
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void populateEntityWidgets(List<ShapeType<?>> rendered) {
        // add widget for each entity to be rendered
        int x = 15;
        int y = 35;
        int rows = (int) Math.ceil(rendered.size() / 7f);

        ShapeType<LivingEntity> currentType = ShapeType.from(PlayerShape.getCurrentShape(minecraft.player));

        for (int yIndex = 0; yIndex <= rows; yIndex++) {
            for (int xIndex = 0; xIndex < 7; xIndex++) {
                int listIndex = yIndex * 7 + xIndex;

                if (listIndex < rendered.size()) {
                    ShapeType<?> type = rendered.get(listIndex);
                    Mob entity = renderEntities.get(type);
                    if (entity != null) {
                        EntityWidget<?> entityWidget = new EntityWidget<>(
                                (getWindow().getGuiScaledWidth() - 27) / 7f * xIndex + x,
                                getWindow().getGuiScaledHeight() / 5f * yIndex + y,
                                (getWindow().getGuiScaledWidth() - 27) / 7f,
                                getWindow().getGuiScaledHeight() / 5f,
                                (ShapeType<Mob>) type,
                                entity,
                                this,
                                ((RemorphedPlayerDataProvider) minecraft.player).remorphed$getFavorites().contains(type),
                                type.equals(currentType)
                        );

                        addRenderableWidget(entityWidget);
                        entityWidgets.add(entityWidget);
                    } else {
                        Remorphed.LOGGER.error("invalid shape type: " + type.getEntityType().getDescriptionId());
                    }
                }
            }
        }
    }

    public synchronized void populateUnlockedRenderEntities(Player player) {
        if (renderEntities.isEmpty() && Minecraft.getInstance().level != null) {
            List<ShapeType<?>> validUnlocked = Remorphed.getUnlockedShapes(player);
            for (ShapeType<?> type : validUnlocked) {
                Entity entity = type.create(Minecraft.getInstance().level);
                if (entity instanceof Mob living) {
                    unlocked.add(type);
                    renderEntities.put(type, living);
                }
            }

            Remorphed.LOGGER.info(String.format("Loaded %d entities for rendering", unlocked.size()));
        }
    }

    private SearchWidget createSearchBar() {
        return new SearchWidget(
                getWindow().getGuiScaledWidth() / 2f - (getWindow().getGuiScaledWidth() / 4f / 2) - 5,
                5,
                getWindow().getGuiScaledWidth() / 4f,
                20f);
    }

    private Button createHelpButton() {
        Button.Builder helpButton = Button.builder(Component.nullToEmpty("?"), (widget) -> Minecraft.getInstance().setScreen(new RemorphedHelpScreen()));

        int xOffset = Walkers.hasSpecialShape(Minecraft.getInstance().player.getUUID()) ? 30 : 0;

        helpButton.pos((int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 35 + xOffset), 5);
        helpButton.size(20, 20);
        helpButton.tooltip(Tooltip.create(Component.translatable(Remorphed.MODID + ".help")));
        return helpButton.build();
    }

    private Button createVariantsButton() {
        Button.Builder variantsButton = Button.builder(Component.translatable("remorphed.display_variants"), (widget) -> {
            Remorphed.displayVariantsInMenu = !Remorphed.displayVariantsInMenu;
            Minecraft.getInstance().setScreen(new RemorphedScreen());
        });

        variantsButton.pos((int) (getWindow().getGuiScaledWidth() / 2f - (getWindow().getGuiScaledWidth() / 4f / 2) - 110), 5);
        variantsButton.size(100, 20);
        variantsButton.tooltip(Tooltip.create(Component.translatable(Remorphed.MODID + ".variants")));

        return variantsButton.build();
    }

    private Button createSkillsButton() {
        Button.Builder skillButton = Button.builder(Component.translatable("remorphed.show_skills"), (widget) -> Remorphed.displaySkillsInMenu = !Remorphed.displaySkillsInMenu);
        int xOffset = Walkers.hasSpecialShape(Minecraft.getInstance().player.getUUID()) ? 30 : 0;

        int xPos = (int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 65 + xOffset);
        skillButton.pos(xPos, 5);
        skillButton.size(Math.min(50, getWindow().getGuiScaledWidth() - xPos), 20);
        skillButton.tooltip(Tooltip.create(Component.translatable(Remorphed.MODID + ".skills")));

        return skillButton.build();
    }

    private PlayerWidget createPlayerButton() {
        return new PlayerWidget(
                (int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 5),
                5,
                20,
                20,
                this);
    }

    private SpecialShapeWidget createSpecialShapeButton() {
        return new SpecialShapeWidget(
                (int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 35),
                5,
                20,
                20,
                this);
    }

    private Window getWindow() {
        return Minecraft.getInstance().getWindow();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY < 35) {
            return searchBar.mouseClicked(mouseX, mouseY, button) || helpButton.mouseClicked(mouseX, mouseY, button) || variantsButton.mouseClicked(mouseX, mouseY, button) || skillsButton.mouseClicked(mouseX, mouseY, button) || playerButton.mouseClicked(mouseX, mouseY, button) || specialShapeButton.mouseClicked(mouseX, mouseY, button);
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
