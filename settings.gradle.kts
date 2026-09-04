dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
    google()
  }
}

rootProject.name = "minesweeper"

include(":app")
include(":logic")
include(":lib")
