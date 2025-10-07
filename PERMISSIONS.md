# ReMorphed Permission System

ReMorphed supports permission plugins like LuckPerms and PermissionsEx for both Fabric and NeoForge platforms. This
document outlines all available permission nodes and their functionality.

## Platform Support

- **NeoForge**: Uses built-in PermissionAPI (works automatically)
- **Fabric**: Uses Fabric Permissions API (**requires separate mod installation**)

### ⚠️ **IMPORTANT FOR FABRIC SERVERS**

**Fabric servers MUST install the [Fabric Permissions API](https://modrinth.com/mod/fabric-permissions-api) mod
separately!**

- Download from: https://modrinth.com/mod/fabric-permissions-api
- Place `fabric-permissions-api.jar` in your server's `mods/` folder
- **Without this mod, permissions will not work on Fabric servers**
- NeoForge servers do not need this - permissions work automatically

## Implementation Status

### ✅ **Fully Working**

- **Unlock/Morph Permissions**: Players can only unlock and morph into entities they have permission for
- **Command Permissions**: Granular `.self` vs `.others` command permissions work correctly

## Quick Start

### Enable Permission System

**IMPORTANT**: Permissions are disabled by default. To enable the permission system:

1. **For Fabric servers**: Install [Fabric Permissions API](https://modrinth.com/mod/fabric-permissions-api) mod first!
2. Set `usePermissions = true` in your ReMorphed config file
3. Restart your server
4. Configure permissions using your permission plugin (LuckPerms, etc.)

### For NeoForge Servers
Permissions work automatically once enabled.

### For Fabric Servers

**REQUIRED**: Install [Fabric Permissions API](https://modrinth.com/mod/fabric-permissions-api) mod first!

After installing the API mod and enabling permissions, permissions will be automatically discovered by LuckPerms when first used. No manual registration needed!

### How Permissions Work

#### When `usePermissions = true` (Permission System Enabled):

- **Unlocking**: Players can only unlock entities they have permission for
- **Morphing**: Players can only morph into entities they have permission for
- **Command Access**: `/remorphed` commands (addShape, clearShapes, etc.) require `.self` and `.others` permissions

#### When `usePermissions = false` (Default Behavior):

- Uses original server config behavior
- No permission checks are performed
- All players follow the same rules based on server config

### Important: Selector Permission + Granular Control

Commands use `EntityArgument.players()` which requires the `minecraft.selector.*` permission for player name targeting.
This provides proper autocomplete functionality.

**Complete Solution**: Grant both selector permission AND specific command permissions:

```bash
# REQUIRED: Grant selector permission (required for player name targeting and autocomplete)
/lp user <player> permission set minecraft.selector.* true

# Grant specific command permissions (controls what they can actually do)
/lp user <player> permission set remorphed.command.clearShapes.self true  # Only on themselves
# OR
/lp user <player> permission set remorphed.command.clearShapes.others true  # Only on others
# OR  
/lp user <player> permission set remorphed.command.clearShapes true  # On anyone (fallback)
```

**How it works:**

1. **`minecraft.selector.*`** - Allows using player names in commands (required by Minecraft for autocomplete)
2. **`remorphed.command.clearShapes.self`** - Allows clearing shapes only on themselves
3. **`remorphed.command.clearShapes.others`** - Allows clearing shapes only on other players
4. **`remorphed.command.clearShapes`** - Allows clearing shapes on anyone (fallback)

**Autocomplete**: With `minecraft.selector.*` permission, players get proper autocomplete for player names when typing
commands.

## Permission Nodes

### Core Access

| Permission              | Description                        | Default             |
|-------------------------|------------------------------------|---------------------|
| `remorphed.morph`       | Basic morphing functionality       | Operator (level 2+) |
| `remorphed.bypass.lock` | Bypass transform lock when enabled | Operator (level 2+) |

### Commands

All `/remorphed` commands support granular permission control with `.self` and `.others` variants:

| Permission                             | Command                  | Description                             |
|----------------------------------------|--------------------------|-----------------------------------------|
| `remorphed.command.addShape`           | `/remorphed addShape`    | Basic permission (works on anyone)      |
| `remorphed.command.addShape.self`      | `/remorphed addShape`    | Can only add shapes to themselves       |
| `remorphed.command.addShape.others`    | `/remorphed addShape`    | Can add shapes to other players         |
| `remorphed.command.removeShape`        | `/remorphed removeShape` | Basic permission (works on anyone)      |
| `remorphed.command.removeShape.self`   | `/remorphed removeShape` | Can only remove their own shapes        |
| `remorphed.command.removeShape.others` | `/remorphed removeShape` | Can remove shapes from other players    |
| `remorphed.command.clearShapes`        | `/remorphed clearShapes` | Basic permission (works on anyone)      |
| `remorphed.command.clearShapes.self`   | `/remorphed clearShapes` | Can only clear their own shapes         |
| `remorphed.command.clearShapes.others` | `/remorphed clearShapes` | Can clear shapes from other players     |
| `remorphed.command.hasShape`           | `/remorphed hasShape`    | Basic permission (works on anyone)      |
| `remorphed.command.hasShape.self`      | `/remorphed hasShape`    | Can only check their own shapes         |
| `remorphed.command.hasShape.others`    | `/remorphed hasShape`    | Can check shapes of other players       |
| `remorphed.command.addSkin`            | `/remorphed addSkin`     | Basic permission (requires SkinShifter) |
| `remorphed.command.addSkin.self`       | `/remorphed addSkin`     | Can only add skins to themselves        |
| `remorphed.command.addSkin.others`     | `/remorphed addSkin`     | Can add skins to other players          |
| `remorphed.command.removeSkin`         | `/remorphed removeSkin`  | Basic permission (requires SkinShifter) |
| `remorphed.command.removeSkin.self`    | `/remorphed removeSkin`  | Can only remove their own skins         |
| `remorphed.command.removeSkin.others`  | `/remorphed removeSkin`  | Can remove skins from other players     |
| `remorphed.command.clearSkins`         | `/remorphed clearSkins`  | Basic permission (requires SkinShifter) |
| `remorphed.command.clearSkins.self`    | `/remorphed clearSkins`  | Can only clear their own skins          |
| `remorphed.command.clearSkins.others`  | `/remorphed clearSkins`  | Can clear skins from other players      |
| `remorphed.command.hasSkin`            | `/remorphed hasSkin`     | Basic permission (requires SkinShifter) |
| `remorphed.command.hasSkin.self`       | `/remorphed hasSkin`     | Can only check their own skins          |
| `remorphed.command.hasSkin.others`     | `/remorphed hasSkin`     | Can check skins of other players        |

### Entity Types

Control access to specific mob types for both unlocking and morphing:

| Permission                   | Description                           | Example                           |
|------------------------------|---------------------------------------|-----------------------------------|
| `remorphed.type.<entity_id>` | Unlock and morph into specific entity | `remorphed.type.minecraft:zombie` |

**Common Entity Permissions:**

- `remorphed.type.minecraft:zombie` - Zombies
- `remorphed.type.minecraft:skeleton` - Skeletons
- `remorphed.type.minecraft:creeper` - Creepers
- `remorphed.type.minecraft:enderman` - Endermen
- `remorphed.type.minecraft:ender_dragon` - Ender Dragon

## Commands

### `/remorphed list-permissions`
Lists all available permissions with copy-paste LuckPerms commands.

**Note**: Only server operators (OP level 2+) can run this command.

## Configuration Examples

### LuckPerms Setup Examples

**Example 1: Regular Player (Self-Only)**

```bash
# Basic permissions
/lp user <player> permission set remorphed.morph true
/lp user <player> permission set remorphed.type.minecraft:zombie true

# REQUIRED: Grant selector permission
/lp user <player> permission set minecraft.selector.* true

# Self-only command permissions (can only use commands on themselves)
/lp user <player> permission set remorphed.command.clearShapes.self true
/lp user <player> permission set remorphed.command.removeShape.self true
```

**Example 2: Helper (Others-Only)**

```bash
# Basic permissions
/lp user <helper> permission set remorphed.morph true
/lp user <helper> permission set remorphed.type.* true

# REQUIRED: Grant selector permission
/lp user <helper> permission set minecraft.selector true

# Others-only command permissions (can only use commands on other players)
/lp user <helper> permission set remorphed.command.addShape.others true
/lp user <helper> permission set remorphed.command.clearShapes.others true
```

**Example 3: Moderator (Both Self and Others)**

```bash
# Basic permissions
/lp user <mod> permission set remorphed.morph true
/lp user <mod> permission set remorphed.type.* true

# REQUIRED: Grant selector permission
/lp user <mod> permission set minecraft.selector true

# Both self and others permissions
/lp user <mod> permission set remorphed.command.*.self true
/lp user <mod> permission set remorphed.command.*.others true
```

**Example 4: Admin (Full Access)**

```bash
# Admin full access
/lp group admin permission set remorphed.* true
# REQUIRED: Grant selector permission
/lp user <player> permission set minecraft.selector.* true
```

### Wildcard Permissions

| Permission                   | Description                       |
|------------------------------|-----------------------------------|
| `remorphed.*`                | All ReMorphed permissions         |
| `remorphed.type.*`           | All entity type permissions       |
| `remorphed.command.*`        | All command permissions (basic)   |
| `remorphed.command.*.self`   | All self-only command permissions |
| `remorphed.command.*.others` | All others command permissions    |

## Troubleshooting

### Common Issues

1. **Permissions not working on Fabric**: *
   *Install [Fabric Permissions API](https://modrinth.com/mod/fabric-permissions-api) mod first!**
2. **Can't morph**: Check `remorphed.morph` permission
3. **Can't unlock specific mob**: Check `remorphed.type.<entity_id>` permission
4. **Can't morph into specific mob**: Check `remorphed.type.<entity_id>` permission
5. **Commands not working**: Check `remorphed.command.<command>` permission
6. **Can't use command on self**: Check `remorphed.command.<command>.self` permission
7. **Can't use command on others**: Check `remorphed.command.<command>.others` permission
8. **"Selector not allowed" error**: Grant `minecraft.selector.*` permission (works with Vanilla Permissions mod)
9. **Permissions not showing in LuckPerms GUI**: Permissions are automatically discovered when first used. If they don't appear, ensure Fabric Permissions API is installed and restart the server.
10. **Can't transform despite permissions**: Check if you have the required entity type permissions

### Debug Commands

- `/remorphed list-permissions` - Lists all permissions with copy-paste commands

### Platform Differences

- **NeoForge**: Permissions work automatically with built-in PermissionAPI
- **Fabric**: Permissions are automatically discovered when first used (requires Fabric Permissions API mod)

## Default Behavior

If no permission plugin is installed, ReMorphed falls back to operator permissions (level 2+). All permission checks
return `true` for operators and `false` for regular players.
