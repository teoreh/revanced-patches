group = "app.revanced"

patches {
    about {
        name = "teoreh ReVanced Patches"
        description = "Custom patches for own usage"
        source = "git@github.com:teoreh/revanced-patches.git"
        author = "teoreh"
        contact = ""
        website = ""
        license = "GNU General Public License v3.0"
    }
}

dependencies {
    // Required due to smali, or build fails. Can be removed once smali is bumped.
    implementation(libs.guava)

    implementation(libs.apksig)

    // Android API stubs defined here.
    compileOnly(project(":patches:stub"))
}

tasks {
    register<JavaExec>("preprocessCrowdinStrings") {
        description = "Preprocess strings for Crowdin push"

        dependsOn(compileKotlin)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.revanced.util.CrowdinPreprocessorKt")

        args = listOf(
            "src/main/resources/addresources/values/strings.xml",
            // Ideally this would use build/tmp/crowdin/strings.xml
            // But using that does not work with Crowdin pull because
            // it does not recognize the strings.xml file belongs to this project.
            "src/main/resources/addresources/values/strings.xml"
        )
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

apiValidation {
    validationDisabled = true
}

// Disable all signing tasks on the CI runner
tasks.configureEach {
    if (name.contains("sign", ignoreCase = true)) {
        enabled = false
    }
}