import java.util.*

plugins {
    id("dev.tocraft.modmaster.common")
}

val sversion = (parent!!.ext["props"] as Properties)["skinshifter"] as String
dependencies {
    modApi("dev.tocraft:craftedcore:${parent!!.name}-${rootProject.properties["craftedcore_version"]}") {
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers:${parent!!.name}-${rootProject.properties["woodwalkers_version"]}")
    modApi("dev.tocraft:skinshifter:${sversion}-${rootProject.properties["skinshifter_version"]}")
}
