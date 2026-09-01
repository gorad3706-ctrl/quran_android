plugins {
  id("quran.android.application")
}

android {
  namespace = "sn.reussite.bfembac"

  defaultConfig {
    applicationId = "sn.reussite.bfembac"
    versionCode = 1
    versionName = "1.0.0"
  }
}

dependencies {
  implementation(project(":common:education"))
  implementation(libs.androidx.appcompat)
}
