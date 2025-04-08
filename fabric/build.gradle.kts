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

dependencies {
    modApi("dev.tocraft:craftedcore-fabric:${parent!!.name}-${rootProject.properties["craftedcore_version"]}") {
        exclude("net.fabricmc.fabric-api")
        exclude("com.terraformersmc")
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers-fabric:${parent!!.name}-${rootProject.properties["woodwalkers_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
    modApi("dev.tocraft:skinshifter-fabric:${parent!!.name}-${rootProject.properties["skinshifter_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
}