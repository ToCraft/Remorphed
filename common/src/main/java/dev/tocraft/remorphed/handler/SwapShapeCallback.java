package dev.tocraft.remorphed.handler;

import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.walkers.api.events.ShapeEvents;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SwapShapeCallback implements ShapeEvents.ShapeSwapCallback {
    @Override
    public InteractionResult swap(ServerPlayer player, @Nullable LivingEntity to) {
        if (to instanceof LivingEntity) {
            PlayerMorph.handleSwap(player, ShapeType.from(to));
        }
        return InteractionResult.PASS;
    }
}
