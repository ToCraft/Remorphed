package dev.tocraft.remorphed.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tocraft.remorphed.permission.PermissionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Command to manually trigger permission registration
 * This is useful for admins to ensure permissions are visible in LuckPerms GUI
 */
public class RegisterPermissionsCommand {

    public static LiteralCommandNode<CommandSourceStack> createNode() {
        return Commands.literal("register-permissions")
                .requires(source -> source.hasPermission(2))
                .executes(RegisterPermissionsCommand::execute)
                .build();
    }

    private static int execute(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        source.sendSuccess(() -> Component.literal("§7Triggering permission registration..."), false);

        // Run permission registration in a separate thread to avoid blocking
        Thread registrationThread = new Thread(() -> {
            try {
                // Check ALL permissions to ensure they're all registered
                // This is the most comprehensive approach
                Set<String> allPermissions = getAllPermissions();
                final int totalPermissions = allPermissions.size();
                int checked = 0;

                for (String permission : allPermissions) {
                    try {
                        PermissionManager.hasPermission(player, permission);
                        checked++;
                        Thread.sleep(10); // Small delay between checks

                        // Log progress every 100 permissions (less verbose)
                        if (checked % 100 == 0 && checked > 0) {
                            final int currentChecked = checked;
                            source.getServer().execute(() -> source.sendSuccess(() -> Component.literal("§7Checked " + currentChecked + "/" + totalPermissions + " permissions..."), false));
                        }
                    } catch (Exception e) {
                        // Continue with other permissions
                    }
                }

                // Send success message
                source.getServer().execute(() -> source.sendSuccess(() -> Component.literal("§aPermission registration completed! Check your LuckPerms GUI."), false));

            } catch (Exception e) {
                source.getServer().execute(() -> source.sendFailure(Component.literal("§cPermission registration failed: " + e.getMessage())));
            }
        });

        registrationThread.setName("ReMorphed-PermissionRegistration");
        registrationThread.start();

        return 1;
    }

    private static @NotNull Set<String> getAllPermissions() {
        Set<String> permissions = new HashSet<>();

        // Core permissions
        permissions.add("remorphed.morph");
        permissions.add("remorphed.bypass.lock");

        // Command permissions (basic)
        permissions.add("remorphed.command.addShape");
        permissions.add("remorphed.command.removeShape");
        permissions.add("remorphed.command.clearShapes");
        permissions.add("remorphed.command.hasShape");
        permissions.add("remorphed.command.addSkin");
        permissions.add("remorphed.command.removeSkin");
        permissions.add("remorphed.command.clearSkins");
        permissions.add("remorphed.command.hasSkin");

        // Command permissions (.self variants)
        permissions.add("remorphed.command.addShape.self");
        permissions.add("remorphed.command.removeShape.self");
        permissions.add("remorphed.command.clearShapes.self");
        permissions.add("remorphed.command.hasShape.self");
        permissions.add("remorphed.command.addSkin.self");
        permissions.add("remorphed.command.removeSkin.self");
        permissions.add("remorphed.command.clearSkins.self");
        permissions.add("remorphed.command.hasSkin.self");

        // Command permissions (.others variants)
        permissions.add("remorphed.command.addShape.others");
        permissions.add("remorphed.command.removeShape.others");
        permissions.add("remorphed.command.clearShapes.others");
        permissions.add("remorphed.command.hasShape.others");
        permissions.add("remorphed.command.addSkin.others");
        permissions.add("remorphed.command.removeSkin.others");
        permissions.add("remorphed.command.clearSkins.others");
        permissions.add("remorphed.command.hasSkin.others");

        // Entity type permissions for all registered entities
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            ResourceLocation key = EntityType.getKey(entityType);
            permissions.add("remorphed.type." + key);
        });


        return permissions;
    }
}
