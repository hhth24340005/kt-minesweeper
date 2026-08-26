import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

repositories {
  mavenCentral()
}

kotlin {
  explicitApi()

  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  jvm {
    val main = "MainKt"
    binaries {
      executable {
        mainClass = main
      }
    }
    mainRun {
      mainClass = main
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlin.logging)
      }
    }

    jvmMain {
      dependencies {
        runtimeOnly(libs.logback.classic)
      }
    }
  }
}
