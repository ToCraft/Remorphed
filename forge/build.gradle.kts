plugins {
    id("dev.tocraft.modmaster.forge")
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST")val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>

    filesMatching("META-INF/mods.toml") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

loom {
    forge {
        mixinConfigs.add("remorphed.mixins.json")
    }
}

dependencies {
    modApi("dev.tocraft:craftedcore-forge:${parent!!.name}-${rootProject.properties["craftedcore_version"]}") {
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers-forge:${parent!!.name}-${rootProject.properties["woodwalkers_version"]}")
    modApi("dev.tocraft:skinshifter-forge:${parent!!.name}-${rootProject.properties["skinshifter_version"]}")
}
