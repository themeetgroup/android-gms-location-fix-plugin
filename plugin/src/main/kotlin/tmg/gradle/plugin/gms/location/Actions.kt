package tmg.gradle.plugin.gms.location

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.logging.Logging

internal class ReportCallsitesAction(
    private val ext: TmgLocationFixExtension,
    private val callsites: Set<CallsiteData>,
) : Action<Task> {
    private val logger = Logging.getLogger(javaClass)
    override fun execute(t: Task) {
        logger.lifecycle("[TMG] Discovered call-sites:")
        val forcing = ext.forceApi.orNull
        callsites
            .sortedBy { it.api }
            .forEach { (caller, api) ->
                val note = if (forcing == null || forcing == api) "" else " [*]"
                logger.lifecycle("      - v$api$note: $caller")
            }
    }
}

internal class StrictAction(
    private val ext: TmgLocationFixExtension,
    private val callsites: Set<CallsiteData>,
) : Action<Task> {
    override fun execute(t: Task) {
        val forcing = ext.forceApi.get()
        val good = callsites.filter { it.api == forcing }
        val bad = callsites.filter { it.api != forcing }

        if (bad.isEmpty() && good.isNotEmpty()) {
            throw GradleException(
                "[TMG] tmg.gms.location.fix plugin might not be needed anymore.\n" +
                        "      ${good.size} callers were already using the configured forceApi ($forcing), and ${bad.size} were fixed."
            )
        }
    }
}
