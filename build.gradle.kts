///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    java
    id("com.diffplug.spotless") version "6.25.0"
    id("org.sonarqube") version "3.1"
    id("com.vanniktech.maven.publish") version "0.30.0"
    jacoco
}
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.keyple:keyple-gradle:0.2.+") { isChanging = true }
    }
}
apply(plugin = "org.eclipse.keyple")

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.DEFAULT)
    signAllPublications()

    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
    )

    pom {
        name.set(project.property("title").toString())
        description.set(project.property("description").toString())
        url.set("https://github.com/eclipse-keyple/${project.name}")
        licenses {
            license {
                name.set("Eclipse Public License - v 2.0")
                url.set("https://www.eclipse.org/legal/epl-2.0/")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("keyple")
                name.set("Keyple Contributors")
                email.set("keyple-dev@eclipse.org")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/eclipse-keyple/${project.name}.git")
            developerConnection.set("scm:git:ssh://github.com:eclipse-keyple/${project.name}.git")
            url.set("https://github.com/eclipse-keyple/${project.name}/tree/main")
        }
    }
}

val javaSourceLevel: String by project
val javaTargetLevel: String by project
java {
    sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
    targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    println("Compiling Java $sourceCompatibility to Java $targetCompatibility.")
    withJavadocJar()
    withSourcesJar()
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    spotless {
        java {
            target("src/**/*.java")
            licenseHeaderFile("${project.rootDir}/LICENSE_HEADER")
            importOrder("java", "javax", "org", "com", "")
            removeUnusedImports()
            googleJavaFormat()
        }
    }
    test {
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy("jacocoTestReport")
    }
    jacocoTestReport {
        dependsOn("test")
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
        }
    }
    sonarqube {
        properties {
            property("sonar.projectKey", "eclipse_" + project.name)
            property("sonar.organization", "eclipse")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.login", System.getenv("SONAR_LOGIN"))
            System.getenv("BRANCH_NAME")?.let {
                if (!"main".equals(it)) {
                    property("sonar.branch.name", it)
                }
            }
        }
    }
}
