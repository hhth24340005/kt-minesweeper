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

  jvm()
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(25)
    @Suppress("UnstableApiUsage")
    vendor = JvmVendorSpec.JETBRAINS
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlin.logging)
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
        implementation(libs.compose.material3)
      }
    }

    jvmMain {
      dependencies {
        implementation(compose.desktop.currentOs)
        runtimeOnly(libs.logback.classic)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Deb)
      modules("java.instrument", "jdk.unsupported")
    }
  }
}
