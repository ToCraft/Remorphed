package dev.tocraft.remorphed.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tocraft.craftedcore.event.common.CommandEvents;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.impl.PlayerMorph;
import dev.tocraft.remorphed.permission.PermissionManager;
import dev.tocraft.skinshifter.SkinShifter;
import dev.tocraft.skinshifter.data.SkinPlayerData;
import dev.tocraft.walkers.api.PlayerShapeChanger;
import dev.tocraft.walkers.api.variant.ShapeType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// TODO: Throw when no Player can be found
@SuppressWarnings("UnstableApiUsage")
public class RemorphedCommand implements CommandEvents.CommandRegistration {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, CommandSelection selection) {

        LiteralCommandNode<CommandSourceStack> rootNode = Commands.literal(Remorphed.MODID)
                .requires(source -> source.hasPermission(0)).build();

        rootNode.addChild(ListPermissionsCommand.createNode());
        rootNode.addChild(RegisterPermissionsCommand.createNode());

        /*
         * Used to remove an unlocked shape of the specified Player.
         */
        LiteralCommandNode<CommandSourceStack> removeShape = Commands.literal("removeShape")
                .requires(source -> {
                    // Console usage (no player entity) - allow if has permission level 2+
                    if (source.getEntity() == null) {
                        return source.hasPermission(2);
                    }
                    // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                    if (source.getEntity() instanceof ServerPlayer player) {
                        // If permissions are disabled, allow command usage for all players
                        if (!Remorphed.CONFIG.usePermissions) {
                            return true;
                        }
                        return PermissionManager.canUseCommandOnSelf(player, "removeShape") ||
                                PermissionManager.canUseCommandOnOthers(player, "removeShape");
                    }
                    return false;
                })
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).executes(context -> {
                                    removeShape(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                            EntityType.getKey(ResourceArgument.getSummonableEntityType(context, "shape").value()),
                                            null);
                                    return 1;
                                }).then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                        .executes(context -> {
                                            CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");
                                            removeShape(context.getSource(),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    EntityType.getKey(ResourceArgument.getSummonableEntityType(context, "shape").value()),
                                                    nbt);

                                            return 1;
                                        }))))
                .build();

        /*
         * Used to add a shape to the specified Player.
         */
        LiteralCommandNode<CommandSourceStack> addShape = Commands.literal("addShape")
                .requires(source -> {
                    // Console usage (no player entity) - allow if has permission level 2+
                    if (source.getEntity() == null) {
                        return source.hasPermission(2);
                    }
                    // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                    if (source.getEntity() instanceof ServerPlayer player) {
                        // If permissions are disabled, allow command usage for all players
                        if (!Remorphed.CONFIG.usePermissions) {
                            return true;
                        }
                        return PermissionManager.canUseCommandOnSelf(player, "addShape") ||
                                PermissionManager.canUseCommandOnOthers(player, "addShape");
                    }
                    return false;
                })
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).executes(context -> {
                                    addShape(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                            EntityType.getKey(ResourceArgument.getSummonableEntityType(context, "shape").value()),
                                            null);
                                    return 1;
                                }).then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                        .executes(context -> {
                                            CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                            addShape(context.getSource(),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    EntityType.getKey(ResourceArgument.getSummonableEntityType(context, "shape").value()),
                                                    nbt);

                                            return 1;
                                        }))))
                .build();

        /*
         * Used to remove all unlocked shapes of the specified Player.
         */
        LiteralCommandNode<CommandSourceStack> clearShapes = Commands.literal("clearShapes")
                .requires(source -> {
                    // Console usage (no player entity) - allow if has permission level 2+
                    if (source.getEntity() == null) {
                        return source.hasPermission(2);
                    }
                    // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                    if (source.getEntity() instanceof ServerPlayer player) {
                        // If permissions are disabled, allow command usage for all players
                        if (!Remorphed.CONFIG.usePermissions) {
                            return true;
                        }
                        return PermissionManager.canUseCommandOnSelf(player, "clearShapes") ||
                                PermissionManager.canUseCommandOnOthers(player, "clearShapes");
                    }
                    return false;
                })
                .then(Commands.argument("player", EntityArgument.players()).executes(context -> {
                    clearShapes(context.getSource(), EntityArgument.getPlayer(context, "player"));
                    return 1;
                })).build();

        /*
         * Used to check if a player has unlocked a specific shape
         */
        LiteralCommandNode<CommandSourceStack> hasShape = Commands.literal("hasShape")
                .requires(source -> {
                    // Console usage (no player entity) - allow if has permission level 2+
                    if (source.getEntity() == null) {
                        return source.hasPermission(2);
                    }
                    // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                    if (source.getEntity() instanceof ServerPlayer player) {
                        // If permissions are disabled, allow command usage for all players
                        if (!Remorphed.CONFIG.usePermissions) {
                            return true;
                        }
                        return PermissionManager.canUseCommandOnSelf(player, "hasShape") ||
                                PermissionManager.canUseCommandOnOthers(player, "hasShape");
                    }
                    return false;
                })
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).executes(context -> hasShape(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        EntityType.getKey(ResourceArgument.getSummonableEntityType(context, "shape").value()),
                                        null)).then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                        .executes(context -> {
                                            CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                            return hasShape(context.getSource(),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    EntityType.getKey(ResourceArgument.getSummonableEntityType(context, "shape").value()),
                                                    nbt);
                                        }))))
                .build();


        rootNode.addChild(removeShape);
        rootNode.addChild(addShape);
        rootNode.addChild(clearShapes);
        rootNode.addChild(hasShape);

        if (Remorphed.foundSkinShifter) {
            LiteralCommandNode<CommandSourceStack> removeSkin = Commands.literal("removeSkin")
                    .requires(source -> {
                        // Console usage (no player entity) - allow if has permission level 2+
                        if (source.getEntity() == null) {
                            return source.hasPermission(2);
                        }
                        // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                        if (source.getEntity() instanceof ServerPlayer player) {
                            // If permissions are disabled, allow command usage based on permission level
                            if (!Remorphed.CONFIG.usePermissions) {
                                return player.hasPermissions(2);
                            }
                            return PermissionManager.canUseCommandOnSelf(player, "removeSkin") ||
                                    PermissionManager.canUseCommandOnOthers(player, "removeSkin");
                        }
                        return false;
                    })
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.argument("playerUUID", UuidArgument.uuid())
                                    .executes(context -> {
                                        UUID playerUUID = UuidArgument.getUuid(context, "playerUUID");
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        SkinPlayerData.getSkinProfile(playerUUID).thenAccept(playerProfile -> {
                                            if (playerProfile.isEmpty()) {
                                                context.getSource().sendSuccess(() -> Component.translatable("skinshifter.invalid_player", playerUUID), true);
                                            } else {
                                                removeSkin(context.getSource(), player, playerProfile.get());
                                            }
                                        });
                                        return 1;
                                    }))
                            .then(Commands.argument("playerName", MessageArgument.message())
                                    .executes(context -> {
                                        String playerName = MessageArgument.getMessage(context, "playerName").getString();
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        SkinPlayerData.getSkinProfile(playerName).thenAccept(playerProfile -> {
                                            if (playerProfile.isEmpty()) {
                                                context.getSource().sendSuccess(() -> Component.translatable("skinshifter.invalid_player", playerName), true);
                                            } else {
                                                removeSkin(context.getSource(), player, playerProfile.get());
                                            }
                                        });
                                        return 1;
                                    }))).build();

            LiteralCommandNode<CommandSourceStack> addSkin = Commands.literal("addSkin")
                    .requires(source -> {
                        // Console usage (no player entity) - allow if has permission level 2+
                        if (source.getEntity() == null) {
                            return source.hasPermission(2);
                        }
                        // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                        if (source.getEntity() instanceof ServerPlayer player) {
                            // If permissions are disabled, allow command usage based on permission level
                            if (!Remorphed.CONFIG.usePermissions) {
                                return player.hasPermissions(2);
                            }
                            return PermissionManager.canUseCommandOnSelf(player, "addSkin") ||
                                    PermissionManager.canUseCommandOnOthers(player, "addSkin");
                        }
                        return false;
                    })
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.argument("playerUUID", UuidArgument.uuid())
                                    .executes(context -> {
                                        UUID playerUUID = UuidArgument.getUuid(context, "playerUUID");
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        SkinPlayerData.getSkinProfile(playerUUID).thenAccept(playerProfile -> {
                                            if (playerProfile.isEmpty()) {
                                                context.getSource().sendSuccess(() -> Component.translatable("skinshifter.invalid_player", playerUUID), true);
                                            } else {
                                                addSkin(context.getSource(), player, playerProfile.get());
                                            }
                                        });
                                        return 1;
                                    }))
                            .then(Commands.argument("playerName", MessageArgument.message())
                                    .executes(context -> {
                                        String playerName = MessageArgument.getMessage(context, "playerName").getString();
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        SkinPlayerData.getSkinProfile(playerName).thenAccept(playerProfile -> {
                                            if (playerProfile.isEmpty()) {
                                                context.getSource().sendSuccess(() -> Component.translatable("skinshifter.invalid_player", playerName), true);
                                            } else {
                                                addSkin(context.getSource(), player, playerProfile.get());
                                            }
                                        });
                                        return 1;
                                    }))).build();

            LiteralCommandNode<CommandSourceStack> clearSkins = Commands.literal("clearSkins")
                    .requires(source -> {
                        // Console usage (no player entity) - allow if has permission level 2+
                        if (source.getEntity() == null) {
                            return source.hasPermission(2);
                        }
                        // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                        if (source.getEntity() instanceof ServerPlayer player) {
                            // If permissions are disabled, allow command usage based on permission level
                            if (!Remorphed.CONFIG.usePermissions) {
                                return player.hasPermissions(2);
                            }
                            return PermissionManager.canUseCommandOnSelf(player, "clearSkins") ||
                                    PermissionManager.canUseCommandOnOthers(player, "clearSkins");
                        }
                        return false;
                    })
                    .then(Commands.argument("player", EntityArgument.players()).executes(context -> {
                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                        clearSkins(context.getSource(), player);
                        return 1;
                    })).build();

            /*
             * Used to check if a player has unlocked a specific shape
             */
            LiteralCommandNode<CommandSourceStack> hasSkin = Commands.literal("hasSkin")
                    .requires(source -> {
                        // Console usage (no player entity) - allow if has permission level 2+
                        if (source.getEntity() == null) {
                            return source.hasPermission(2);
                        }
                        // Player usage - check if they can use command at all (will check target-specific permissions in execution)
                        if (source.getEntity() instanceof ServerPlayer player) {
                            // If permissions are disabled, allow command usage based on permission level
                            if (!Remorphed.CONFIG.usePermissions) {
                                return player.hasPermissions(2);
                            }
                            return PermissionManager.canUseCommandOnSelf(player, "hasSkin") ||
                                    PermissionManager.canUseCommandOnOthers(player, "hasSkin");
                        }
                        return false;
                    })
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.argument("playerUUID", UuidArgument.uuid())
                                    .executes(context -> {
                                        UUID playerUUID = UuidArgument.getUuid(context, "playerUUID");
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        SkinPlayerData.getSkinProfile(playerUUID).thenAccept(playerProfile -> {
                                            if (playerProfile.isEmpty()) {
                                                context.getSource().sendSuccess(() -> Component.translatable("skinshifter.invalid_player", playerUUID), true);
                                            } else {
                                                hasSkin(context.getSource(), player, playerProfile.get());
                                            }
                                        });
                                        return 1;
                                    }))
                            .then(Commands.argument("playerName", MessageArgument.message())
                                    .executes(context -> {
                                        String playerName = MessageArgument.getMessage(context, "playerName").getString();
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        CompletableFuture.runAsync(() -> {
                                            GameProfile playerProfile = SkinPlayerData.getSkinProfile(playerName).getNow(Optional.empty()).orElse(null);
                                            if (playerProfile == null) {
                                                context.getSource().sendSuccess(() -> Component.translatable("skinshifter.invalid_player", playerName), true);
                                            } else {
                                                hasSkin(context.getSource(), player, playerProfile);
                                            }
                                        });
                                        return 1;
                                    }))).build();

            rootNode.addChild(removeSkin);
            rootNode.addChild(addSkin);
            rootNode.addChild(clearSkins);
            rootNode.addChild(hasSkin);
        }

        dispatcher.getRoot().addChild(rootNode);

    }

    private static int hasShape(@NotNull CommandSourceStack source, ServerPlayer player, ResourceLocation id, @Nullable CompoundTag nbt) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "hasShape")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return 0;
                }
            }
        }

        ShapeType<LivingEntity> type = getType(source.getLevel(), id, nbt);
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        if (PlayerMorph.getUnlockedShapes(player).containsKey(type)) {
            source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".hasShape_success",
                    player.getName(), name), true);

            return 1;
        } else
            source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".hasShape_fail", player.getName(), name), true);

        return 0;
    }

    private static void removeShape(@NotNull CommandSourceStack source, ServerPlayer player, ResourceLocation id, @Nullable CompoundTag nbt) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "removeShape")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return;
                }
            }
        }

        ShapeType<LivingEntity> type = getType(source.getLevel(), id, nbt);
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        PlayerMorph.getUnlockedShapes(player).remove(type);

        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".removeShape", name, player.getName()), true);
    }

    private static void addShape(@NotNull CommandSourceStack source, ServerPlayer player, ResourceLocation id, @Nullable CompoundTag nbt) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "addShape")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return;
                }
            }
        }

        ShapeType<LivingEntity> type = getType(source.getLevel(), id, nbt);
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        PlayerMorph.getUnlockedShapes(player).put(type, Remorphed.getKillToUnlock(type.getEntityType()));

        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".addShape", player.getName(), name), true);
    }

    private static void clearShapes(@NotNull CommandSourceStack source, ServerPlayer player) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "clearShapes")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return;
                }
            }
        }

        PlayerMorph.getUnlockedShapes(player).clear();
        PlayerMorph.getShapeCounter(player).clear();

        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".clearShapes", player.getName()), true);
        PlayerShapeChanger.change2ndShape(player, null);
    }

    @SuppressWarnings("unchecked")
    private static ShapeType<LivingEntity> getType(ServerLevel serverLevel, ResourceLocation id, @Nullable CompoundTag nbt) {
        ShapeType<LivingEntity> type = ShapeType.from((EntityType<LivingEntity>) BuiltInRegistries.ENTITY_TYPE.get(id).map(Holder::value).orElse(null));

        if (nbt != null) {
            CompoundTag copy = nbt.copy();
            copy.putString("id", id.toString());
            Entity loaded = EntityType.loadEntityRecursive(copy, serverLevel, EntitySpawnReason.LOAD, it -> it);
            if (loaded instanceof LivingEntity living) {
                type = new ShapeType<>(living);
            }
        }

        return type;
    }

    private static void hasSkin(CommandSourceStack source, ServerPlayer player, @NotNull GameProfile playerProfile) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "hasSkin")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return;
                }
            }
        }

        if (PlayerMorph.getUnlockedSkinIds(player).containsKey(playerProfile.getId())) {
            source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".hasSkin_success",
                    player.getName(), playerProfile.getName()), true);

        } else
            source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".hasSkin_fail", player.getName(), playerProfile.getName()), true);

    }

    private static void removeSkin(@NotNull CommandSourceStack source, ServerPlayer player, @NotNull GameProfile playerProfile) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "removeSkin")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return;
                }
            }
        }

        PlayerMorph.getUnlockedSkinIds(player).remove(playerProfile.getId());

        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".removeSkin", playerProfile.getName(), player.getName()), true);
    }

    private static void addSkin(@NotNull CommandSourceStack source, ServerPlayer player, @NotNull GameProfile playerProfile) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "addSkin")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return;
                }
            }
        }

        PlayerMorph.getUnlockedSkinIds(player).put(playerProfile.getId(), Remorphed.CONFIG.killToUnlockPlayers);

        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".addSkin", player.getName(), playerProfile.getName()), true);
    }

    private static void clearSkins(@NotNull CommandSourceStack source, ServerPlayer player) {
        // Check permissions if executed by a player
        if (source.getEntity() instanceof ServerPlayer executor) {
            // If permissions are disabled, allow all players to use commands
            if (Remorphed.CONFIG.usePermissions) {
                if (!PermissionManager.canUseCommandOnTarget(executor, player, "clearSkins")) {
                    source.sendFailure(Component.translatable("commands.generic.permission"));
                    return;
                }
            }
        }

        PlayerMorph.getUnlockedSkinIds(player).clear();
        PlayerMorph.getSkinCounter(player).clear();

        source.sendSuccess(() -> Component.translatable(Remorphed.MODID + ".clearSkins", player.getName()), true);
        if (Remorphed.foundSkinShifter) {
            SkinShifter.setSkin(player, null);
        }
    }
}
