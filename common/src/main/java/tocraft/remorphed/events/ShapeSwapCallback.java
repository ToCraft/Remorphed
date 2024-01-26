package tocraft.remorphed.events;

import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import tocraft.remorphed.Remorphed;
import tocraft.walkers.api.event.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class ShapeSwapCallback implements ShapeEvents.ShapeSwapCallback {
    @Override
    public EventResult swap(ServerPlayer player, @Nullable LivingEntity to) {
        // check if entity is unlocked by remorphed, prevents native unlocks by walkers
        if (!Remorphed.canUseShape(player, ShapeType.from(to)))
            return EventResult.interruptFalse();
        else
            return EventResult.pass();
    }
}
