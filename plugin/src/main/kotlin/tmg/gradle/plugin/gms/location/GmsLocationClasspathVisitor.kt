package tmg.gradle.plugin.gms.location

import org.gradle.api.Action
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal class GmsLocationClasspathVisitor(
    delegate: ClassVisitor,
    private val asmVersion: Int,
    private val onClasspathApiDiscovered: Action<Int?>,
) : ClassVisitor(asmVersion, delegate) {
    @field:Transient
    private val logger: Logger = Logging.getLogger(javaClass)

    @field:Transient
    private var discoveredClasspathApi: Int? = null
        set(value) {
            if (field != value && value != null) {
                if (field != null) {
                    logger.warn("[TMG] WARNING: discovered multiple gms.locations classpath versions! $field ~> $value")
                }
                field = value
            }
        }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val delegate = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "getFusedLocationProviderClient") {
            return object : MethodVisitor(asmVersion, delegate) {
                override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
                    if (opcode == Opcodes.INVOKESPECIAL && name == "<init>") {
                        discoveredClasspathApi = if (owner == "com/google/android/gms/location/FusedLocationProviderClient") {
                            // LocationServices instantiates the FusedLocationProviderClient class
                            20
                        } else {
                            // LocationServices instantiates a different class
                            21
                        }
                    }
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
            }
        }
        return delegate
    }

    override fun visitEnd() {
        onClasspathApiDiscovered.execute(discoveredClasspathApi)
        super.visitEnd()
    }
}
