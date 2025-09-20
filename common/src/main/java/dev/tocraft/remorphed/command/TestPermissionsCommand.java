package dev.tocraft.remorphed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.tocraft.craftedcore.event.common.CommandEvents;
import dev.tocraft.remorphed.permission.PermissionManager;
import dev.tocraft.remorphed.permission.PermissionRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Debug command to test permission checks
 */
public class TestPermissionsCommand implements CommandEvents.CommandRegistration {
    
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("remorphed-test-permissions")
            .requires(source -> source.hasPermission(2))
            .executes(this::execute));
    }
    
    private int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        PermissionManager manager = PermissionRegistry.getInstance();
        
        source.sendSuccess(() -> Component.literal("§6=== ReMorphed Permission Test ==="), false);
        source.sendSuccess(() -> Component.literal("§7Testing permissions for: " + player.getName().getString()), false);
        source.sendSuccess(() -> Component.literal(""), false);
        
        // Test core permissions
        boolean menuPerm = manager.hasPermission(player, "remorphed.menu");
        boolean morphPerm = manager.hasPermission(player, "remorphed.morph");
        boolean creativePerm = manager.hasPermission(player, "remorphed.creative");
        boolean bypassPerm = manager.hasPermission(player, "remorphed.bypass.lock");
        
        source.sendSuccess(() -> Component.literal("§a§lCore Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§fremorphed.menu: " + (menuPerm ? "§a✓" : "§c✗")), false);
        source.sendSuccess(() -> Component.literal("§fremorphed.morph: " + (morphPerm ? "§a✓" : "§c✗")), false);
        source.sendSuccess(() -> Component.literal("§fremorphed.creative: " + (creativePerm ? "§a✓" : "§c✗")), false);
        source.sendSuccess(() -> Component.literal("§fremorphed.bypass.lock: " + (bypassPerm ? "§a✓" : "§c✗")), false);
        
        // Test entity permissions
        boolean zombiePerm = manager.hasPermission(player, "remorphed.type.minecraft:zombie");
        boolean skeletonPerm = manager.hasPermission(player, "remorphed.type.minecraft:skeleton");
        boolean creeperPerm = manager.hasPermission(player, "remorphed.type.minecraft:creeper");
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§a§lEntity Permissions:"), false);
        source.sendSuccess(() -> Component.literal("§fremorphed.type.minecraft:zombie: " + (zombiePerm ? "§a✓" : "§c✗")), false);
        source.sendSuccess(() -> Component.literal("§fremorphed.type.minecraft:skeleton: " + (skeletonPerm ? "§a✓" : "§c✗")), false);
        source.sendSuccess(() -> Component.literal("§fremorphed.type.minecraft:creeper: " + (creeperPerm ? "§a✓" : "§c✗")), false);
        
        // Test method calls
        boolean canMorph = manager.canMorph(player);
        boolean canAccessMenu = manager.canAccessMenu(player);
        boolean canUseCreative = manager.canUseCreativeMode(player);
        
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§a§lMethod Results:"), false);
        source.sendSuccess(() -> Component.literal("§fcanMorph(): " + (canMorph ? "§a✓" : "§c✗")), false);
        source.sendSuccess(() -> Component.literal("§fcanAccessMenu(): " + (canAccessMenu ? "§a✓" : "§c✗")), false);
        source.sendSuccess(() -> Component.literal("§fcanUseCreativeMode(): " + (canUseCreative ? "§a✓" : "§c✗")), false);
        
        // Test player's actual permissions
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§a§lPlayer Info:"), false);
        source.sendSuccess(() -> Component.literal("§fIs OP: " + (player.hasPermissions(2) ? "§aYes" : "§cNo")), false);
        source.sendSuccess(() -> Component.literal("§fPermission Level: " + player.getPermissionLevel()), false);
        
        return 1;
    }
}
