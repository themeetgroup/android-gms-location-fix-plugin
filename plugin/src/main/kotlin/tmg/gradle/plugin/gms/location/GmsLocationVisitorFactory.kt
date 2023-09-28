package tmg.gradle.plugin.gms.location

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.objectweb.asm.ClassVisitor
import java.io.Serializable

internal interface LocationCompatParams : InstrumentationParameters {
    @get:Input
    val enabled: Property<Boolean>

    @get:Input
    val variantName: Property<String>

    @get:Input
    @get:Optional
    val forceApi: Property<Int>

    @get:Input
    @get:Optional
    val strict: Property<Boolean>
}

internal abstract class GmsLocationVisitorFactory : AsmClassVisitorFactory<LocationCompatParams>, Serializable {
    private val logger: Logger get() = Logging.getLogger(javaClass)

    private val params
        get() = parameters.get()

    private val asmVersion
        get() = instrumentationContext.apiVersion.get()

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor,
    ): ClassVisitor {
        return if (classContext.currentClassData.className == "com.google.android.gms.location.LocationServices") {
            GmsLocationClasspathVisitor(nextClassVisitor, asmVersion) { api: Int? ->
                if (api == null) {
                    val msg = "[TMG] Unable to discover gms.location classpath API version!"
                    if (params.strict.getOrElse(false)) {
                        logger.error(msg)
                        throw AssertionError(msg)
                    }
                    logger.warn(msg)
                } else {
                    if (params.forceApi.isPresent) {
                        assert(params.forceApi.get() == api) {
                            "[TMG] Discovered gms.location classpath is v$api, but forceApi=${params.forceApi.get()}! These should match."
                                .also { logger.error(it) }
                        }
                    }
                    logger.lifecycle("[TMG] Discovered gms.location classpath v$api")
                }
            }
        } else {
            GmsLocationCallsiteVisitor(classContext, asmVersion, params, nextClassVisitor) { callsite ->
                logger.info("[TMG] Discovered play-services-location call-site ${callsite.caller} => API ${callsite.api}")
                callsites.computeIfAbsent(params.variantName.get()) { mutableSetOf() }
                    .add(callsite)
            }
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        if (classData.className == "com.google.android.gms.location.LocationServices") {
            // Needed to discover classpath version
            return true
        } else if (classData.className == "com.google.android.gms.location.FusedLocationProviderClient") {
            // We don't need to look at everything the client is doing (in v20)
            return false
        }
        return params.enabled.getOrElse(false)
    }
}
