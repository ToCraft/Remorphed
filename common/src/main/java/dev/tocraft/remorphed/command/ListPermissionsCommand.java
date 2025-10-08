package dev.tocraft.remorphed.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Simple command to list all ReMorphed permissions for easy copy-paste into LuckPerms
 */
public class ListPermissionsCommand {

    public static LiteralCommandNode<CommandSourceStack> createNode() {
        return Commands.literal("list-permissions")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    listAllPermissions(context.getSource());
                    return 1;
                }).build();
    }

    private static void listAllPermissions(@NotNull CommandSourceStack source) {
        int size = countPermissions();

        source.sendSuccess(() -> Component.literal("§6=== ReMorphed Permissions List ==="), false);
        source.sendSuccess(() -> Component.literal("§eCopy and paste these into LuckPerms:"), false);
        source.sendSuccess(() -> Component.literal(""), false);

        source.sendSuccess(() -> Component.literal("§a§lCore Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.morph true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.bypass.lock true"), false);

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§a§lCommand Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.command.addShape true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.command.removeShape true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.command.clearShapes true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.command.hasShape true"), false);

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§a§lExample Entity Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.type.minecraft:zombie true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.type.minecraft:skeleton true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.type.minecraft:creeper true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group vip permission set remorphed.type.minecraft:enderman true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.type.minecraft:ender_dragon true"), false);

        source.sendSuccess(() -> Component.literal(""), false);

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§a§lWildcard Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.* true §7# All permissions"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.type.* true §7# All entity types"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.command.* true §7# All commands"), false);

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§e§lTotal: " + size + " permissions available"), false);
        source.sendSuccess(() -> Component.literal("§7Use these exact commands in your server console or LuckPerms web editor"), false);
    }

    private static int countPermissions() {
        int i = 0;

        // Core permissions
        i++;   // remorphed.morph
        i++;   // remorphed.bypass.lock

        // Command permissions
        i++;   // remorphed.command.addShape
        i++;   // remorphed.command.removeShape
        i++;   // remorphed.command.clearShapes
        i++;   // remorphed.command.hasShape
        i++;   // remorphed.command.addSkin
        i++;   // remorphed.command.removeSkin
        i++;   // remorphed.command.clearSkins
        i++;   // remorphed.command.hasSkin

        // Entity type permissions for all registered entities
        i += BuiltInRegistries.ENTITY_TYPE.size();

        return i;
    }
}
