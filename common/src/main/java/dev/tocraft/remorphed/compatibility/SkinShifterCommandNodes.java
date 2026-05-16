package dev.tocraft.remorphed.compatibility;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.remorphed.permission.PermissionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Optional;
import java.util.UUID;

/**
 * Builds and registers all SkinShifter-specific command nodes onto the root
 * {@code /remorphed} node. Only called when SkinShifter is present.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class SkinShifterCommandNodes {

    private SkinShifterCommandNodes() {}

    public static void registerAll(LiteralCommandNode<CommandSourceStack> root) {
        root.addChild(buildRemoveSkin());
        root.addChild(buildAddSkin());
        root.addChild(buildClearSkins());
        root.addChild(buildHasSkin());
    }

    // -------------------------------------------------------------------------
    // Node builders
    // -------------------------------------------------------------------------

    private static LiteralCommandNode<CommandSourceStack> buildRemoveSkin() {
        return Commands.literal("removeSkin")
                .requires(source -> requiresSkinPermission(source, "removeSkin"))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("playerUUID", UuidArgument.uuid())
                                .executes(ctx -> {
                                    UUID uuid = UuidArgument.getUuid(ctx, "playerUUID");
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    resolveById(ctx.getSource(), player, uuid,
                                            profile -> removeSkin(ctx.getSource(), player, profile));
                                    return 1;
                                }))
                        .then(Commands.argument("playerName", MessageArgument.message())
                                .executes(ctx -> {
                                    String name = MessageArgument.getMessage(ctx, "playerName").getString();
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    resolveByName(ctx.getSource(), player, name,
                                            profile -> removeSkin(ctx.getSource(), player, profile));
                                    return 1;
                                })))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildAddSkin() {
        return Commands.literal("addSkin")
                .requires(source -> requiresSkinPermission(source, "addSkin"))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("playerUUID", UuidArgument.uuid())
                                .executes(ctx -> {
                                    UUID uuid = UuidArgument.getUuid(ctx, "playerUUID");
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    resolveById(ctx.getSource(), player, uuid,
                                            profile -> addSkin(ctx.getSource(), player, profile));
                                    return 1;
                                }))
                        .then(Commands.argument("playerName", MessageArgument.message())
                                .executes(ctx -> {
                                    String name = MessageArgument.getMessage(ctx, "playerName").getString();
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    resolveByName(ctx.getSource(), player, name,
                                            profile -> addSkin(ctx.getSource(), player, profile));
                                    return 1;
                                })))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildClearSkins() {
        return Commands.literal("clearSkins")
                .requires(source -> requiresSkinPermission(source, "clearSkins"))
                .then(Commands.argument("player", EntityArgument.players())
                        .executes(ctx -> {
                            clearSkins(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"));
                            return 1;
                        }))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildHasSkin() {
        return Commands.literal("hasSkin")
                .requires(source -> requiresSkinPermission(source, "hasSkin"))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("playerUUID", UuidArgument.uuid())
                                .executes(ctx -> {
                                    UUID uuid = UuidArgument.getUuid(ctx, "playerUUID");
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    resolveById(ctx.getSource(), player, uuid,
                                            profile -> hasSkin(ctx.getSource(), player, profile));
                                    return 1;
                                }))
                        .then(Commands.argument("playerName", MessageArgument.message())
                                .executes(ctx -> {
                                    String name = MessageArgument.getMessage(ctx, "playerName").getString();
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    resolveByName(ctx.getSource(), player, name,
                                            profile -> hasSkin(ctx.getSource(), player, profile));
                                    return 1;
                                })))
                .build();
    }

    // -------------------------------------------------------------------------
    // Implementations
    // -------------------------------------------------------------------------

    private static void hasSkin(CommandSourceStack source, ServerPlayer player, GameProfile profile) {
        if (!checkTargetPermission(source, player, "hasSkin")) return;
        if (Remorphed.getUnlockedSkins(player).contains(profile)) {
            source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".hasSkin_success",
                    player.getName(), profile.name()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".hasSkin_fail",
                    player.getName(), profile.name()), true);
        }
    }

    private static void removeSkin(CommandSourceStack source, ServerPlayer player, GameProfile profile) {
        if (!checkTargetPermission(source, player, "removeSkin")) return;
        PlayerMorph.getUnlockedSkinIds(player).remove(profile.id());
        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".removeSkin",
                profile.name(), player.getName()), true);
    }

    private static void addSkin(CommandSourceStack source, ServerPlayer player, GameProfile profile) {
        if (!checkTargetPermission(source, player, "addSkin")) return;
        PlayerMorph.getUnlockedSkinIds(player).put(profile.id(), Remorphed.CONFIG.killToUnlockPlayers);
        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".addSkin",
                player.getName(), profile.name()), true);
    }

    private static void clearSkins(CommandSourceStack source, ServerPlayer player) {
        if (!checkTargetPermission(source, player, "clearSkins")) return;
        PlayerMorph.getUnlockedSkinIds(player).clear();
        PlayerMorph.getSkinCounter(player).clear();
        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".clearSkins",
                player.getName()), true);
        SkinShifterCompat.clearSkin(player);
    }

    // -------------------------------------------------------------------------
    // Permission helpers
    // -------------------------------------------------------------------------

    /**
     * Gate applied on the command node itself (before arguments are parsed).
     * Skin commands always require at least op level 2 when permissions are off.
     */
    private static boolean requiresSkinPermission(CommandSourceStack source, String command) {
        if (source.getEntity() == null) {
            return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        }
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!Remorphed.CONFIG.usePermissions) {
                return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
            }
            return PermissionManager.canUseCommandOnSelf(player, command)
                    || PermissionManager.canUseCommandOnOthers(player, command);
        }
        return false;
    }

    /**
     * Per-execution check against a specific target player.
     * Returns {@code false} and sends a failure message if the executor lacks permission.
     */
    private static boolean checkTargetPermission(CommandSourceStack source, ServerPlayer target, String command) {
        if (source.getEntity() instanceof ServerPlayer executor && Remorphed.CONFIG.usePermissions) {
            if (!PermissionManager.canUseCommandOnTarget(executor, target, command)) {
                source.sendFailure(Component.translatable("commands.generic.permission"));
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Profile resolution helpers (reduce duplicated Optional-unwrap boilerplate)
    // -------------------------------------------------------------------------

    @FunctionalInterface
    private interface ProfileConsumer {
        void accept(GameProfile profile);
    }

    private static void resolveById(CommandSourceStack source, ServerPlayer player,
                                    UUID uuid, ProfileConsumer action) {
        Optional<GameProfile> profile = source.getServer().services().profileResolver().fetchById(uuid);
        if (profile.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("skinshifter.invalid_player", uuid), true);
        } else {
            action.accept(profile.get());
        }
    }

    private static void resolveByName(CommandSourceStack source, ServerPlayer player,
                                      String name, ProfileConsumer action) {
        Optional<GameProfile> profile = source.getServer().services().profileResolver().fetchByName(name);
        if (profile.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("skinshifter.invalid_player", name), true);
        } else {
            action.accept(profile.get());
        }
    }
}