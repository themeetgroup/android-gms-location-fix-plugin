# `tmg.gms.location.fix` plugin

This plugin applies a transformation to code that was compiled against one
version of play-services-location, so it becomes compatible with another
version. In particular, this resolves a binary-incompatible change that
Google introduced between v20 and v21, resulting in a runtime crash due to
`IncompatibleClassChangeError`, caused by a dependency that was compiled
against v20 using bytecode specific to invoking methods on a class instance,
while v21 changes that class to an interface. As a result, the opcode used
in the v20-compiled dependency is no longer compatible with the v21 runtime.

Google's solution so far has been to tell people "that's expected, just update
to v21", ignoring the fact that this also affects third party dependencies
and SDKs that the consuming app does not control.

## Usage

### Add the Github Packages repository

```
// settings.gradle.kts
pluginManagement {
  repositories {
    maven {
      url = uri("https://maven.pkg.github.com/themeetgroup/android-gms-location-fix-plugin")
      credentials {
        username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
      }
    }
  }
}
```

NOTE: as of now, Github Packages does not support anonymous read access, so
in order to access this, you'll need to use a Github Personal Access Token.

See https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry

### Apply the plugin

```
// app/build.gradle.kts
plugins {
  // ...
  id("tmg.gms.location.fix") version "..."
}

tmgLocationFix {
    enabled.set(true)
    strict.set(true)
    forceApi.set(21)
}

implementation("com.google.android.gms:play-services-location") {
    // If any dependencies had reject() constraints on 21+, this will bypass those:
    version { strictly("21.0.1") }
}
```

Note that the plugin is most effective in the Application module, so that it
can transform all of the application's dependency classes at once.

See documentation for
the [`tmgLocationFix` extension](https://github.com/themeetgroup/android-gms-location-fix-plugin/blob/main/plugin/src/main/kotlin/tmg/gradle/plugin/gms/location/TmgLocationFixExtension.kt).
