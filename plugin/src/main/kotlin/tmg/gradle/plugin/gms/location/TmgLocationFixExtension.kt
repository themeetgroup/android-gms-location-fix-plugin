package tmg.gradle.plugin.gms.location

import com.android.build.api.variant.VariantSelector
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

public abstract class TmgLocationFixExtension @Inject constructor(
    objects: ObjectFactory,
) {
    /**
     * Behavior must be enabled explicitly.
     */
    @get:Input
    public val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * Whether the plugin's behavior will be applied strictly. Defaults to false.
     *
     * If set to true, the plugin will add dependency constraints to ensure that the configured [forceApi] version matches the actual dependency
     * version on the classpath. It will also raise an error if/when the plugin is no longer effective, as an indication that it can or should be
     * removed.
     */
    @get:Input
    public val strict: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * Optionally set the play-services-location syntax to force.
     * * Set to `20` to force-downgrade any uses of FusedLocationClientProvider back to an instance class method.
     * * Set to `21` to force-upgrade any uses of FusedLocationClientProvider from a class to an interface.
     * * Any other value is rejected.
     * * If not set, the bytecode will NOT be modified.
     */
    @get:Input
    @get:Optional
    public val forceApi: Property<Int> = objects.property(Int::class.java)

    /**
     * Optional variant selector to choose which variants will be modified.
     *
     * If not set, defaults to [`androidComponents.selector().all()`][com.android.build.api.variant.VariantSelector.all].
     * Other options include [`withBuildType()`][com.android.build.api.variant.VariantSelector.withBuildType] and
     * [`withFlavor()`][com.android.build.api.variant.VariantSelector.withFlavor].
     *
     * @see com.android.build.api.variant.VariantSelector
     */
    @get:Input
    @get:Optional
    public val variantSelector: Property<VariantSelector> = objects.property(VariantSelector::class.java)
}
