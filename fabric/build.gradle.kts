import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("dev.tocraft.modmaster.fabric")
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

tasks.getByName<RemapJarTask>("remapJar") {
    atAccessWideners.add("remorphed.accessWidener")
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>

    filesMatching("fabric.mod.json") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

dependencies {
    modApi("dev.tocraft:craftedcore-fabric:${rootProject.properties["craftedcore_version"]}") {
        exclude("net.fabricmc.fabric-api")
        exclude("com.terraformersmc")
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers-fabric:${rootProject.properties["woodwalkers_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
    modApi("dev.tocraft:skinshifter-fabric:${rootProject.properties["skinshifter_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
}