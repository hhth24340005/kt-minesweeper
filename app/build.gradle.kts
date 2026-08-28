import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.hotReload)
}

repositories {
  mavenCentral()
  google()
}

kotlin {
  explicitApi()
  compilerOptions {
    freeCompilerArgs.add("-Xexplicit-context-arguments")
  }

  jvm()
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(25)
    @Suppress("UnstableApiUsage")
    vendor = JvmVendorSpec.JETBRAINS
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
        implementation(libs.compose.material3)
        implementation(libs.compose.components.resources)
      }
    }

    jvmMain {
      dependencies {
        implementation(compose.desktop.currentOs)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "io.github.hhth24340005.minesweeper.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Deb)
      modules("java.instrument", "jdk.unsupported")
    }
  }
}

compose.resources {
  packageOfResClass = "io.github.hhth24340005.minesweeper.resources"
  generateResClass = auto
}
