package dev.tocraft.remorphed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tocraft.craftedcore.event.common.CommandEvents;
import dev.tocraft.remorphed.Remorphed;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple command to list all ReMorphed permissions for easy copy-paste into LuckPerms
 */
public class ListPermissionsCommand implements CommandEvents.CommandRegistration {
    
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        LiteralCommandNode<CommandSourceStack> listPermissions = Commands.literal("remorphed-list-permissions")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    listAllPermissions(context.getSource());
                    return 1;
                }).build();
        
        dispatcher.getRoot().addChild(listPermissions);
    }
    
    private void listAllPermissions(CommandSourceStack source) {
        List<String> permissions = getAllPermissions();
        
        source.sendSuccess(() -> Component.literal("§6=== ReMorphed Permissions List ==="), false);
        source.sendSuccess(() -> Component.literal("§eCopy and paste these into LuckPerms:"), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        source.sendSuccess(() -> Component.literal("§a§lCore Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.menu true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.morph true"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.creative true"), false);
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
        source.sendSuccess(() -> Component.literal("§a§lExample Configuration Overrides:"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group vip permission set remorphed.unlockKills.1 true §7# Only need 1 kill"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group vip permission set remorphed.unlockKills.0 true §7# Instant unlock"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group premium permission set remorphed.killValue.10 true §7# Keep unlocks longer"), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§a§lWildcard Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.* true §7# All permissions"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group default permission set remorphed.type.* true §7# All entity types"), false);
        source.sendSuccess(() -> Component.literal("§f/lp group admin permission set remorphed.command.* true §7# All commands"), false);
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§e§lTotal: " + permissions.size() + " permissions available"), false);
        source.sendSuccess(() -> Component.literal("§7Use these exact commands in your server console or LuckPerms web editor"), false);
    }
    
    private List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>();
        
        // Core permissions
        permissions.add("remorphed.menu");
        permissions.add("remorphed.morph");
        permissions.add("remorphed.creative");
        permissions.add("remorphed.bypass.lock");
        
        // Command permissions
        permissions.add("remorphed.command.addShape");
        permissions.add("remorphed.command.removeShape");
        permissions.add("remorphed.command.clearShapes");
        permissions.add("remorphed.command.hasShape");
        permissions.add("remorphed.command.addSkin");
        permissions.add("remorphed.command.removeSkin");
        permissions.add("remorphed.command.clearSkins");
        permissions.add("remorphed.command.hasSkin");
        
        // Entity type permissions for all registered entities
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            ResourceLocation key = EntityType.getKey(entityType);
            if (key != null) {
                permissions.add("remorphed.type." + key.toString());
            }
        });
        
        // Dynamic configuration permissions (0-20 range)
        for (int i = 0; i <= 20; i++) {
            permissions.add("remorphed.unlockKills." + i);
            permissions.add("remorphed.playerUnlockKills." + i);
            permissions.add("remorphed.killValue." + i);
            permissions.add("remorphed.playerKillValue." + i);
        }
        
        Collections.sort(permissions);
        return permissions;
    }
}
