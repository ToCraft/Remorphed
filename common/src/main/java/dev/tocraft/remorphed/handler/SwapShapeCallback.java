package dev.tocraft.remorphed.handler;

import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.walkers.api.PlayerShape;
import dev.tocraft.walkers.api.events.ShapeEvents;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SwapShapeCallback implements ShapeEvents.ShapeSwapCallback {
    @Override
    public InteractionResult swap(ServerPlayer player, @Nullable LivingEntity to) {
        if (to != null) {
            ShapeType<?> toType = ShapeType.from(to);
            LivingEntity previous = PlayerShape.getCurrentShape(player);
            if (previous == null || !Objects.equals(previous.getType(), to.getType())) {
                PlayerMorph.handleSwap(player, toType);
            } else {
                PlayerMorph.setDefaultVariant(player, (EntityType<? extends LivingEntity>) previous.getType(), toType);
            }
        }
        return InteractionResult.PASS;
    }
}
