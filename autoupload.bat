@echo off
set /p version=Please enter the version (needed for github, also check gradle.properties and mods.toml): 

:: add mcversion here
set mcversions="1.16.5";"1.18.2";"1.19.4";"1.20.1";"1.20.2"
:: add loader here
set modloader="fabric";"forge"

:: loops
for %%m in (%mcversions%) DO (
    git checkout "%%m"
    gh release create "%version%-%%m" --generate-notes

    .\gradlew publish
    for %%l in (%modloader%) DO (
        call .\gradlew %%l:build
        .\gradlew %%l:modrinth
        .\gradlew %%l:curseforge
        move "%%l\build\libs\ycdm-%version%-%%l.jar" "%%l\build\ycdm-%%m-%%l-%version%.jar"
        rmdir /s /q "%%l\build\libs"
        gh release upload "%version%-%%m" "%%l\build\ycdm-%%m-%%l-%version%.jar"
    )
)
