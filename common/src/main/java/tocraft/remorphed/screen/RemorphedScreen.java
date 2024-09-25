package tocraft.remorphed.screen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.tocraft.skinshifter.SkinShifter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
//#if MC>=1201
import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif
import net.minecraft.client.gui.components.Button;
//#if MC>1182
import net.minecraft.client.gui.components.Tooltip;
//#endif
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.patched.TComponent;
import tocraft.craftedcore.platform.PlayerProfile;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.FakeClientPlayer;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.remorphed.mixin.accessor.ScreenAccessor;
import tocraft.remorphed.screen.widget.*;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.api.variant.ShapeType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings({"DataFlowIssue", "SequencedCollectionMethodCanBeUsed"})
@Environment(EnvType.CLIENT)
public class RemorphedScreen extends Screen {
    private final List<ShapeType<?>> unlockedShapes = new CopyOnWriteArrayList<>();
    private final List<PlayerProfile> unlockedSkins = new CopyOnWriteArrayList<>();
    private final Map<ShapeType<?>, Mob> renderEntities = new ConcurrentHashMap<>();
    private final Map<PlayerProfile, FakeClientPlayer> renderPlayers = new ConcurrentHashMap<>();
    private final List<ShapeWidget> shapeWidgets = new CopyOnWriteArrayList<>();
    private final SearchWidget searchBar = createSearchBar();
    private final Button helpButton = createHelpButton();
    private final Button variantsButton = createVariantsButton();
    private final Button traitsButton = createTraitsButton();
    private final PlayerWidget playerButton = createPlayerButton();
    private final SpecialShapeWidget specialShapeButton = createSpecialShapeButton();
    private String lastSearchContents = "";

    public RemorphedScreen() {
        super(TComponent.literal(""));
        super.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
    }

    @Override
    public void init() {
        // don't initialize if the player is null
        if (minecraft == null) return;
        if (minecraft.player == null) {
            this.onClose();
            return;
        }

        addRenderableWidget(searchBar);
        addRenderableWidget(helpButton);
        addRenderableWidget(variantsButton);
        addRenderableWidget(traitsButton);
        addRenderableWidget(playerButton);
        if (Walkers.hasSpecialShape(minecraft.player.getUUID())) {
            addRenderableWidget(specialShapeButton);
        }

        if (Remorphed.CONFIG.loadMenuAsynchronous) {
            CompletableFuture.runAsync(this::instantiate);
        } else {
            instantiate();
        }
    }

