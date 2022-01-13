import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
  NOTE use the project git@github.com:adligo/2022.adligo.org.git to build this
*/

plugins {
  `maven-publish`
  eclipse
  java
  signing
}

sourceSets {
  main {
    java {
      srcDirs("src")
    }
  }
}

fun getProp(key: String, default: String): String {
  var r : String = default
  if (project.hasProperty(key)) {
    r = project.getProperties().get(key) as String
  }
  return r
}

fun getTag(): String {
  val current = LocalDateTime.now()
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
  val snapshot = "SNAPSHOT_" + current.format(formatter)
  //println(snapshot)
  return getProp("tag", snapshot)
}

// gradle publishToMavenLocal 
// or with the git tag
//  gradle publishToMavenLocal -Ptag=vg0_1 
publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "org.adligo"
      artifactId = "ctx4jse"
      version = getTag()
      from(components["java"])
    }
  }
}
