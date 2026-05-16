package dev.tocraft.remorphed.compatibility;

import dev.tocraft.skinshifter.SkinShifter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Single access point for all SkinShifter API calls.
 * <p>
 * This class is <em>only ever referenced</em> from inside an
 * {@code if (Remorphed.foundSkinShifter)} guard, so the JVM will not attempt
 * to load (and fail to resolve) the {@code SkinShifter} class unless the mod
 * is actually present.
 */
public final class SkinShifterCompat {

    public static @Nullable UUID getCurrentSkin(Player player) {
        return SkinShifter.getCurrentSkin(player);
    }

    public static void setSkin(ServerPlayer player, @Nullable UUID skinId) {
        SkinShifter.setSkin(player, skinId);
    }

    public static void clearSkin(ServerPlayer player) {
        SkinShifter.setSkin(player, null);
    }
}