    private void instantiate() {
        populateUnlockedRenderEntities(minecraft.player);
        ShapeType<? extends LivingEntity> currentShape = ShapeType.from(PlayerShape.getCurrentShape(minecraft.player));

        // handle favorites
        unlockedShapes.sort((first, second) -> {
            if (Objects.equals(first, currentShape)) {
                return -1;
            } else if (Objects.equals(second, currentShape)) {
                return 1;
            } else {
                boolean firstIsFav = PlayerMorph.getFavoriteShapes(minecraft.player).contains(first);
                boolean secondIsFav = PlayerMorph.getFavoriteShapes(minecraft.player).contains(second);
                if (firstIsFav == secondIsFav) {
                    return 0;
                }
                else if (firstIsFav) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        });

        // filter unlocked
        if (!Remorphed.displayVariantsInMenu) {
            List<ShapeType<?>> newUnlocked = new ArrayList<>();
            for (ShapeType<?> shapeType : unlockedShapes) {
                if (shapeType.equals(currentShape) || !newUnlocked.stream().map(ShapeType::getEntityType).toList().contains(shapeType.getEntityType())) {
                    newUnlocked.add(shapeType);
                }
            }

            unlockedShapes.clear();
            unlockedShapes.addAll(newUnlocked);
        }

        if (Remorphed.foundSkinShifter) {
            populateUnlockedRenderPlayers(minecraft.player);
            UUID currentSkin = SkinShifter.getCurrentSkin(minecraft.player);

            unlockedSkins.sort((first, second) -> {
                if (Objects.equals(first.id(), currentSkin) && currentShape != null) {
                    return -1;
                } else if (Objects.equals(second.id(), currentSkin) && currentShape != null) {
                    return 1;
                } else {
                    boolean firstIsFav = PlayerMorph.getFavoriteSkinIds(minecraft.player).contains(first.id());
                    boolean secondIsFav = PlayerMorph.getFavoriteSkinIds(minecraft.player).contains(second.id());
                    if (firstIsFav == secondIsFav) {
                        return first.name().compareTo(second.name());
                    }
                    else if (firstIsFav) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            });
        }

        populateShapeWidgets(unlockedShapes, unlockedSkins);

        // implement search handler
        searchBar.setResponder(text -> {
            setFocused(searchBar);

            // Only re-filter if the text contents changed
            if (!lastSearchContents.equals(text)) {
                ((ScreenAccessor) this).getSelectables().removeIf(button -> button instanceof EntityWidget);
                children().removeIf(button -> button instanceof EntityWidget);

                List<ShapeType<?>> filteredShapes = unlockedShapes
                        .stream()
                        .filter(type -> text.isEmpty() || ShapeType.createTooltipText(renderEntities.get(type)).getString().toUpperCase().contains(text.toUpperCase()) || EntityType.getKey(type.getEntityType()).toString().toUpperCase().contains(text.toUpperCase()))
                        .toList();
                List<PlayerProfile> filteredSkins = unlockedSkins
                        .stream()
                        .filter(skin -> text.isEmpty() || skin.name().toUpperCase().contains(text.toUpperCase()) || skin.id().toString().contains(text.toUpperCase()))
                        .toList();

                populateShapeWidgets(filteredShapes, filteredSkins);
            }

            lastSearchContents = text;
        });
    }

    @Override
    //#if MC>1194
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    //#else
    //$$ public void render(PoseStack context, int mouseX, int mouseY, float delta) {
    //#endif
        //#if MC>1201
        renderTransparentBackground(context);
        //#else
        //$$ renderBackground(context);
        //#endif

        searchBar.render(context, mouseX, mouseY, delta);
        helpButton.render(context, mouseX, mouseY, delta);
        variantsButton.render(context, mouseX, mouseY, delta);
        traitsButton.render(context, mouseX, mouseY, delta);
        playerButton.render(context, mouseX, mouseY, delta);
        if (Walkers.hasSpecialShape(minecraft.player.getUUID())) {
            specialShapeButton.render(context, mouseX, mouseY, delta);
        }

        double scaledFactor = this.minecraft.getWindow().getGuiScale();
        int top = 35;

        //#if MC>1194
        context.pose().pushPose();
        //#else
        //$$ context.pushPose();
        //#endif
        RenderSystem.enableScissor(
                (int) ((double) 0 * scaledFactor),
                (int) ((double) 0 * scaledFactor),
                (int) ((double) width * scaledFactor),
                (int) ((double) (this.height - top) * scaledFactor));

        for (ShapeWidget widget : shapeWidgets) {
            if (widget.getY() + widget.getHeight() > top && widget.getY() < getWindow().getGuiScaledHeight()) {
                widget.render(context, mouseX, mouseY, delta);
            }
        }

        RenderSystem.disableScissor();

        //#if MC>1194
        context.pose().popPose();
        //#else
        //$$ context.popPose();
        //#endif
    }

    @Override
    //#if MC>1201
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    //#else
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    //#endif
        if (!shapeWidgets.isEmpty()) {
            float firstPos = shapeWidgets.get(0).getY();
            ShapeWidget lastWidget = shapeWidgets.get(shapeWidgets.size() - 1);

            // Top section should always have mobs, prevent scrolling the entire list down the screen
            if ((scrollY >= 0 && firstPos >= 35) || (scrollY <= 0 && lastWidget.getY() <= getWindow().getGuiScaledHeight() - lastWidget.getHeight())) {
                return false;
            }

            for (NarratableEntry button : ((ScreenAccessor) this).getSelectables()) {
                if (button instanceof ShapeWidget widget) {
                    //#if MC>1182
                    widget.setPosition(widget.getX(), (int) (widget.getY() + scrollY * 10));
                    //#else
                    //$$ widget.y += (int) (scrollY * 10);
                    //#endif
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void populateShapeWidgets(List<ShapeType<?>> rendered, List<PlayerProfile> skinProfiles) {
        shapeWidgets.clear();
        // add widget for each entity to be rendered
        int x = 15;
        int y = 35;
        int rows = (int) Math.ceil((rendered.size() + skinProfiles.size()) / 7f);

        ShapeType<LivingEntity> currentType = ShapeType.from(PlayerShape.getCurrentShape(minecraft.player));

        for (int yIndex = 0; yIndex <= rows; yIndex++) {
            for (int xIndex = 0; xIndex < 7; xIndex++) {
                int listIndex = yIndex * 7 + xIndex;

                if (Remorphed.foundSkinShifter && listIndex < skinProfiles.size()) {
                    PlayerProfile skinProfile = skinProfiles.get(listIndex);
                    FakeClientPlayer fakePlayer = renderPlayers.get(skinProfile);
                    if (fakePlayer != null) {
                        SkinWidget skinWidget = new SkinWidget(
                                (getWindow().getGuiScaledWidth() - 27) / 7f * xIndex + x,
                                getWindow().getGuiScaledHeight() / 5f * yIndex + y,
                                (getWindow().getGuiScaledWidth() - 27) / 7f,
                                getWindow().getGuiScaledHeight() / 5f,
                                skinProfile,
                                new FakeClientPlayer(minecraft.level, skinProfile),
                                this,
                                PlayerMorph.getFavoriteSkins(minecraft.player).contains(skinProfile),
                                Objects.equals(SkinShifter.getCurrentSkin(minecraft.player), skinProfile.id()) && currentType == null
                        );

                        addRenderableWidget(skinWidget);
                        shapeWidgets.add(skinWidget);
                    } else {
                        Remorphed.LOGGER.error("invalid skin profile: {}", skinProfile);
                    }
                }
                else if (listIndex < skinProfiles.size() + rendered.size()) {
                    ShapeType<?> type = rendered.get(listIndex - skinProfiles.size());
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
                                PlayerMorph.getFavoriteShapes(minecraft.player).contains(type),
                                type.equals(currentType)
                        );

                        addRenderableWidget(entityWidget);
                        shapeWidgets.add(entityWidget);
                    } else {
                        Remorphed.LOGGER.error("invalid shape type: {}", type.getEntityType().getDescriptionId());
                    }
                }
            }
        }
    }

    public synchronized void populateUnlockedRenderEntities(Player player) {
        unlockedShapes.clear();
        renderEntities.clear();
        List<ShapeType<?>> validUnlocked = Remorphed.getUnlockedShapes(player);
        for (ShapeType<?> type : validUnlocked) {
            Entity entity = type.create(Minecraft.getInstance().level);
            if (entity instanceof Mob living) {
                unlockedShapes.add(type);
                renderEntities.put(type, living);
            }
        }

        Remorphed.LOGGER.info("Loaded {} entities for rendering", unlockedShapes.size());
    }

    public synchronized void populateUnlockedRenderPlayers(Player player) {
        unlockedSkins.clear();
        renderPlayers.clear();
        List<PlayerProfile> validUnlocked = Remorphed.getUnlockedSkins(player);
        for (PlayerProfile profile : validUnlocked) {
            if (profile.id() != player.getUUID()) {
                FakeClientPlayer entity = new FakeClientPlayer(minecraft.level, profile);
                unlockedSkins.add(profile);
                renderPlayers.put(profile, entity);
            }
        }

        Remorphed.LOGGER.info("Loaded {} players for rendering", unlockedSkins.size());
    }

    private SearchWidget createSearchBar() {
        return new SearchWidget(
                getWindow().getGuiScaledWidth() / 2f - (getWindow().getGuiScaledWidth() / 4f / 2) - 5,
                5,
                getWindow().getGuiScaledWidth() / 4f,
                20f);
    }

    //#if MC>1182
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
    
    private Button createTraitsButton() {
        Button.Builder traitButton = Button.builder(Component.translatable("remorphed.show_traits"), (widget) -> Remorphed.displayTraitsInMenu = !Remorphed.displayTraitsInMenu);
        int xOffset = Walkers.hasSpecialShape(Minecraft.getInstance().player.getUUID()) ? 30 : 0;
    
        int xPos = (int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 65 + xOffset);
        traitButton.pos(xPos, 5);
        traitButton.size(Math.min(50, getWindow().getGuiScaledWidth() - xPos), 20);
        traitButton.tooltip(Tooltip.create(Component.translatable(Remorphed.MODID + ".traits")));
    
        return traitButton.build();
    }
    //#else
    //$$ private Button createHelpButton() {
    //$$     int xOffset = Walkers.hasSpecialShape(Minecraft.getInstance().player.getUUID()) ? 30 : 0;
    //$$     return new Button((int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 35 + xOffset), 5, 20, 20, Component.nullToEmpty("?"), (widget) -> Minecraft.getInstance().setScreen(new RemorphedHelpScreen()));
    //$$ }
    //$$
    //$$ private Button createVariantsButton() {
    //$$     return new Button((int) (getWindow().getGuiScaledWidth() / 2f - (getWindow().getGuiScaledWidth() / 4f / 2) - 110), 5, 100, 20,  TComponent.translatable("remorphed.display_variants"), (widget) -> {
    //$$         Remorphed.displayVariantsInMenu = !Remorphed.displayVariantsInMenu;
    //$$         Minecraft.getInstance().setScreen(new RemorphedScreen());
    //$$     });
    //$$ }
    //$$
    //$$ private Button createTraitsButton() {
    //$$     int xOffset = Walkers.hasSpecialShape(Minecraft.getInstance().player.getUUID()) ? 30 : 0;
    //$$     int xPos = (int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 65 + xOffset);
    //$$     return new Button(xPos, 5, Math.min(50, getWindow().getGuiScaledWidth() - xPos), 20,  TComponent.translatable("remorphed.show_traits"), (widget) -> {
    //$$         Remorphed.displayTraitsInMenu = !Remorphed.displayTraitsInMenu;
    //$$     });
    //$$ }
    //#endif

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
            return searchBar.mouseClicked(mouseX, mouseY, button) || helpButton.mouseClicked(mouseX, mouseY, button) || variantsButton.mouseClicked(mouseX, mouseY, button) || traitsButton.mouseClicked(mouseX, mouseY, button) || playerButton.mouseClicked(mouseX, mouseY, button) || specialShapeButton.mouseClicked(mouseX, mouseY, button);
        } else {
            for (ShapeWidget shapeWidget : shapeWidgets) {
                if (shapeWidget.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(shapeWidget);
                    return true;
                }
            }
        }
        return false;
    }
}
