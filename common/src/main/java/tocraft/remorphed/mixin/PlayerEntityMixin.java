package tocraft.remorphed.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({"DataFlowIssue", "resource", "ControlFlowStatementWithoutBraces", "unused"})
@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RemorphedPlayerDataProvider {
    @Shadow
    public abstract boolean isCreative();

    @Unique
    private final Map<ShapeType<? extends LivingEntity>, Integer> remorphed$unlockedShapes = new HashMap<>();
    @Unique
    private final Set<ShapeType<?>> remorphed$favoriteShapes = new HashSet<>();
    @Unique
    private final Map<UUID, Integer> remorphed$unlockedSkins = new ConcurrentHashMap<>();
    @Unique
    private final Set<UUID> remorphed$favoriteSkins = new CopyOnWriteArraySet<>();
    @Unique
    private final Map<ShapeType<?>, Integer> remorphed$ShapeMorphCounter = new ConcurrentHashMap<>();
    @Unique
    private final Map<UUID, Integer> remorphed$SkinMorphCounter = new ConcurrentHashMap<>();
    @Unique
    private static final String UNLOCKED_SHAPES = "UnlockedShapes";
    @Unique
    private static final String FAVORITE_SHAPES = "FavoriteShapes";
    @Unique
    private static final String UNLOCKED_SKINS = "UnlockedSkins";
    @Unique
    private static final String FAVORITE_SKINS = "FavoriteSkins";
    @Unique
    private static final String MORPH_COUNTER = "MorphCounter";

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void serverTick(CallbackInfo info) {
        if (!this.level().isClientSide) {
            Remorphed.sync((ServerPlayer) (Object) this);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readNbt(@NotNull CompoundTag tag, CallbackInfo info) {
        remorphed$readData(tag.getCompoundOrEmpty(Remorphed.MODID));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeNbt(@NotNull CompoundTag tag, CallbackInfo info) {
        tag.put(Remorphed.MODID, remorphed$writeData());
    }

    @Unique
    private @NotNull CompoundTag remorphed$writeData() {
        CompoundTag tag = new CompoundTag();
        ListTag unlockedShapes = new ListTag();
        remorphed$unlockedShapes.forEach((shape, killAmount) -> {
            if (killAmount > 0 && shape != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("id", EntityType.getKey(shape.getEntityType()).toString());
                entryTag.putInt("variant", shape.getVariantData());
                entryTag.putInt("killAmount", killAmount);
                unlockedShapes.add(entryTag);
            }
        });
        if (!unlockedShapes.isEmpty()) {
            tag.put(UNLOCKED_SHAPES, unlockedShapes);
        }

        ListTag favoriteShapes = new ListTag();
        remorphed$favoriteShapes.forEach(shape -> {
            if (shape != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("id", EntityType.getKey(shape.getEntityType()).toString());
                entryTag.putInt("variant", shape.getVariantData());
                favoriteShapes.add(entryTag);
            }
        });
        if (!favoriteShapes.isEmpty()) {
            tag.put(FAVORITE_SHAPES, favoriteShapes);
        }

        ListTag unlockedSkins = new ListTag();
        remorphed$unlockedSkins.forEach((skinId, killAmount) -> {
            if (killAmount > 0 && skinId != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                entryTag.putInt("killAmount", killAmount);
                unlockedSkins.add(entryTag);
            }
        });
        if (!unlockedSkins.isEmpty()) {
            tag.put(UNLOCKED_SKINS, unlockedSkins);
        }

        ListTag favoriteSkins = new ListTag();
        remorphed$favoriteSkins.forEach(skinId -> {
            if (skinId != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                favoriteSkins.add(entryTag);
            }
        });
        if (!favoriteSkins.isEmpty()) {
            tag.put(FAVORITE_SKINS, favoriteSkins);
        }

        ListTag morphCounter = new ListTag();
        remorphed$ShapeMorphCounter.forEach((type, count) -> {
            if (count > 0 && type != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putBoolean("isSkin", false);
                entryTag.putString("id", EntityType.getKey(type.getEntityType()).toString());
                entryTag.putInt("variant", type.getVariantData());
                entryTag.putInt("counter", count);
                morphCounter.add(entryTag);
            }
        });
        remorphed$SkinMorphCounter.forEach((skinId, count) -> {
            if (count > 0 && skinId != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putBoolean("isSkin", true);
                entryTag.putIntArray("uuid", UUIDUtil.uuidToIntArray(skinId));
                entryTag.putInt("counter", count);
                morphCounter.add(entryTag);
            }
        });
        if (!morphCounter.isEmpty()) {
            tag.put(MORPH_COUNTER, morphCounter);
        }

        return tag;
    }

    @SuppressWarnings("unchecked")
    @Unique
    public void remorphed$readData(@NotNull CompoundTag tag) {
        remorphed$unlockedShapes.clear();
        remorphed$favoriteShapes.clear();
        remorphed$unlockedSkins.clear();
        remorphed$favoriteSkins.clear();
        remorphed$SkinMorphCounter.clear();
        remorphed$ShapeMorphCounter.clear();

        ListTag unlockedShapes = tag.getListOrEmpty(UNLOCKED_SHAPES);
        unlockedShapes.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                ResourceLocation typeId = ResourceLocation.parse(((CompoundTag) entry).getString("id").orElseThrow());
                int typeVariantId = ((CompoundTag) entry).getIntOr("variant", -1);
                int killAmount = ((CompoundTag) entry).getIntOr("killAmount", 0);

                remorphed$unlockedShapes.put(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), typeVariantId), killAmount);
            }
        });
        ListTag favoriteShapes = tag.getListOrEmpty(FAVORITE_SHAPES);
        favoriteShapes.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                ResourceLocation typeId = ResourceLocation.parse(((CompoundTag) entry).getString("id").orElseThrow());
                int typeVariantId = ((CompoundTag) entry).getIntOr("variant", -1);

                remorphed$favoriteShapes.add(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), typeVariantId));
            }
        });

        ListTag unlockedSkins = tag.getListOrEmpty(UNLOCKED_SKINS);
        unlockedSkins.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                UUID skinId = UUIDUtil.uuidFromIntArray(((CompoundTag) entry).getIntArray("uuid").orElseThrow());
                int killAmount = ((CompoundTag) entry).getIntOr("killAmount", 0);
                remorphed$unlockedSkins.put(skinId, killAmount);
            }
        });
        ListTag favoriteSkins = tag.getListOrEmpty(FAVORITE_SKINS);
        favoriteSkins.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                UUID skinId = UUIDUtil.uuidFromIntArray(((CompoundTag) entry).getIntArray("uuid").orElseThrow());

                remorphed$favoriteSkins.add(skinId);
            }
        });

        ListTag morphCounter = tag.getListOrEmpty(MORPH_COUNTER);
        morphCounter.forEach(entry -> {
            boolean isSkin = ((CompoundTag) entry).getBoolean("isSkin").orElseThrow();
            int count = ((CompoundTag) entry).getIntOr("counter", 0);
            if (isSkin) {
                UUID skinId = UUIDUtil.uuidFromIntArray(((CompoundTag) entry).getIntArray("uuid").orElseThrow());
                remorphed$SkinMorphCounter.put(skinId, count);
            } else {
                ResourceLocation typeId = ResourceLocation.parse(((CompoundTag) entry).getString("id").orElseThrow());
                int typeVariantId = ((CompoundTag) entry).getIntOr("variant", -1);
                remorphed$ShapeMorphCounter.put(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), typeVariantId), count);
            }
        });
    }

    @Unique
    @Override
    public Map<ShapeType<? extends LivingEntity>, Integer> remorphed$getUnlockedShapes() {
        return remorphed$unlockedShapes;
    }

    @Unique
    @Override
    public void remorphed$addKill(ShapeType<? extends LivingEntity> type) {
        remorphed$unlockedShapes.put(type, remorphed$getKills(type) + 1);
    }

    @Unique
    @Override
    public int remorphed$getKills(ShapeType<? extends LivingEntity> type) {
        if (Walkers.CONFIG.unlockEveryVariant) {
            int killAmount = 0;
            for (int i : remorphed$unlockedShapes.entrySet().stream().filter(entry -> entry.getKey().getEntityType().equals(type.getEntityType())).map(Map.Entry::getValue).toList()) {
                killAmount += i;
            }
            return killAmount;
        } else {
            return remorphed$unlockedShapes.getOrDefault(type, 0);
        }
    }

    @Unique
    @Override
    public Set<ShapeType<?>> remorphed$getFavoriteShapes() {
        return remorphed$favoriteShapes;
    }

    @Unique
    @Override
    public Map<UUID, Integer> remorphed$getUnlockedSkins() {
        return remorphed$unlockedSkins;
    }

    @Unique
    @Override
    public void remorphed$addKill(UUID skinId) {
        remorphed$unlockedSkins.put(skinId, remorphed$getKills(skinId) + 1);
    }

    @Unique
    @Override
    public int remorphed$getKills(UUID skinId) {
        return remorphed$unlockedSkins.getOrDefault(skinId, 0);
    }

    @Unique
    @Override
    public Set<UUID> remorphed$getFavoriteSkins() {
        return remorphed$favoriteSkins;
    }

    @Unique
    @Override
    public int remorphed$getCounter(ShapeType<? extends LivingEntity> type) {
        if (Walkers.CONFIG.unlockEveryVariant) {
            int counter = 0;
            for (int i : remorphed$ShapeMorphCounter.entrySet().stream().filter(entry -> entry.getKey().getEntityType().equals(type.getEntityType())).map(Map.Entry::getValue).toList()) {
                counter += i;
            }
            return counter;
        } else {
            return remorphed$ShapeMorphCounter.getOrDefault(type, 0);
        }
    }

    @Unique
    @Override
    public int remorphed$getCounter(UUID skinId) {
        return remorphed$SkinMorphCounter.getOrDefault(skinId, 0);
    }

    @Unique
    @Override
    public void remorphed$handleSwap(ShapeType<? extends LivingEntity> type) {
        if (((Player) (Object) this).isCreative()) {
            return;
        }

        int counter = remorphed$getCounter(type) + 1;
        int killValue = Remorphed.getKillValue(type.getEntityType());

        if (killValue > 0 && counter >= killValue) {
            // get current kill amount
            int k = remorphed$unlockedShapes.getOrDefault(type, 0);
            ShapeType<?> killType = type;

            // check the kill amount of other variants if current one is zero
            if (Walkers.CONFIG.unlockEveryVariant) {
                List<? extends ShapeType<?>> variants = ShapeType.getAllTypes(type.getEntityType(), level());
                for (int i = 0; k <= 0 && i < variants.size(); i++) {
                    killType = variants.get(i);
                    k = remorphed$unlockedShapes.getOrDefault(killType, 0);
                }
            }

            // remove one kill
            if (k <= 1) {
                int k2 = remorphed$unlockedShapes.remove(killType);
            } else {
                remorphed$unlockedShapes.put(killType, k - 1);
            }

            // reset counter
            if (Walkers.CONFIG.unlockEveryVariant) {
                ShapeType<? extends LivingEntity> ctype;
                List<? extends ShapeType<?>> variants = ShapeType.getAllTypes(type.getEntityType(), level());
                for (int i = 0; counter > 0 && i < variants.size(); i++) {
                    Integer c = remorphed$ShapeMorphCounter.remove(variants.get(i));
                    if (c != null) {
                        counter -= c;
                    }
                }
            } else {
                remorphed$ShapeMorphCounter.remove(type);
            }

            // check and remove 2nd Shape if necessary
            //noinspection ConstantValue
            if ((Object) this instanceof ServerPlayer serverPlayer && !Remorphed.canUseShape(serverPlayer, type)) {
                PlayerShapeChanger.change2ndShape(serverPlayer, null);
            }
        } else {
            // raise counter
            remorphed$ShapeMorphCounter.put(type, remorphed$ShapeMorphCounter.getOrDefault(type, 0) + 1);
        }
    }

    @Unique
    @Override
    public void remorphed$handleSwap(UUID skinId) {
        if (((Player) (Object) this).isCreative()) {
            return;
        }

        int counter = remorphed$SkinMorphCounter.getOrDefault(skinId, 0) + 1;
        counter++;
        int killValue = Remorphed.CONFIG.playerKillValue;
        if (killValue > 0 && counter >= killValue) {
            // reset counter
            remorphed$SkinMorphCounter.remove(skinId);
            // remove one kill
            int k = remorphed$getKills(skinId) - 1;
            if (k <= 0) {
                remorphed$unlockedSkins.remove(skinId);
            } else {
                remorphed$unlockedSkins.put(skinId, k);
            }
        } else {
            remorphed$SkinMorphCounter.put(skinId, counter);
        }
    }

    @Unique
    @Override
    public Map<ShapeType<?>, Integer> remorphed$getShapeCounter() {
        return remorphed$ShapeMorphCounter;
    }

    @Unique
    @Override
    public Map<UUID, Integer> remorphed$getSkinCounter() {
        return remorphed$SkinMorphCounter;
    }
}
