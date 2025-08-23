import net.fabricmc.loom.task.RemapJarTask
import org.gradle.kotlin.dsl.getByName

plugins {
    id("dev.tocraft.modmaster.neoforge")
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

tasks.getByName<RemapJarTask>("remapJar") {
    atAccessWideners.add("remorphed.accessWidener")
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

dependencies {
    modApi("dev.tocraft:craftedcore-neoforge:${rootProject.properties["craftedcore_version"]}") {
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers-neoforge:${rootProject.properties["woodwalkers_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
    modApi("dev.tocraft:skinshifter-neoforge:${rootProject.properties["skinshifter_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
}