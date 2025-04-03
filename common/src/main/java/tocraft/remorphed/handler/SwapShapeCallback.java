package tocraft.remorphed.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import tocraft.remorphed.impl.PlayerMorph;
import tocraft.walkers.api.events.ShapeEvents;
import tocraft.walkers.api.variant.ShapeType;

public class SwapShapeCallback implements ShapeEvents.ShapeSwapCallback {
    @Override
    public InteractionResult swap(ServerPlayer player, @Nullable LivingEntity to) {
        if (to instanceof LivingEntity) {
            ShapeType<?> type = ShapeType.from(to);
            boolean bl = PlayerMorph.handleSwap(player, type);
            return bl ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        else {
            return InteractionResult.PASS;
        }
    }
}
