plugins {
    id("dev.tocraft.modmaster.fabric")
}

dependencies {
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric")}")

    // MixinExtras bundled with the mod
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${property("mixinextras_version")}")!!)!!)

    implementation("dev.tocraft:craftedcore-fabric:${property("craftedcore_version")}") {
        exclude("net.fabricmc.fabric-api")
        exclude(group = "me.shedaniel.cloth")
        exclude(group = "com.terraformersmc")
    }
    implementation("dev.tocraft:walkers-fabric:${property("woodwalkers_version")}") {
        exclude("dev.tocraft", "craftedcore")
    }
    implementation("dev.tocraft:skinshifter-fabric:${property("skinshifter_version")}") {
        exclude("dev.tocraft", "craftedcore")
    }
}

tasks.processResources {
    val mcVersion = project.property("minecraft")
    val craftedcoreVersion = project.property("craftedcore_version")
    val walkersVersion = project.property("woodwalkers_version")
    val skinshifterVersion = project.property("skinshifter_version")
    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to project.version,
            "minecraft_version" to mcVersion,
            "craftedcore_version" to craftedcoreVersion,
            "woodwalkers_version" to walkersVersion,
            "skinshifter_version" to skinshifterVersion
        ))
    }
}
