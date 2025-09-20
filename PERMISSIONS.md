# ReMorphed Permission System

ReMorphed supports permission plugins like LuckPerms and PermissionsEx for both Fabric and NeoForge platforms. This document outlines all available permission nodes and their functionality.

## Platform Support

- **NeoForge**: Uses built-in PermissionAPI (works automatically)
- **Fabric**: Uses Fabric Permissions API (requires manual registration command)

## Quick Start

### For NeoForge Servers
Permissions should work automatically. If not, run `/remorphed-register-permissions` once as an admin.

### For Fabric Servers
After installing/updating the mod, run `/remorphed-register-permissions` as an admin to make permissions visible in LuckPerms GUI.

## Permission Nodes

### Core Access

| Permission | Description | Default |
|------------|-------------|---------|
| `remorphed.menu` | Access to the morph selection menu | Operator (level 2+) |
| `remorphed.morph` | Basic morphing functionality | Operator (level 2+) |
| `remorphed.creative` | Bypass creative mode restrictions | Operator (level 2+) |
| `remorphed.bypass.lock` | Bypass transform lock when enabled | Operator (level 2+) |

### Commands

All `/remorphed` commands require specific permissions:

| Permission | Command |
|------------|---------|
| `remorphed.command.addShape` | `/remorphed addShape` |
| `remorphed.command.removeShape` | `/remorphed removeShape` |
| `remorphed.command.clearShapes` | `/remorphed clearShapes` |
| `remorphed.command.hasShape` | `/remorphed hasShape` |
| `remorphed.command.addSkin` | `/remorphed addSkin` (requires SkinShifter) |
| `remorphed.command.removeSkin` | `/remorphed removeSkin` (requires SkinShifter) |
| `remorphed.command.clearSkins` | `/remorphed clearSkins` (requires SkinShifter) |
| `remorphed.command.hasSkin` | `/remorphed hasSkin` (requires SkinShifter) |

### Entity Types

Control access to specific mob types:

| Permission | Description | Example |
|------------|-------------|---------|
| `remorphed.type.<entity_id>` | Morph into specific entity | `remorphed.type.minecraft:zombie` |

**Common Entity Permissions:**
- `remorphed.type.minecraft:zombie` - Zombies
- `remorphed.type.minecraft:skeleton` - Skeletons
- `remorphed.type.minecraft:creeper` - Creepers
- `remorphed.type.minecraft:enderman` - Endermen
- `remorphed.type.minecraft:ender_dragon` - Ender Dragon

### Configuration Overrides

Override config values for specific players:

| Permission | Description | Example |
|------------|-------------|---------|
| `remorphed.unlockKills.<number>` | Kill requirement for unlocking mobs | `remorphed.unlockKills.1` (only need 1 kill) |
| `remorphed.playerUnlockKills.<number>` | Kill requirement for player skins | `remorphed.playerUnlockKills.2` (need 2 player kills) |
| `remorphed.killValue.<number>` | Morphs before losing unlock | `remorphed.killValue.10` (lose after 10 morphs) |
| `remorphed.playerKillValue.<number>` | Player skin morph usage count | `remorphed.playerKillValue.5` (lose after 5 morphs) |

**Special Values:**
- `remorphed.unlockKills.0` - Instant unlock (no kills needed)
- `remorphed.killValue.0` - Never lose unlocks

## Commands

### `/remorphed-register-permissions`
Manually registers all permissions with LuckPerms. **Required for Fabric servers** to make permissions visible in the GUI.

### `/remorphed-test-permissions`
Tests all permission checks for the current player. Useful for debugging permission issues.

### `/remorphed-list-permissions`
Lists all available permissions with copy-paste LuckPerms commands.

## Configuration Examples

### LuckPerms Setup

```bash
# Basic player permissions
/lp user <player> permission set remorphed.menu true
/lp user <player> permission set remorphed.morph true

# Allow specific mobs
/lp user <player> permission set remorphed.type.minecraft:zombie true
/lp user <player> permission set remorphed.type.minecraft:skeleton true

# VIP group with reduced requirements
/lp group vip permission set remorphed.unlockKills.1 true
/lp group vip permission set remorphed.playerUnlockKills.1 true

# Admin full access
/lp group admin permission set remorphed.* true
```

### Wildcard Permissions

| Permission | Description |
|------------|-------------|
| `remorphed.*` | All ReMorphed permissions |
| `remorphed.type.*` | All entity type permissions |
| `remorphed.command.*` | All command permissions |

## Troubleshooting

### Common Issues

1. **Menu won't open**: Check `remorphed.menu` permission
2. **Can't morph**: Check `remorphed.morph` permission  
3. **Specific mob denied**: Check `remorphed.type.<entity_id>` permission
4. **Commands not working**: Check `remorphed.command.<command>` permission
5. **Permissions not showing in LuckPerms GUI**: Run `/remorphed-register-permissions`
6. **Can't transform despite permissions**: Run `/remorphed-test-permissions` to debug

### Debug Commands

- `/remorphed-test-permissions` - Shows which permissions are working
- `/remorphed-list-permissions` - Lists all permissions with copy-paste commands

### Platform Differences

- **NeoForge**: Permissions work automatically with built-in PermissionAPI
- **Fabric**: Requires `/remorphed-register-permissions` command for GUI visibility

## Default Behavior

If no permission plugin is installed, ReMorphed falls back to operator permissions (level 2+). All permission checks return `true` for operators and `false` for regular players.

## Migration from Config-Only

Config values serve as defaults. Permissions can override them for specific players or groups. For example:
- Config: `killToUnlock = 5`
- Permission: `remorphed.unlockKills.1` (overrides to 1 kill for that player/group)