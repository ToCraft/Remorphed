package tocraft.remorphed.screen;

import com.mojang.blaze3d.platform.Window;
import dev.tocraft.skinshifter.SkinShifter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public class RemorphedMenu extends Screen {
    @Nullable
    protected ShapeListWidget list;
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private String lastSearchContents = "";

    private final List<ShapeType<?>> unlockedShapes = new CopyOnWriteArrayList<>();
    private final List<PlayerProfile> unlockedSkins = new CopyOnWriteArrayList<>();
    private final Map<ShapeType<?>, Mob> renderEntities = new ConcurrentHashMap<>();
    private final Map<PlayerProfile, FakeClientPlayer> renderPlayers = new ConcurrentHashMap<>();

    private final Button variantsButton = createVariantsButton();
    private final SearchWidget searchBar = createSearchBar();
    private final Button helpButton = createHelpButton();
    private final PlayerWidget playerButton = createPlayerButton();
    private final SpecialShapeWidget specialShapeButton = createSpecialShapeButton();
    private final Button traitsButton = createTraitsButton();

    public RemorphedMenu() {
        super(Component.literal("ReMorphed Menu"));
    }

    protected void init() {
        this.addHeader();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addHeader() {
        LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);

        linearLayout.addChild(variantsButton);
        linearLayout.addChild(searchBar);
        linearLayout.addChild(helpButton);
        linearLayout.addChild(playerButton);

        if (minecraft != null && minecraft.player != null && Walkers.hasSpecialShape(minecraft.player.getUUID())) {
            linearLayout.addChild(specialShapeButton);
        }

        linearLayout.addChild(traitsButton);
    }

    protected void addContents() {
        this.list = this.layout.addToContents(new ShapeListWidget(this.minecraft, this.width, this.layout));

        if (minecraft != null) {
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
                    } else if (firstIsFav) {
                        return -1;
                    } else {
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
                        } else if (firstIsFav) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            }

            populateShapeWidgets(unlockedShapes, unlockedSkins);
        }

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

    @SuppressWarnings("unchecked")
    private void populateShapeWidgets(@NotNull List<ShapeType<?>> rendered, @NotNull List<PlayerProfile> skinProfiles) {
        if (this.list != null) {
            this.list.clearEntries();

            // add widget for each entity to be rendered
            int rows = (int) Math.ceil((rendered.size() + skinProfiles.size()) / 5f);

            ShapeType<LivingEntity> currentType = null;
            if (minecraft != null) {
                currentType = ShapeType.from(PlayerShape.getCurrentShape(minecraft.player));
            }

            for (int i = 0; i <= rows; i++) {
                List<ShapeWidget> row = new ArrayList<>();

                for (int j = 0; j < 5; j++) {
                    int listIndex = i * 5 + j;

                    if (Remorphed.foundSkinShifter && listIndex < skinProfiles.size()) {
                        PlayerProfile skinProfile = skinProfiles.get(listIndex);
                        FakeClientPlayer fakePlayer = renderPlayers.get(skinProfile);
                        if (fakePlayer != null) {
                            row.add(new SkinWidget(
                                    0,
                                    0,
                                    0,
                                    0,
                                    skinProfile,
                                    new FakeClientPlayer(minecraft.level, skinProfile),
                                    this,
                                    PlayerMorph.getFavoriteSkins(minecraft.player).contains(skinProfile),
                                    Objects.equals(SkinShifter.getCurrentSkin(minecraft.player), skinProfile.id()) && currentType == null
                            ));
                        } else {
                            Remorphed.LOGGER.error("invalid skin profile: {}", skinProfile);
                        }
                    } else if (listIndex < skinProfiles.size() + rendered.size()) {
                        ShapeType<?> type = rendered.get(listIndex - skinProfiles.size());
                        Mob entity = renderEntities.get(type);
                        if (entity != null) {
                            row.add(new EntityWidget<>(
                                    0,
                                    0,
                                    0,
                                    0,
                                    (ShapeType<Mob>) type,
                                    entity,
                                    this,
                                    PlayerMorph.getFavoriteShapes(minecraft.player).contains(type),
                                    type.equals(currentType)
                            ));
                        } else {
                            Remorphed.LOGGER.error("invalid shape type: {}", type.getEntityType().getDescriptionId());
                        }
                    }
                }

                this.list.addRow(row.toArray(ShapeWidget[]::new));
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
                FakeClientPlayer entity = null;
                if (minecraft != null) {
                    entity = new FakeClientPlayer(minecraft.level, profile);
                }
                unlockedSkins.add(profile);
                renderPlayers.put(profile, entity);
            }
        }

        Remorphed.LOGGER.info("Loaded {} players for rendering", unlockedSkins.size());
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).width(200).build());
    }

    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
    }

    @Contract(" -> new")
    private @NotNull SearchWidget createSearchBar() {
        return new SearchWidget(
                0,
                0,
                getWindow().getGuiScaledWidth() / 4f,
                20f);
    }

    private @NotNull Button createHelpButton() {
        Button.Builder helpButton = Button.builder(Component.nullToEmpty("?"), (widget) -> Minecraft.getInstance().setScreen(new RemorphedHelpScreen()));

        helpButton.size(20, 20);
        helpButton.tooltip(Tooltip.create(Component.translatable(Remorphed.MODID + ".help")));
        return helpButton.build();
    }

    private @NotNull Button createVariantsButton() {
        Button.Builder variantsButton = Button.builder(Component.translatable("remorphed.display_variants"), (widget) -> {
            Remorphed.displayVariantsInMenu = !Remorphed.displayVariantsInMenu;
            Minecraft.getInstance().setScreen(new RemorphedMenu());
        });

        variantsButton.size(100, 20);
        variantsButton.tooltip(Tooltip.create(Component.translatable(Remorphed.MODID + ".variants")));

        return variantsButton.build();
    }

    private @NotNull Button createTraitsButton() {
        Button.Builder traitButton = Button.builder(Component.translatable("remorphed.show_traits"), (widget) -> Remorphed.displayTraitsInMenu = !Remorphed.displayTraitsInMenu);
        int xOffset = 0;
        if (Minecraft.getInstance().player != null) {
            xOffset = Walkers.hasSpecialShape(Minecraft.getInstance().player.getUUID()) ? 30 : 0;
        }

        int xPos = (int) (getWindow().getGuiScaledWidth() / 2f + (getWindow().getGuiScaledWidth() / 8f) + 65 + xOffset);
        traitButton.size(Math.min(50, getWindow().getGuiScaledWidth() - xPos), 20);
        traitButton.tooltip(Tooltip.create(Component.translatable(Remorphed.MODID + ".traits")));

        return traitButton.build();
    }

    @Contract(" -> new")
    private @NotNull PlayerWidget createPlayerButton() {
        return new PlayerWidget(
                0,
                0,
                20,
                20,
                this);
    }

    @Contract(" -> new")
    private @NotNull SpecialShapeWidget createSpecialShapeButton() {
        return new SpecialShapeWidget(
                0,
                0,
                20,
                20,
                this);
    }

    private @NotNull Window getWindow() {
        return Minecraft.getInstance().getWindow();
    }
}
