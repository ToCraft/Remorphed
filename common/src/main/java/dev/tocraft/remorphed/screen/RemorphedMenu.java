package dev.tocraft.remorphed.screen;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Window;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.FakeClientPlayer;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.remorphed.mixin.client.accessor.ScreenAccessor;
import dev.tocraft.remorphed.screen.widget.*;
import dev.tocraft.skinshifter.SkinShifter;
import dev.tocraft.walkers.Walkers;
import dev.tocraft.walkers.api.PlayerShape;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public class RemorphedMenu extends Screen {
    @Nullable
    protected ShapeListWidget list;
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private static String lastSearchContents = "";

    private final List<ShapeType<?>> unlockedShapes = new CopyOnWriteArrayList<>();
    private final List<GameProfile> unlockedSkins = new CopyOnWriteArrayList<>();
    private final Map<ShapeType<?>, Mob> renderEntities = new ConcurrentHashMap<>();
    private final Map<GameProfile, FakeClientPlayer> renderPlayers = new ConcurrentHashMap<>();
    
    // Cache for EntityRenderState with proper scale - this is what prevents visual reloading
    private static final Map<ShapeType<?>, EntityRenderState> CACHED_ENTITY_RENDER_STATES = new ConcurrentHashMap<>();
    private static final Map<GameProfile, EntityRenderState> CACHED_PLAYER_RENDER_STATES = new ConcurrentHashMap<>();
    

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

        linearLayout.addChild(traitsButton);

        linearLayout.addChild(searchBar);
        linearLayout.addChild(helpButton);
        linearLayout.addChild(playerButton);

        if (minecraft != null && minecraft.player != null && Walkers.hasSpecialShape(minecraft.player.getUUID())) {
            linearLayout.addChild(specialShapeButton);
        }
    }

    protected void addContents() {
        this.list = this.layout.addToContents(new ShapeListWidget(this.minecraft, this.width, this.layout));

        if (minecraft != null && minecraft.player != null) {
            populateUnlockedRenderEntities(minecraft.player);

            ShapeType<? extends LivingEntity> currentShape = ShapeType.from(PlayerShape.getCurrentShape(minecraft.player));

            // handle favorites
            unlockedShapes.sort((first, second) -> {
                // sort by selected
                if (Remorphed.CONFIG.sort_selected) {
                    if (Objects.equals(first, currentShape)) {
                        return -1;
                    } else if (Objects.equals(second, currentShape)) {
                        return 1;
                    }
                }
                // sort by favorite
                boolean firstIsFav = PlayerMorph.getFavoriteShapes(minecraft.player).contains(first);
                boolean secondIsFav = PlayerMorph.getFavoriteShapes(minecraft.player).contains(second);
                if (firstIsFav == secondIsFav) {
                    return 0;
                } else if (firstIsFav) {
                    return -1;
                } else {
                    return 1;
                }
            });


            // filter unlocked
            List<ShapeType<?>> newUnlocked = new ArrayList<>();
            for (ShapeType<?> shapeType : unlockedShapes) {
                if (shapeType.equals(currentShape) || !newUnlocked.stream().map(ShapeType::getEntityType).toList().contains(shapeType.getEntityType())) {
                    newUnlocked.add(shapeType);
                }
            }

            unlockedShapes.clear();
            unlockedShapes.addAll(newUnlocked);

            if (Remorphed.foundSkinShifter) {
                populateUnlockedRenderPlayers(minecraft.player);
                UUID currentSkin = SkinShifter.getCurrentSkin(minecraft.player);

                unlockedSkins.sort((first, second) -> {
                    if (Objects.equals(first.getId(), currentSkin) && currentShape != null) {
                        return -1;
                    } else if (Objects.equals(second.getId(), currentSkin) && currentShape != null) {
                        return 1;
                    } else {
                        boolean firstIsFav = PlayerMorph.getFavoriteSkinIds(minecraft.player).contains(first.getId());
                        boolean secondIsFav = PlayerMorph.getFavoriteSkinIds(minecraft.player).contains(second.getId());
                        if (firstIsFav == secondIsFav) {
                            return first.getName().compareTo(second.getName());
                        } else if (firstIsFav) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            }
        }

        // implement search handler and display matching entities
        searchBar.setResponder(text -> {
            // re-filter if the text contents changed
            ((ScreenAccessor) this).getNarratables().removeIf(button -> button instanceof EntityWidget);
            children().removeIf(button -> button instanceof EntityWidget);

            List<ShapeType<?>> filteredShapes = unlockedShapes
                    .stream()
                    .filter(type -> text.isEmpty() || ShapeType.createTooltipText(renderEntities.get(type)).getString().toUpperCase().contains(text.toUpperCase()) || EntityType.getKey(type.getEntityType()).toString().toUpperCase().contains(text.toUpperCase()))
                    .toList();
            List<GameProfile> filteredSkins = unlockedSkins
                    .stream()
                    .filter(skin -> text.isEmpty() || skin.getName().toUpperCase().contains(text.toUpperCase()) || skin.getId().toString().contains(text.toUpperCase()))
                    .toList();

            populateShapeWidgets(filteredShapes, filteredSkins);

            lastSearchContents = text;
        });
        searchBar.insertText(lastSearchContents);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // make the background DARK
        renderTransparentBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @SuppressWarnings("unchecked")
    private void populateShapeWidgets(@NotNull List<ShapeType<?>> rendered, @NotNull List<GameProfile> skinProfiles) {
        if (this.list != null && minecraft != null && minecraft.player != null) {
            this.list.clearEntries();

            // add widget for each entity to be rendered
            int rows = (int) Math.ceil((float) (rendered.size() + skinProfiles.size()) / Remorphed.CONFIG.shapes_per_row);

            ShapeType<LivingEntity> currentType = ShapeType.from(PlayerShape.getCurrentShape(minecraft.player));
            int currentRow = 0;

            for (int i = 0; i <= rows; i++) {
                List<ShapeWidget> row = new ArrayList<>();

                for (int j = 0; j < Remorphed.CONFIG.shapes_per_row; j++) {
                    int listIndex = i * Remorphed.CONFIG.shapes_per_row + j;

                    if (Remorphed.foundSkinShifter && listIndex < skinProfiles.size()) {
                        GameProfile skinProfile = skinProfiles.get(listIndex);
                        AbstractClientPlayer fakePlayer = renderPlayers.get(skinProfile);
                        if (fakePlayer != null) {
                            boolean bl = Objects.equals(SkinShifter.getCurrentSkin(minecraft.player), skinProfile.getId()) && currentType == null;
                            if (bl) currentRow = i;
                            EntityRenderState cachedPlayerRenderState = CACHED_PLAYER_RENDER_STATES.get(skinProfile);
                            row.add(new SkinWidget(
                                    0,
                                    0,
                                    0,
                                    0,
                                    skinProfile,
                                    new FakeClientPlayer(minecraft.level, skinProfile),
                                    this,
                                    PlayerMorph.getFavoriteSkins(minecraft.player).contains(skinProfile),
                                    bl,
                                    Remorphed.canUseEveryShape(minecraft.player) || Remorphed.CONFIG.playerKillValue < 1 ? -1 : Remorphed.CONFIG.playerKillValue * PlayerMorph.getPlayerKills(minecraft.player, skinProfile.getId()) - PlayerMorph.getCounter(minecraft.player, skinProfile.getId()),
                                    cachedPlayerRenderState
                            ));
                        } else {
                            Remorphed.LOGGER.error("invalid skin profile: {}", skinProfile);
                        }
                    } else if (listIndex < skinProfiles.size() + rendered.size()) {
                        ShapeType<?> type = rendered.get(listIndex - skinProfiles.size());
                        Mob entity = renderEntities.get(type);
                        if (entity != null) {
                            boolean bl = type.equals(currentType);
                            if (bl) currentRow = i;
                            EntityRenderState cachedRenderState = CACHED_ENTITY_RENDER_STATES.get(type);
                            row.add(new EntityWidget<>(
                                    i * Remorphed.CONFIG.shapes_per_row + j,
                                    0,
                                    0,
                                    0,
                                    0,
                                    (ShapeType<Mob>) type,
                                    entity,
                                    this,
                                    PlayerMorph.getFavoriteShapes(minecraft.player).contains(type),
                                    bl,
                                    Remorphed.canUseEveryShape(minecraft.player) || Remorphed.getKillValue(type.getEntityType()) < 1 ? -1 : Remorphed.getKillValue(type.getEntityType()) * PlayerMorph.getKills(minecraft.player, type) - PlayerMorph.getCounter(minecraft.player, type),
                                    cachedRenderState
                            ));
                        } else {
                            Remorphed.LOGGER.error("invalid shape type: {}", type.getEntityType().getDescriptionId());
                        }
                    }
                }

                this.list.addRow(row.toArray(ShapeWidget[]::new));
            }

            if (Remorphed.CONFIG.focus_selected) {
                // auto center the selected shape
                this.list.setScrollAmount((double) this.list.rowHeight() * (currentRow - 2));
            }
        }
    }

    public synchronized void populateUnlockedRenderEntities(Player player) {
        unlockedShapes.clear();
        renderEntities.clear();
        List<ShapeType<?>> validUnlocked = Remorphed.getUnlockedShapes(player);
        
        
        
        // Create new entities and render states only for newly unlocked shapes
        for (ShapeType<?> type : validUnlocked) {
            if (!CACHED_ENTITY_RENDER_STATES.containsKey(type)) {
                try {
                    Entity entity = type.create(Minecraft.getInstance().level, player);
                    if (entity instanceof Mob living) {
                        // Fix slimes and magma cubes - set size to 1 (smallest)
                        if (living instanceof Slime slime) {
                            slime.setSize(1, true);
                        } else if (living instanceof MagmaCube magmaCube) {
                            magmaCube.setSize(1, true);
                        }
                        
                        // Disable animations for consistent rendering
                        living.setNoAi(true);
                        living.setInvulnerable(true);
                        
                        // Create and cache the EntityRenderState with 1.0F scale (like original)
                        // The actual scaling is handled by the widget's size calculation
                        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(living);
                        EntityRenderState entityRenderState = entityRenderer.createRenderState(living, 1.0F);
                        entityRenderState.hitboxesRenderState = null;
                        CACHED_ENTITY_RENDER_STATES.put(type, entityRenderState);
                        
                        // Don't cache the entity - create fresh each time to avoid state issues
                        renderEntities.put(type, living);
                    }
                } catch (Exception e) {
                    Remorphed.LOGGER.warn("Failed to create entity for type {}: {}", type.getEntityType(), e.getMessage());
                }
            } else {
                // Create fresh entity but use cached render state
                Entity entity = type.create(Minecraft.getInstance().level, player);
                if (entity instanceof Mob living) {
                    // Fix slimes and magma cubes - set size to 1 (smallest)
                    if (living instanceof Slime slime) {
                        slime.setSize(1, true);
                    } else if (living instanceof MagmaCube magmaCube) {
                        magmaCube.setSize(1, true);
                    }
                    
                    // Disable animations for consistent rendering
                    living.setNoAi(true);
                    living.setInvulnerable(true);
                    
                    renderEntities.put(type, living);
                }
            }
        }
        
        // Add all valid unlocked shapes
        for (ShapeType<?> type : validUnlocked) {
            if (CACHED_ENTITY_RENDER_STATES.containsKey(type)) {
                unlockedShapes.add(type);
            }
        }
        

        Remorphed.LOGGER.info("Loaded {} entities for rendering", unlockedShapes.size());
    }

    public synchronized void populateUnlockedRenderPlayers(Player player) {
        unlockedSkins.clear();
        renderPlayers.clear();
        List<GameProfile> validUnlocked = Remorphed.getUnlockedSkins(player);
        
        // Filter out the player's own skin
        List<GameProfile> filteredUnlocked = validUnlocked.stream()
            .filter(profile -> profile.getId() != player.getUUID())
            .toList();
        
        // Create new fake players and render states only for newly unlocked skins
        for (GameProfile profile : filteredUnlocked) {
            if (!CACHED_PLAYER_RENDER_STATES.containsKey(profile)) {
                try {
                    if (minecraft != null) {
                        FakeClientPlayer entity = new FakeClientPlayer(minecraft.level, profile);
                        
                        // Create and cache the EntityRenderState with 1.0F scale (like original)
                        // The actual scaling is handled by the widget's size calculation
                        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(entity);
                        EntityRenderState entityRenderState = entityRenderer.createRenderState(entity, 1.0F);
                        entityRenderState.hitboxesRenderState = null;
                        CACHED_PLAYER_RENDER_STATES.put(profile, entityRenderState);
                        
                        // Don't cache the player - create fresh each time to avoid state issues
                        renderPlayers.put(profile, entity);
                    }
                } catch (Exception e) {
                    Remorphed.LOGGER.warn("Failed to create fake player for profile {}: {}", profile.getName(), e.getMessage());
                }
            } else {
                // Create fresh player but use cached render state
                if (minecraft != null) {
                    FakeClientPlayer entity = new FakeClientPlayer(minecraft.level, profile);
                    renderPlayers.put(profile, entity);
                }
            }
        }
        
        // Add all valid unlocked skins
        for (GameProfile profile : filteredUnlocked) {
            if (CACHED_PLAYER_RENDER_STATES.containsKey(profile)) {
                unlockedSkins.add(profile);
            }
        }
        

        Remorphed.LOGGER.info("Loaded {} players for rendering", unlockedSkins.size());
    }
    
    /**
     * Clears the entity and player caches. Call this when the player logs out
     * or when you want to force a complete refresh of all entities.
     */
    public static void clearCache() {
        CACHED_ENTITY_RENDER_STATES.clear();
        CACHED_PLAYER_RENDER_STATES.clear();
        Remorphed.LOGGER.info("Cleared render state caches");
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

    private @NotNull Button createTraitsButton() {
        Component text = Component.translatable("remorphed.show_traits");
        Button.Builder traitButton = Button.builder(text, (widget) -> Remorphed.displayDataInMenu = !Remorphed.displayDataInMenu);

        traitButton.size(Minecraft.getInstance().font.width(text.getString()) + 20, 20);
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

