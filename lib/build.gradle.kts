plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose)
}

kotlin {
  explicitApi()

  jvm()
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(21)
    @Suppress("UnstableApiUsage")
    vendor = JvmVendorSpec.JETBRAINS
  }

  sourceSets {
    commonMain {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.compose.foundation)
        api(libs.compose.runtime)
      }
    }
  }
}
