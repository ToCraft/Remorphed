package tocraft.remorphed.mixin;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.variant.ShapeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"DataFlowIssue", "resource", "ControlFlowStatementWithoutBraces"})
@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RemorphedPlayerDataProvider {
    @Unique
    private Map<ShapeType<? extends LivingEntity>, Integer> remorphed$unlockedShapes = new HashMap<>();
    @Unique
    private final Set<ShapeType<?>> remorphed$favoriteShapes = new HashSet<>();
    @Unique
    private final String UNLOCKED_SHAPES = "UnlockedShapes";
    @Unique
    private final String FAVORITE_SHAPES = "FavoriteShapes";

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void serverTick(CallbackInfo info) {
        if (!level.isClientSide)
            Remorphed.sync((ServerPlayer) (Object) this);
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
        ListTag unlockedList = new ListTag();
        remorphed$unlockedShapes.forEach((shape, killAmount) -> {
            if (killAmount > 0 && shape != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("id", Registry.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
                entryTag.putInt("variant", shape.getVariantData());
                entryTag.putInt("killAmount", killAmount);
                unlockedList.add(entryTag);
            }
        });
        if (!remorphed$unlockedShapes.isEmpty())
            tag.put(UNLOCKED_SHAPES, unlockedList);

        ListTag favoritesList = new ListTag();
        remorphed$favoriteShapes.forEach(shape -> {
            if (shape != null) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("id", Registry.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
                entryTag.putInt("variant", shape.getVariantData());
                favoritesList.add(entryTag);
            }
        });
        if (!remorphed$favoriteShapes.isEmpty())
            tag.put(FAVORITE_SHAPES, favoritesList);

        return tag;
    }

    @SuppressWarnings("unchecked")
    @Unique
    public void remorphed$readData(CompoundTag tag) {
        remorphed$unlockedShapes.clear();
        remorphed$favoriteShapes.clear();

        ListTag unlockedList = tag.getList(UNLOCKED_SHAPES, ListTag.TAG_COMPOUND);
        unlockedList.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                ResourceLocation typeId = new ResourceLocation(((CompoundTag) entry).getString("id"));
                int typeVariantId = ((CompoundTag) entry).getInt("variant");
                int killAmount = ((CompoundTag) entry).getInt("killAmount");

                remorphed$unlockedShapes.put(ShapeType.from((EntityType<? extends LivingEntity>) Registry.ENTITY_TYPE.get(typeId), typeVariantId), killAmount);
            }
        });
        ListTag favoritesList = tag.getList(FAVORITE_SHAPES, ListTag.TAG_COMPOUND);
        favoritesList.forEach(entry -> {
            if (entry instanceof CompoundTag) {
                ResourceLocation typeId = new ResourceLocation(((CompoundTag) entry).getString("id"));
                int typeVariantId = ((CompoundTag) entry).getInt("variant");

                remorphed$favoriteShapes.add(ShapeType.from((EntityType<? extends LivingEntity>) Registry.ENTITY_TYPE.get(typeId), typeVariantId));
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
    public void remorphed$setUnlockedShapes(Map<ShapeType<? extends LivingEntity>, Integer> types) {
        remorphed$unlockedShapes = types;
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
    public Set<ShapeType<?>> remorphed$getFavorites() {
        return remorphed$favoriteShapes;
    }
}
