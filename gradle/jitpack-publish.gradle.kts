/**
 * Shared JitPack / Maven Local publishing for Android library modules.
 * Applied from module build.gradle.kts via: apply(from = rootProject.file("gradle/jitpack-publish.gradle.kts"))
 *
 * Requires android { publishing { singleVariant("release") { withSourcesJar() } } } in the module.
 */
apply(plugin = "maven-publish")

group = rootProject.property("GROUP").toString()
version = rootProject.property("VERSION_NAME").toString()

afterEvaluate {
    extensions.configure<PublishingExtension>("publishing") {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name.set(project.name)
                    description.set(
                        when (project.name) {
                            "abtesting" -> "AbTestingKit core API (backend-agnostic)"
                            "abtesting-firebase" -> "AbTestingKit Firebase Remote Config provider"
                            else -> "AbTestingKit module ${project.name}"
                        },
                    )
                    url.set(rootProject.property("POM_URL").toString())
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        url.set(rootProject.property("POM_URL").toString())
                        connection.set(rootProject.property("POM_SCM_CONNECTION").toString())
                        developerConnection.set(rootProject.property("POM_SCM_CONNECTION").toString())
                    }
                }
            }
        }
    }
}
