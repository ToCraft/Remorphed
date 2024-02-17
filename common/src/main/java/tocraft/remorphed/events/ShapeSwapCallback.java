package tocraft.remorphed.events;

import dev.architectury.event.EventResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import org.jetbrains.annotations.Nullable;
import tocraft.remorphed.Remorphed;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class ShapeSwapCallback implements ShapeEvents.ShapeSwapCallback {
    @Override
    public EventResult swap(ServerPlayer player, @Nullable LivingEntity to) {
        // check if entity is unlocked by remorphed, prevents native unlocks by walkers
        if (!Remorphed.canUseShape(player, ShapeType.from(to))) {
            // handle special shape
            CompoundTag nbt = new CompoundTag();
            if (Walkers.hasSpecialShape(player.getUUID()) && to instanceof Wolf) {
                to.saveWithoutId(nbt);
                if (nbt.contains("isSpecial") && nbt.getBoolean("isSpecial")) {
                    return EventResult.pass();
                }
            } else return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }
}
