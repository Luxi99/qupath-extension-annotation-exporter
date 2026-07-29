plugins {
    // Support writing the extension in Groovy (remove this if you don't want to)
    groovy
    // To optionally create a shadow/fat jar that bundle up any non-core dependencies
    id("com.gradleup.shadow") version "8.3.5"
    // QuPath Gradle extension convention plugin
    id("qupath-conventions")
}

// TODO: Configure your extension here (please change the defaults!)
qupathExtension {
    name = "qupath-extension-annotation-exporter"
    group = "io.github.qupath"
    version = "0.1.1"
    description = "This extension allows you to export manual annotations from images of the current project."
    automaticModule = "io.github.qupath.extension.template"
}

repositories {
    // repository già esistenti (scijava, mavenCentral, ecc.)
    mavenCentral()
    maven { url = uri("https://maven.scijava.org/content/repositories/releases") }
    maven { url = uri("https://jitpack.io") }
}

tasks.test {
    useJUnitPlatform {
        if (System.getenv("CI") != null) {
            excludeTags("requires-javafx")
        }
    }
}

// TODO: Define your dependencies here
dependencies {

    implementation("com.github.Luxi99:annotation-exporter-core:0.1.1")

    // Main dependencies for most QuPath extensions
    shadow(libs.bundles.qupath)
    shadow(libs.bundles.logging)
    shadow(libs.qupath.fxtras)

    // If you aren't using Groovy, this can be removed
    shadow(libs.bundles.groovy)

    // For testing
    testImplementation(libs.bundles.qupath)
    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.openjfx:javafx-base:21:linux")
    testImplementation("org.openjfx:javafx-graphics:21:linux")
    testImplementation("org.openjfx:javafx-controls:21:linux")
    testImplementation("org.openjfx:javafx-fxml:21:linux")
    testImplementation("org.openjfx:javafx-swing:21:linux")
    testImplementation("org.hamcrest:hamcrest:2.2")
}