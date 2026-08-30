plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
        implementation(libs.kotlinx.collections.immutable)
        api(libs.compose.runtime)
      }
    }
  }
}
