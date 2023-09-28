package tmg.gradle.plugin.gms.location

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
public abstract class TmgLocationFixPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withId("com.android.library") {
            project.logger.warn("[TMG] tmg.gms.location.fix plugin is most effective when applied to the Application module")
        }

        val ext = project.extensions.create("tmgLocationFix", TmgLocationFixExtension::class.java)

        project.extensions.configure(AndroidComponentsExtension::class.java) { androidComponents ->
            @Suppress("UnstableApiUsage")
            androidComponents.finalizeDsl {
                val selector = ext.variantSelector.getOrElse(androidComponents.selector().all())
                androidComponents.onVariants(selector) { variant ->
                    // Configure instrumentation
                    variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
                    variant.instrumentation.transformClassesWith(GmsLocationVisitorFactory::class.java, InstrumentationScope.ALL) { config ->
                        config.variantName.set(variant.name)
                        config.enabled.convention(ext.enabled)
                        config.strict.convention(ext.strict)
                        config.forceApi.convention(ext.forceApi.map { it.also { check(it == 20 || it == 21) } })
                    }

                    // Add the strict constraint only if configured
                    if (ext.strict.getOrElse(false) && ext.forceApi.isPresent) {
                        val forcing = ext.forceApi.get()
                        val constraint = project.dependencies.constraints.createStrictConstraint(forcing, variant.name)
                        variant.compileConfiguration.dependencyConstraints.add(constraint)
                    }

                    // Add task actions for reporting
                    val transformTask = "transform${variant.name.replaceFirstChar { it.uppercaseChar() }}ClassesWithAsm"
                    // FIXME: task name might change
                    project.tasks.maybeNamed(transformTask) {
                        val variantCallsites = callsites.computeIfAbsent(variant.name) { mutableSetOf() }

                        it.doLast(ReportCallsitesAction(ext, variantCallsites))
                        if (ext.strict.getOrElse(false) && ext.forceApi.isPresent) {
                            it.doLast(StrictAction(ext, variantCallsites))
                        }
                    }
                }
            }
        }
    }
}
