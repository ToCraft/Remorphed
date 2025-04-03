import java.util.*

plugins {
    id("dev.tocraft.modmaster.neoforge")
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>

    filesMatching("META-INF/mods.toml") {
        expand(modMeta)
    }

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(modMeta)
    }


    outputs.upToDateWhen { false }
}

val sversion = (parent!!.ext["props"] as Properties)["skinshifter"] as String
dependencies {
    modApi("dev.tocraft:craftedcore-neoforge:${parent!!.name}-${rootProject.properties["craftedcore_version"]}") {
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers-neoforge:${parent!!.name}-${rootProject.properties["woodwalkers_version"]}")
    modApi("dev.tocraft:skinshifter-neoforge:${sversion}-${rootProject.properties["skinshifter_version"]}")
}