package tocraft.remorphed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import tocraft.remorphed.Remorphed;
import tocraft.remorphed.impl.RemorphedPlayerDataProvider;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerShapeChanger;
import tocraft.walkers.api.variant.ShapeType;

public class RemorphedCommand implements CommandRegistrationEvent {
    private static int hasShape(CommandSourceStack source, ServerPlayer player, ResourceLocation id, @Nullable CompoundTag nbt) {
        ShapeType<LivingEntity> type = getType(source.getLevel(), id, nbt);
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        if (((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().containsKey(type)) {
            if (Walkers.CONFIG.logCommands) {
                source.sendSystemMessage(Component.translatable(Remorphed.MODID + ".hasShape_success",
                        player.getDisplayName(), name));
            }

            return 1;
        } else if (Walkers.CONFIG.logCommands) {
            source.sendSystemMessage(Component.translatable(Remorphed.MODID + ".hasShape_fail", player.getDisplayName(), name));
        }

        return 0;
    }

    private static void removeShape(CommandSourceStack source, ServerPlayer player, ResourceLocation id, @Nullable CompoundTag nbt) {
        ShapeType<LivingEntity> type = getType(source.getLevel(), id, nbt);
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().remove(type);

        if (Walkers.CONFIG.logCommands) {
            source.sendSystemMessage(Component.translatable(Remorphed.MODID + ".removeShape", name, player.getDisplayName()));
        }
    }

    private static void addShape(CommandSourceStack source, ServerPlayer player, ResourceLocation id, @Nullable CompoundTag nbt) {
        ShapeType<LivingEntity> type = getType(source.getLevel(), id, nbt);
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().put(type, Remorphed.getKillToUnlock(type.getEntityType()));

        if (Walkers.CONFIG.logCommands) {
            source.sendSystemMessage(Component.translatable(Remorphed.MODID + ".addShape", player.getDisplayName(), name));
        }
    }

    private static void clearShapes(CommandSourceStack source, ServerPlayer player) {
        ((RemorphedPlayerDataProvider) player).remorphed$getUnlockedShapes().clear();

        if (Walkers.CONFIG.logCommands) {
            source.sendSystemMessage(Component.translatable(Remorphed.MODID + ".clearShapes", player.getDisplayName()));
            PlayerShapeChanger.change2ndShape(player, null);
        }
    }

    private static ShapeType<LivingEntity> getType(ServerLevel serverLevel, ResourceLocation id, @Nullable CompoundTag nbt) {
        ShapeType<LivingEntity> type = ShapeType.from(BuiltInRegistries.ENTITY_TYPE.get(id));

        if (nbt != null) {
            CompoundTag copy = nbt.copy();
            copy.putString("id", id.toString());
            Entity loaded = EntityType.loadEntityRecursive(copy, serverLevel, it -> it);
            if (loaded instanceof LivingEntity living) {
                type = new ShapeType<>(living);
            }
        }

        return type;
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, CommandSelection selection) {

        LiteralCommandNode<CommandSourceStack> rootNode = Commands.literal(Remorphed.MODID)
                .requires(source -> source.hasPermission(2)).build();

        /*
         * Used to remove a unlocked shape of the specified Player.
         */
        LiteralCommandNode<CommandSourceStack> removeShape = Commands.literal("removeShape")
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(context -> {
                                    removeShape(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                            EntityType.getKey(ResourceArgument
                                                    .getSummonableEntityType(context, "shape").value()),
                                            null);
                                    return 1;
                                }).then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                        .executes(context -> {
                                            CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                            removeShape(context.getSource(),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    EntityType.getKey(ResourceArgument
                                                            .getSummonableEntityType(context, "shape").value()),
                                                    nbt);

                                            return 1;
                                        }))))
                .build();

        /*
         * Used to add a shape to the specified Player.
         */
        LiteralCommandNode<CommandSourceStack> addShape = Commands.literal("addShape")
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(context -> {
                                    addShape(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                            EntityType.getKey(ResourceArgument
                                                    .getSummonableEntityType(context, "shape").value()),
                                            null);
                                    return 1;
                                }).then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                        .executes(context -> {
                                            CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                            addShape(context.getSource(),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    EntityType.getKey(ResourceArgument
                                                            .getSummonableEntityType(context, "shape").value()),
                                                    nbt);

                                            return 1;
                                        }))))
                .build();

        /*
         * Used to remove all unlocked shapes of the specified Player.
         */
        LiteralCommandNode<CommandSourceStack> clearShapes = Commands.literal("clearShapes")
                .then(Commands.argument("player", EntityArgument.players()).executes(context -> {
                    clearShapes(context.getSource(), EntityArgument.getPlayer(context, "player"));
                    return 1;
                })).build();

        /*
         * Used to check if a player has unlocked a specific shape
         */
        LiteralCommandNode<CommandSourceStack> hasShape = Commands.literal("hasShape")
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("shape", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(context -> hasShape(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        EntityType.getKey(ResourceArgument
                                                .getSummonableEntityType(context, "shape").value()),
                                        null)).then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                        .executes(context -> {
                                            CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                            return hasShape(context.getSource(),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    EntityType.getKey(ResourceArgument
                                                            .getSummonableEntityType(context, "shape").value()),
                                                    nbt);
                                        }))))
                .build();

        rootNode.addChild(removeShape);
        rootNode.addChild(addShape);
        rootNode.addChild(clearShapes);
        rootNode.addChild(hasShape);

        dispatcher.getRoot().addChild(rootNode);

    }
}
