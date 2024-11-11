package tocraft.remorphed.mixin;

import net.minecraft.core.Holder;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.variant.ShapeType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({"DataFlowIssue", "resource", "ControlFlowStatementWithoutBraces", "unused"})
@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RemorphedPlayerDataProvider {
    @Unique
    private final Map<ShapeType<? extends LivingEntity>, Integer> remorphed$unlockedShapes = new HashMap<>();
    @Unique
    private final Set<ShapeType<?>> remorphed$favoriteShapes = new HashSet<>();
    @Unique
    private final Map<UUID, Integer> remorphed$unlockedSkins = new ConcurrentHashMap<>();
    @Unique
    private final Set<UUID> remorphed$favoriteSkins = new CopyOnWriteArraySet<>();
    @Unique
    private final String UNLOCKED_SHAPES = "UnlockedShapes";
    @Unique
    private final String FAVORITE_SHAPES = "FavoriteShapes";
    @Unique
    private final String UNLOCKED_SKINS = "UnlockedSkins";
    @Unique
    private final String FAVORITE_SKINS = "FavoriteSkins";

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
    private void readNbt(CompoundTag tag, CallbackInfo info) {
        remorphed$readData(tag.getCompound(Remorphed.MODID));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeNbt(CompoundTag tag, CallbackInfo info) {
        tag.put(Remorphed.MODID, remorphed$writeData());
    }

    @Unique
    private CompoundTag remorphed$writeData() {
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
                entryTag.putUUID("uuid", skinId);
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
                entryTag.putUUID("uuid", skinId);
                favoriteSkins.add(entryTag);
            }
        });
        if (!favoriteSkins.isEmpty()) {
            tag.put(FAVORITE_SKINS, favoriteSkins);
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

        ListTag unlockedShapes = tag.getList(UNLOCKED_SHAPES, ListTag.TAG_COMPOUND);
        unlockedShapes.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                ResourceLocation typeId = ResourceLocation.parse(((CompoundTag) entry).getString("id"));
                int typeVariantId = ((CompoundTag) entry).getInt("variant");
                int killAmount = ((CompoundTag) entry).getInt("killAmount");

                remorphed$unlockedShapes.put(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), typeVariantId), killAmount);
            }
        });
        ListTag favoriteShapes = tag.getList(FAVORITE_SHAPES, ListTag.TAG_COMPOUND);
        favoriteShapes.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                ResourceLocation typeId = ResourceLocation.parse(((CompoundTag) entry).getString("id"));
                int typeVariantId = ((CompoundTag) entry).getInt("variant");

                remorphed$favoriteShapes.add(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId).map(Holder::value).orElse(null), typeVariantId));
            }
        });

        ListTag unlockedSkins = tag.getList(UNLOCKED_SKINS, ListTag.TAG_COMPOUND);
        unlockedSkins.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                UUID skinId = ((CompoundTag) entry).getUUID("uuid");
                int killAmount = ((CompoundTag) entry).getInt("killAmount");
                remorphed$unlockedSkins.put(skinId, killAmount);
            }
        });
        ListTag favoriteSkins = tag.getList(FAVORITE_SKINS, ListTag.TAG_COMPOUND);
        favoriteSkins.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                UUID skinId = ((CompoundTag) entry).getUUID("uuid");

                remorphed$favoriteSkins.add(skinId);
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
            for (Integer i : remorphed$unlockedShapes.entrySet().stream().filter(entry -> entry.getKey().getEntityType().equals(type.getEntityType())).map(Map.Entry::getValue).toList()) {
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
}
