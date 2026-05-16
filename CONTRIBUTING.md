# Contributing to ReMorphed

Thanks for your interest in contributing to ReMorphed! ReMorphed is a morph addon built on top of Woodwalkers that adds kill-to-unlock mechanics, a shape-selection menu, and support for more than multiple disguises. It is also compatible with [SkinShifter](https://github.com/ToCraft/SkinShifter/) as an optional integration for morphing into other players.

## Table of Contents

- [Ways to Contribute](#ways-to-contribute)
- [Reporting Bugs](#reporting-bugs)
- [Translations](#translations)
- [Development Setup](#development-setup)
- [Project Architecture](#project-architecture)
- [Making Changes](#making-changes)
- [Updating to a New Minecraft Version](#updating-to-a-new-minecraft-version)
- [CI / GitHub Actions](#ci--github-actions)
- [Code Style](#code-style)
- [License](#license)

---

## Ways to Contribute

- **Bug reports** – Open an [issue](https://github.com/ToCraft/Remorphed/issues) describing what went wrong.
- **Translations** – Add or improve language files under `common/src/main/resources/assets/remorphed/lang/`.
- **Bug fixes & features** – Fork the repo, make your changes, and open a pull request against `main`.
- **Financial support** – [Patreon](https://www.patreon.com/tocraft).

---

## Reporting Bugs

Before opening a new issue, please search existing ones to avoid duplicates. When you file a bug, include:

- The exact ReMorphed version (e.g. `8.0`) and Minecraft version.
- Whether you are on Fabric or NeoForge, and the loader version.
- The versions of **CraftedCore** and **Woodwalkers (Walkers)* you have installed.
- Whether **SkinShifter** is also present and its version (it is an optional integration and can affect behaviour).
- A clear description of what you expected and what happened, plus any relevant log output or crash report from the most minimal setup where the error occurs.

---

## Translations

Language files live at:

```
common/src/main/resources/assets/remorphed/lang/<locale>.json
```

Copy `en_us.json` as a starting point, rename it to your locale code (e.g. `de_de.json`), translate the values, and open a pull request. No Java knowledge is required for translations.

---

## Development Setup

### Prerequisites

| Tool | Minimum version |
|------|----------------|
| JDK  | 25             |
| Git  | any recent     |

IntelliJ IDEA with Gradle support is recommended.

### Cloning and building

```bash
git clone https://github.com/ToCraft/Remorphed.git
cd Remorphed

# Build both Fabric and NeoForge jars
./gradlew build
```

The build is powered by the [ModMaster](https://github.com/ToCraft/ModMaster) Gradle plugin. ReMorphed's runtime dependencies — CraftedCore, Woodwalkers and SkinShifter — are resolved from the ToCraft Maven (`https://maven.tocraft.dev/public`) using the versions declared in `gradle.properties`.

### Key `gradle.properties` values

| Property | Purpose |
|----------|---------|
| `minecraft` | Target Minecraft version |
| `mod_version` | Mod version to publish |
| `craftedcore_version` | Required CraftedCore version |
| `woodwalkers_version` | Required Woodwalkers (Walkers) version |
| `skinshifter_version` | Optional SkinShifter integration version |
| `fabric_loader` / `fabric` / `neoforge` | Loader and platform versions |
| `java` | Java toolchain version |

---

## Project Architecture

ReMorphed uses the same multi-module layout as Woodwalkers:

```
Remorphed/
├── common/          # Shared mod code (platform-agnostic)
├── fabric/          # Fabric-specific sources and entrypoints
├── neoforge/        # NeoForge-specific sources and entrypoints
├── assets/          # Repository artwork / screenshots
└── gradle.properties
```

Shared logic (kill detection, shape menu, unlock system) lives in `common`. Platform-specific wiring goes in `fabric` or `neoforge`. The [ModMaster](https://github.com/ToCraft/ModMaster) plugin handles the multiloader compilation automatically.

### Dependency chain

ReMorphed sits at the end of a long dependency chain:

```
ModMaster  →  CraftedCore  →  Woodwalkers & SkinShifter  →  ReMorphed
```

Code paths that interact with SkinShifter must be guarded so the mod still functions correctly when SkinShifter is absent.

---

## Making Changes

1. **Fork** the repository and create a feature branch off `main`.
2. Make your changes in the appropriate module (`common` for shared logic, `fabric`/`neoforge` for platform-specific code).
3. Build locally with `./gradlew build` to make sure everything compiles on both loaders.
4. Open a **pull request** against `main` with a clear description of what the PR does and why.

Keep pull requests focused — one logical change per PR makes review much easier.

---

## Updating to a New Minecraft Version

ReMorphed is at the bottom of a four-level dependency chain. Updates must happen in strict order — attempting any step before the previous one is released will cause dependency resolution failures.

```
1. ModMaster  →  2. CraftedCore  →  3. Woodwalkers & SkinShifter  →  4. ReMorphed
```

Once steps 1–3 are complete and new releases of ModMaster, CraftedCore, and Woodwalkers are available, update `gradle.properties` in this repo:

```properties
# Plugin version in build.gradle.kts
id("dev.tocraft.modmaster.root") version ("2.X-SNAPSHOT")

# In gradle.properties
minecraft=<new_mc_version>
supported_versions=<new_mc_version>
mappings=<new_parchment_date>
fabric=<new_fabric_api_version>
fabric_loader=<new_fabric_loader_version>
neoforge=<new_neoforge_version>
craftedcore_version=<new_craftedcore_version>
woodwalkers_version=<new_woodwalkers_version>
skinshifter_version=<new_skinshifter_version>  # if a new SkinShifter build is available
```

Then fix any compilation errors caused by Mojang or Woodwalkers API changes, run `./gradlew build`, and open a pull request.

---

## CI / GitHub Actions

ReMorphed uses two reusable composite actions maintained in the ToCraft organisation:

| Action | Purpose |
|--------|---------|
| [`modmaster-build-action`](https://github.com/ToCraft/modmaster-build-action) | Triggered on every push/PR — sets up JDK & Gradle, runs `./gradlew check build`, uploads compiled jars as an artifact |
| [`modmaster-release-action`](https://github.com/ToCraft/modmaster-release-action) | Triggered on releases — runs `./gradlew check build release`, publishing to CurseForge, Modrinth, and the ToCraft Maven, sending a Discord notification, and creating a GitHub Release |

You do not need to modify the workflow files for normal contributions. For CI failures on your PR, check the [Actions tab](https://github.com/ToCraft/Remorphed/actions) for the full build log.

---

## Code Style

- The codebase is written in **Java**.
- Follow the conventions already present in the files you are editing (indentation, naming, import ordering).
- Avoid large unrelated refactors in a feature PR — keep the diff readable.

---

## License

ReMorphed is licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**. By submitting a pull request you agree that your contribution will be made available under the same license.
