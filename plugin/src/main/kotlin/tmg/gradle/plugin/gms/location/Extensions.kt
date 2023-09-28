package tmg.gradle.plugin.gms.location

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.api.artifacts.dsl.DependencyConstraintHandler
import org.gradle.api.tasks.TaskCollection

internal fun <T : Task> TaskCollection<T>.maybeNamed(name: String, action: Action<T>) {
    runCatching { named(name, action) }
        .onFailure {
            // Task was not ready yet, wait for it to be added.
            whenTaskAdded {
                if (it.name == name) {
                    action.execute(it)
                }
            }
        }
}

internal fun DependencyConstraintHandler.createStrictConstraint(forcing: Int, variantName: String): DependencyConstraint {
    return create("com.google.android.gms:play-services-location") { c ->
        c.because("[TMG] tmg.gms.location.fix configured with forceApi=$forcing on variant `$variantName`")
        when (forcing) {
            // If forcing v20, reject anything >= 21
            20 -> c.version { it.reject("[21.+,)") }
            // If forcing v21, reject anything < 21
            21 -> c.version { it.reject("[0,21.0)") }
        }
    }
}
