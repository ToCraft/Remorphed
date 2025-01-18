import dev.tocraft.gradle.preprocess.tasks.ApplyPreProcessTask
import java.util.*

plugins {
    id("dev.tocraft.modmaster.common")
}

val ccversion = (parent!!.ext["props"] as Properties)["craftedcore"] as String
val sversion = (parent!!.ext["props"] as Properties)["skinshifter"] as String
dependencies {
    modApi("dev.tocraft:craftedcore:${ccversion}-${rootProject.properties["craftedcore_version"]}") {
        exclude("me.shedaniel.cloth")
    }
    modApi("dev.tocraft:walkers:${parent!!.name}-${rootProject.properties["woodwalkers_version"]}")
    modApi("dev.tocraft:skinshifter:${sversion}-${rootProject.properties["skinshifter_version"]}")
}
