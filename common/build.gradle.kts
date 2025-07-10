import java.util.*

plugins {
    id("dev.tocraft.modmaster.common")
}

dependencies {
    modApi("dev.tocraft:craftedcore:${rootProject.properties["craftedcore_version"]}") {
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers:${rootProject.properties["woodwalkers_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
    modApi("dev.tocraft:skinshifter:${rootProject.properties["skinshifter_version"]}") {
        exclude("dev.tocraft", "craftedcore")
    }
}
