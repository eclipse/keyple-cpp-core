///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    java
    id("com.diffplug.spotless") version "6.25.0"
    id("org.sonarqube") version "6.0.1.5171"
    id("com.vanniktech.maven.publish") version "0.30.0"
    jacoco
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

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

java {
    withJavadocJar()
    withSourcesJar()
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
            }
        }
    }
}
