package dev.tocraft.remorphed.mixin;

import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import dev.tocraft.walkers.api.PlayerShapeChanger;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({"DataFlowIssue", "resource", "ControlFlowStatementWithoutBraces", "unused"})
@Mixin(Player.class)
public abstract class PlayerEntityMixin implements RemorphedPlayerDataProvider {

    @Shadow public abstract boolean isCreative();

    @Unique private final Map<EntityType<?>, Integer> remorphed$unlockedShapes   = new ConcurrentHashMap<>();
    @Unique private final Set<EntityType<?>>                                remorphed$favoriteShapes    = new HashSet<>();
    @Unique private final Map<UUID, Integer>                               remorphed$unlockedSkins     = new ConcurrentHashMap<>();
    @Unique private final Set<UUID>                                        remorphed$favoriteSkins     = new CopyOnWriteArraySet<>();
    @Unique private final Map<EntityType<?>, Integer> remorphed$shapeMorphCounter = new ConcurrentHashMap<>();
    @Unique private final Map<UUID, Integer>                               remorphed$skinMorphCounter  = new ConcurrentHashMap<>();

    @Unique private final Map<EntityType<?>, ShapeType<?>> remorphed$defaultVariants = new ConcurrentHashMap<>();

    @Unique private static final String UNLOCKED_SHAPES = "UnlockedShapes";
    @Unique private static final String FAVORITE_SHAPES = "FavoriteShapes";
    @Unique private static final String UNLOCKED_SKINS  = "UnlockedSkins";
    @Unique private static final String FAVORITE_SKINS  = "FavoriteSkins";
    @Unique private static final String MORPH_COUNTER   = "MorphCounter";
    @Unique private static final String DEFAULT_VARIANTS = "DefaultVariants";

    // -------------------------------------------------------------------------
    // Tick / NBT hooks
    // -------------------------------------------------------------------------

