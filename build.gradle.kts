plugins {
    id("dev.tocraft.modmaster.version")
}

ext {
    val modMeta = mutableMapOf<String, Any>()
    modMeta["minecraft_version"] = project.name
    modMeta["version"] = version
    modMeta["craftedcore_version"] = project.properties["craftedcore_version"] as String
    modMeta["woodwalkers_version"] = project.properties["woodwalkers_version"] as String
    modMeta["skinshifter_version"] = project.properties["skinshifter_version"] as String

    set("mod_meta", modMeta)
}
