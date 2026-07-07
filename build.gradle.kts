plugins {
    application
}

repositories {
    mavenCentral()
    maven(url = "$rootDir/gradle/repository")

}

dependencies {
    implementation(libs.tnoodle.scrambles)
    implementation(libs.h2.database)
    implementation(libs.bundles.batik)
    implementation(libs.miglayout.swing)
    implementation(libs.bld.analyze)
    implementation(libs.alglib)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.puzzletimer.Main"
}