    @Inject(method = "tick", at = @At("HEAD"))
    private void serverTick(CallbackInfo info) {
        if (!((Player) (Object) this).level().isClientSide()) {
            Remorphed.sync((ServerPlayer) (Object) this);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readNbt(ValueInput in, CallbackInfo ci) {
        remorphed$readData(in.read(Remorphed.MODID, CompoundTag.CODEC).orElse(new CompoundTag()));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeNbt(ValueOutput out, CallbackInfo ci) {
        out.store(Remorphed.MODID, CompoundTag.CODEC, remorphed$writeData());
    }

    // -------------------------------------------------------------------------
    // Serialization — variant field dropped, only entity type id stored
    // -------------------------------------------------------------------------

    @Unique
    private @NotNull CompoundTag remorphed$writeData() {
        CompoundTag tag = new CompoundTag();

        ListTag unlockedShapes = new ListTag();
        remorphed$unlockedShapes.forEach((type, kills) -> {
            if (kills > 0) {
                CompoundTag e = new CompoundTag();
                e.putString("id", EntityType.getKey(type).toString());
                e.putInt("killAmount", kills);
                unlockedShapes.add(e);
            }
        });
        if (!unlockedShapes.isEmpty()) tag.put(UNLOCKED_SHAPES, unlockedShapes);

        ListTag favoriteShapes = new ListTag();
        remorphed$favoriteShapes.forEach(shape -> {
            if (shape != null) {
                CompoundTag e = new CompoundTag();
                e.putString("id", EntityType.getKey(shape).toString());
                favoriteShapes.add(e);
            }
        });
        if (!favoriteShapes.isEmpty()) tag.put(FAVORITE_SHAPES, favoriteShapes);

        ListTag unlockedSkins = new ListTag();
        remorphed$unlockedSkins.forEach((skinId, kills) -> {
            if (kills > 0) {
                CompoundTag e = new CompoundTag();
                e.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                e.putInt("killAmount", kills);
                unlockedSkins.add(e);
            }
        });
        if (!unlockedSkins.isEmpty()) tag.put(UNLOCKED_SKINS, unlockedSkins);

        ListTag favoriteSkins = new ListTag();
        remorphed$favoriteSkins.forEach(skinId -> {
            if (skinId != null) {
                CompoundTag e = new CompoundTag();
                e.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                favoriteSkins.add(e);
            }
        });
        if (!favoriteSkins.isEmpty()) tag.put(FAVORITE_SKINS, favoriteSkins);

        ListTag morphCounter = new ListTag();
        remorphed$shapeMorphCounter.forEach((type, count) -> {
            if (count > 0) {
                CompoundTag e = new CompoundTag();
                e.putBoolean("isSkin", false);
                e.putString("id", EntityType.getKey(type).toString());
                e.putInt("counter", count);
                morphCounter.add(e);
            }
        });
        remorphed$skinMorphCounter.forEach((skinId, count) -> {
            if (count > 0) {
                CompoundTag e = new CompoundTag();
                e.putBoolean("isSkin", true);
                e.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                e.putInt("counter", count);
                morphCounter.add(e);
            }
        });
        if (!morphCounter.isEmpty()) tag.put(MORPH_COUNTER, morphCounter);

        ListTag defaultVariantsList = new ListTag();
        remorphed$defaultVariants.forEach((type, shape) -> {
            if (shape != null && !Objects.equals(ShapeType.from((EntityType<? extends LivingEntity>) type), shape)) {
                CompoundTag e = new CompoundTag();
                e.putString("entity_id", EntityType.getKey(type).toString());
                e.putInt("variant", shape.getVariantData());
                defaultVariantsList.add(e);
            }
        });
        if (!defaultVariantsList.isEmpty()) tag.put(DEFAULT_VARIANTS, defaultVariantsList);

        return tag;
    }

    @SuppressWarnings("unchecked")
    @Unique
    public void remorphed$readData(@NotNull CompoundTag tag) {
        remorphed$unlockedShapes.clear();
        remorphed$favoriteShapes.clear();
        remorphed$unlockedSkins.clear();
        remorphed$favoriteSkins.clear();
        remorphed$shapeMorphCounter.clear();
        remorphed$skinMorphCounter.clear();

        tag.getListOrEmpty(UNLOCKED_SHAPES).forEach(entry -> {
            if (entry instanceof CompoundTag e) {
                Identifier typeId = Identifier.parse(e.getString("id").orElseThrow());
                int kills = e.getIntOr("killAmount", 0);
                BuiltInRegistries.ENTITY_TYPE.get(typeId)
                        .map(Holder::value)
                        .ifPresent(type -> remorphed$unlockedShapes.put(
                                type, kills));
            }
        });

        tag.getListOrEmpty(FAVORITE_SHAPES).forEach(entry -> {
            if (entry instanceof CompoundTag e) {
                Identifier typeId = Identifier.parse(e.getString("id").orElseThrow());
                int variant = e.getIntOr("variant", -1);
                BuiltInRegistries.ENTITY_TYPE.get(typeId)
                        .map(Holder::value)
                        .ifPresent(type -> remorphed$favoriteShapes.add(type));
            }
        });

        tag.getListOrEmpty(UNLOCKED_SKINS).forEach(entry -> {
            if (entry instanceof CompoundTag e) {
                UUID skinId = UUIDUtil.uuidFromIntArray(e.getIntArray("uuid").orElseThrow());
                int kills   = e.getIntOr("killAmount", 0);
                remorphed$unlockedSkins.put(skinId, kills);
            }
        });

        tag.getListOrEmpty(FAVORITE_SKINS).forEach(entry -> {
            if (entry instanceof CompoundTag e) {
                remorphed$favoriteSkins.add(
                        UUIDUtil.uuidFromIntArray(e.getIntArray("uuid").orElseThrow()));
            }
        });

        tag.getListOrEmpty(MORPH_COUNTER).forEach(entry -> {
            if (entry instanceof CompoundTag e) {
                boolean isSkin = e.getBoolean("isSkin").orElseThrow();
                int count = e.getIntOr("counter", 0);
                if (isSkin) {
                    remorphed$skinMorphCounter.put(
                            UUIDUtil.uuidFromIntArray(e.getIntArray("uuid").orElseThrow()), count);
                } else {
                    Identifier typeId = Identifier.parse(e.getString("id").orElseThrow());
                    BuiltInRegistries.ENTITY_TYPE.get(typeId)
                            .map(Holder::value)
                            .ifPresent(type -> remorphed$shapeMorphCounter.put(
                                    type, count));
                }
            }
        });

        remorphed$defaultVariants.clear();
        tag.getListOrEmpty(DEFAULT_VARIANTS).forEach(entry -> {
            if (entry instanceof CompoundTag e) {
                Identifier entityId = Identifier.parse(e.getString("entity_id").orElseThrow());
                Optional<Holder.Reference<@NotNull EntityType<?>>> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                Optional<Integer> variant = e.getInt("variant");
                if (type.isPresent() && variant.isPresent()) {
                    EntityType<?> eType = type.get().value();
                    ShapeType<?> shape = ShapeType.from((EntityType<? extends LivingEntity>) eType, variant.get());
                    if (shape != null) {
                        remorphed$getDefaultVariants().put(eType, shape);
                    }
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // RemorphedPlayerDataProvider implementation
    // -------------------------------------------------------------------------

    @Unique @Override
    public Map<EntityType<?>, Integer> remorphed$getUnlockedShapes() {
        return remorphed$unlockedShapes;
    }

    @Unique @Override
    public void remorphed$addKill(EntityType<?> type) {
        remorphed$unlockedShapes.merge(type, 1, Integer::sum);
    }

    @Unique @Override
    public int remorphed$getKills(EntityType<?> type) {
        return remorphed$unlockedShapes.getOrDefault(type, 0);
    }

    @Unique @Override
    public Set<EntityType<?>> remorphed$getFavoriteShapes() {
        return remorphed$favoriteShapes;
    }

    @Unique @Override
    public Map<UUID, Integer> remorphed$getUnlockedSkins() {
        return remorphed$unlockedSkins;
    }

    @Unique @Override
    public void remorphed$addKill(UUID skinId) {
        remorphed$unlockedSkins.merge(skinId, 1, Integer::sum);
    }

    @Unique @Override
    public int remorphed$getKills(UUID skinId) {
        return remorphed$unlockedSkins.getOrDefault(skinId, 0);
    }

    @Unique @Override
    public Set<UUID> remorphed$getFavoriteSkins() {
        return remorphed$favoriteSkins;
    }

    @Unique @Override
    public int remorphed$getCounter(EntityType<?> type) {
        return remorphed$shapeMorphCounter.getOrDefault(type, 0);
    }

    @Unique @Override
    public int remorphed$getCounter(UUID skinId) {
        return remorphed$skinMorphCounter.getOrDefault(skinId, 0);
    }

    @Unique @Override
    public Map<EntityType<?>, Integer> remorphed$getShapeCounter() {
        return remorphed$shapeMorphCounter;
    }

    @Unique @Override
    public Map<UUID, Integer> remorphed$getSkinCounter() {
        return remorphed$skinMorphCounter;
    }

    // -------------------------------------------------------------------------
    // Swap handling — trivially simple now, no variant logic anywhere
    // -------------------------------------------------------------------------

    @Unique @Override
    public void remorphed$handleSwap(EntityType<?> to) {
        if (isCreative()) return;

        Player self = (Player) (Object) this;

        int counter   = remorphed$shapeMorphCounter.getOrDefault(to, 0) + 1;
        int killValue = Remorphed.getKillValue(to);

        if (killValue > 0 && counter >= killValue) {
            // Deduct one kill set from the outgoing type
            int k = remorphed$unlockedShapes.getOrDefault(to, 0);
            if (k <= Remorphed.CONFIG.killToUnlock) {
                remorphed$unlockedShapes.remove(to);
            } else {
                remorphed$unlockedShapes.put(to, k - Remorphed.CONFIG.killToUnlock);
            }
            remorphed$shapeMorphCounter.remove(to);

            if ((Object) this instanceof ServerPlayer sp && !Remorphed.canUseShape(sp, to)) {
                PlayerShapeChanger.change2ndShape(sp, null);
            }
        } else {
            remorphed$shapeMorphCounter.put(to, counter);
        }
    }

    @Unique @Override
    public void remorphed$handleSwap(UUID skinId) {
        if (isCreative()) return;

        int counter   = remorphed$skinMorphCounter.getOrDefault(skinId, 0) + 1;
        int killValue = Remorphed.CONFIG.playerKillValue;

        if (killValue > 0 && counter >= killValue) {
            remorphed$skinMorphCounter.remove(skinId);
            int k = remorphed$getKills(skinId) - Remorphed.CONFIG.killToUnlockPlayers;
            if (k <= 0) {
                remorphed$unlockedSkins.remove(skinId);
            } else {
                remorphed$unlockedSkins.put(skinId, k);
            }
        } else {
            remorphed$skinMorphCounter.put(skinId, counter);
        }
    }

    @Unique @Override
    public Map<EntityType<?>, ShapeType<?>> remorphed$getDefaultVariants() {
        return remorphed$defaultVariants;
    }

    @Unique @Override
    public void remorphed$setDefaultVariant(EntityType<?> entityType, ShapeType<?> shapeType) {
        if (shapeType == null) {
            remorphed$defaultVariants.remove(entityType);
        } else {
            remorphed$defaultVariants.put(entityType, shapeType);
        }
    }
}