plugins {
    id("dev.tocraft.modmaster.common")
}

dependencies {
    modApi("dev.tocraft:craftedcore:${parent!!.name}-${rootProject.properties["craftedcore_version"]}") {
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers:${parent!!.name}-${rootProject.properties["woodwalkers_version"]}")
    modApi("dev.tocraft:skinshifter:${parent!!.name}-${rootProject.properties["skinshifter_version"]}")
}