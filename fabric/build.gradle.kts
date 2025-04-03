import java.util.*

plugins {
    id("dev.tocraft.modmaster.fabric")
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>
    //inputs.properties.putAll(modMeta)

    filesMatching("fabric.mod.json") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

val sversion = (parent!!.ext["props"] as Properties)["skinshifter"] as String
dependencies {
    modApi("dev.tocraft:craftedcore-fabric:${parent!!.name}-${rootProject.properties["craftedcore_version"]}") {
        exclude("net.fabricmc.fabric-api")
        exclude("com.terraformersmc")
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers-fabric:${parent!!.name}-${rootProject.properties["woodwalkers_version"]}")
    modApi("dev.tocraft:skinshifter-fabric:${sversion}-${rootProject.properties["skinshifter_version"]}")
}