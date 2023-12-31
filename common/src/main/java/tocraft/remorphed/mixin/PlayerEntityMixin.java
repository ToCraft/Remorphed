package tocraft.remorphed.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
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
import tocraft.walkers.api.variant.ShapeType;

import java.util.HashMap;
import java.util.Map;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RemorphedPlayerDataProvider {
    @Unique
    private Map<ShapeType<? extends LivingEntity>, Integer> unlockedShapes = new HashMap<ShapeType<? extends LivingEntity>, Integer>();
    @Unique
    private final String UNLOCKED_SHAPES = "UnlockedShapes";

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
        readData(tag.getCompound(Remorphed.MODID));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeNbt(CompoundTag tag, CallbackInfo info) {
        tag.put(Remorphed.MODID, writeData(new CompoundTag()));
    }

    @Unique
    private CompoundTag writeData(CompoundTag tag) {
        ListTag list = new ListTag();
        unlockedShapes.forEach((shape, killAmount) -> {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(shape.getEntityType()).toString());
            entryTag.putInt("variant", shape.getVariantData());
            entryTag.putInt("killAmount", killAmount);
            list.add(entryTag);
        });
        if (list != null)
            tag.put(UNLOCKED_SHAPES, list);
        return tag;
    }

    @Unique
    public void readData(CompoundTag tag) {
        unlockedShapes.clear();

        if (tag.get(UNLOCKED_SHAPES) != null) {
            ListTag list = (ListTag) tag.get(UNLOCKED_SHAPES);
            list.forEach(entry -> {
                if (entry instanceof CompoundTag) {
                    ResourceLocation typeId = new ResourceLocation(((CompoundTag) entry).getString("id"));
                    int typeVariantId = ((CompoundTag) entry).getInt("variant");
                    int killAmount = ((CompoundTag) entry).getInt("killAmount");

                    unlockedShapes.put(ShapeType.from((EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(typeId), typeVariantId), killAmount);
                }
            });
        }
    }

    @Unique
    @Override
    public Map<ShapeType<? extends LivingEntity>, Integer> getUnlockedShapes() {
        return unlockedShapes;
    }

    @Unique
    @Override
    public void setUnlockedShapes(Map<ShapeType<? extends LivingEntity>, Integer> types) {
        unlockedShapes = types;
    }

    @Unique
    @Override
    public void addKill(ShapeType<? extends LivingEntity> type) {
        unlockedShapes.put(type, getKills(type) + 1);
    }

    @Unique
    @Override
    public int getKills(ShapeType<? extends LivingEntity> type) {
        return unlockedShapes.containsKey(type) ? unlockedShapes.get(type) : 0;
    }

}
