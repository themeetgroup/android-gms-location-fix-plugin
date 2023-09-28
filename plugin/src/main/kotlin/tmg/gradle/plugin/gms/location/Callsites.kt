package tmg.gradle.plugin.gms.location


internal data class CallsiteData(
    val caller: String,
    val api: Int,
)

internal val callsites = mutableMapOf<String, MutableSet<CallsiteData>>()
