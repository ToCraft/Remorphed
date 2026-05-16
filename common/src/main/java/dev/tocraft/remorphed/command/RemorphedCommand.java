package dev.tocraft.remorphed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tocraft.craftedcore.event.common.CommandEvents;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.compatibility.SkinShifterCommandNodes;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.remorphed.permission.PermissionManager;
import dev.tocraft.walkers.api.PlayerShapeChanger;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class RemorphedCommand implements CommandEvents.CommandRegistration {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher,
                         CommandBuildContext registry,
                         CommandSelection selection) {

        LiteralCommandNode<CommandSourceStack> rootNode =
                Commands.literal(Remorphed.MODID).build();

        rootNode.addChild(ListPermissionsCommand.createNode());
        rootNode.addChild(buildRemoveShape(registry));
        rootNode.addChild(buildAddShape(registry));
        rootNode.addChild(buildClearShapes());
        rootNode.addChild(buildHasShape(registry));

        // Skin commands are isolated in their own class and only registered when
        // SkinShifter is present — no SkinShifter classes are touched otherwise.
        if (Remorphed.foundSkinShifter) {
            SkinShifterCommandNodes.registerAll(rootNode);
        }

        dispatcher.getRoot().addChild(rootNode);
    }

    // -------------------------------------------------------------------------
    // Node builders
    // -------------------------------------------------------------------------

    private static LiteralCommandNode<CommandSourceStack> buildRemoveShape(CommandBuildContext registry) {
        return Commands.literal("removeShape")
                .requires(source -> requiresShapePermission(source, "removeShape"))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                                .executes(ctx -> {
                                    removeShape(ctx.getSource(),
                                            EntityArgument.getPlayer(ctx, "player"),
                                            EntityType.getKey(ResourceArgument.getSummonableEntityType(ctx, "shape").value())
                                    );
                                    return 1;
                                })))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildAddShape(CommandBuildContext registry) {
        return Commands.literal("addShape")
                .requires(source -> requiresShapePermission(source, "addShape"))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                                .executes(ctx -> {
                                    addShape(ctx.getSource(),
                                            EntityArgument.getPlayer(ctx, "player"),
                                            EntityType.getKey(ResourceArgument.getSummonableEntityType(ctx, "shape").value())
                                    );
                                    return 1;
                                })))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildClearShapes() {
        return Commands.literal("clearShapes")
                .requires(source -> requiresShapePermission(source, "clearShapes"))
                .then(Commands.argument("player", EntityArgument.players())
                        .executes(ctx -> {
                            clearShapes(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"));
                            return 1;
                        }))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildHasShape(CommandBuildContext registry) {
        return Commands.literal("hasShape")
                .requires(source -> requiresShapePermission(source, "hasShape"))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                                .executes(ctx -> hasShape(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"),
                                        EntityType.getKey(ResourceArgument.getSummonableEntityType(ctx, "shape").value())
                                ))))
                .build();
    }

    // -------------------------------------------------------------------------
    // Implementations
    // -------------------------------------------------------------------------

    private static int hasShape(@NotNull CommandSourceStack source, ServerPlayer player,
                                Identifier id) {
        if (!checkTargetPermission(source, player, "hasShape")) return 0;
        EntityType<?> type = resolveType(id);
        Component name = Component.translatable(type.getDescriptionId());
        if (Remorphed.getUnlockedShapes(player).contains(type)) {
            source.sendSuccess(() -> Component.translatable(
                    Remorphed.MODID + ".hasShape_success", player.getName(), name), true);
            return 1;
        }
        source.sendSuccess(() -> Component.translatable(
                Remorphed.MODID + ".hasShape_fail", player.getName(), name), true);
        return 0;
    }

    private static void removeShape(@NotNull CommandSourceStack source, ServerPlayer player,
                                    Identifier id) {
        if (!checkTargetPermission(source, player, "removeShape")) return;
        EntityType<?> type = resolveType(id);
        PlayerMorph.getUnlockedShapes(player).remove(type);
        source.sendSuccess(() -> Component.translatable(
                Remorphed.MODID + ".removeShape",
                Component.translatable(type.getDescriptionId()),
                player.getName()), true);
    }

    private static void addShape(@NotNull CommandSourceStack source, ServerPlayer player,
                                 Identifier id) {
        if (!checkTargetPermission(source, player, "addShape")) return;
        EntityType<?> type = resolveType(id);
        PlayerMorph.getUnlockedShapes(player).put((EntityType<? extends LivingEntity>) type, Remorphed.getKillToUnlock(type));
        source.sendSuccess(() -> Component.translatable(
                Remorphed.MODID + ".addShape",
                player.getName(),
                Component.translatable(type.getDescriptionId())), true);
    }

    private static void clearShapes(@NotNull CommandSourceStack source, ServerPlayer player) {
        if (!checkTargetPermission(source, player, "clearShapes")) return;
        PlayerMorph.getUnlockedShapes(player).clear();
        PlayerMorph.getShapeCounter(player).clear();
        source.sendSuccess(() -> Component.translatable(
                Remorphed.MODID + ".clearShapes", player.getName()), true);
        PlayerShapeChanger.change2ndShape(player, null);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static EntityType<?> resolveType(Identifier id) {
        return BuiltInRegistries.ENTITY_TYPE.get(id).map(Holder::value).orElse(null);
    }

    /** Gate applied to the command node itself. Shape commands allow all players when permissions are off. */
    private static boolean requiresShapePermission(CommandSourceStack source, String command) {
        if (source.getEntity() == null) {
            return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        }
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!Remorphed.CONFIG.usePermissions) return true;
            return PermissionManager.canUseCommandOnSelf(player, command)
                    || PermissionManager.canUseCommandOnOthers(player, command);
        }
        return false;
    }

    /** Per-execution check against a specific target. Sends failure and returns false if denied. */
    private static boolean checkTargetPermission(CommandSourceStack source, ServerPlayer target, String command) {
        if (source.getEntity() instanceof ServerPlayer executor && Remorphed.CONFIG.usePermissions) {
            if (!PermissionManager.canUseCommandOnTarget(executor, target, command)) {
                source.sendFailure(Component.translatable("commands.generic.permission"));
                return false;
            }
        }
        return true;
    }
}