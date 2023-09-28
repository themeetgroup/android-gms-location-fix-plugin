package tmg.gradle.plugin.gms.location

import com.android.build.api.instrumentation.ClassContext
import org.gradle.api.Action
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal class GmsLocationCallsiteVisitor(
    private val classContext: ClassContext,
    private val asmVersion: Int,
    params: LocationCompatParams,
    delegate: ClassVisitor,
    private val onCallsite: Action<CallsiteData>,
) : ClassVisitor(asmVersion, delegate) {

    private val forcingApi: Int? = params.forceApi.orNull

    @field:Transient
    private val logger: Logger = Logging.getLogger(javaClass)

    override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val delegate = super.visitMethod(access, name, desc, signature, exceptions)
        val callSite = classContext.currentClassData.className + "." + name
        return object : MethodVisitor(asmVersion, delegate) {
            override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
                if (owner != "com/google/android/gms/location/FusedLocationProviderClient") {
                    return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }

                val api = if (isInterface) 21 else 20
                assert(isInterface == (opcode == Opcodes.INVOKEINTERFACE))

                onCallsite.execute(CallsiteData(callSite, api))

                when (forcingApi) {
                    null -> {
                        logger.info("[TMG] Not rewriting gms.location call-sites...")
                        return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }

                    api -> {
                        logger.info("[TMG] gms.location call-site already matches v$api.")
                        return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }

                    20 -> {
                        logger.lifecycle("[TMG] Rewriting gms.location call-site `$callSite` to v20")
                        return super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false)
                    }

                    21 -> {
                        logger.lifecycle("[TMG] Rewriting gms.location call-site `$callSite` to v21")
                        return super.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, name, descriptor, true)
                    }

                    else -> {
                        error("[TMG] Invalid value for `tmgLocationFix.forceApi`: $forcingApi")
                    }
                }
            }
        }
    }
}
