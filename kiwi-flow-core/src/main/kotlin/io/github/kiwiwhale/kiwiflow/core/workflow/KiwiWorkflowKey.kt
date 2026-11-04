package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiregistry.RegistryId
import io.github.kiwiwhale.kiwiregistry.RegistryKey

/**
 * Typed identifier for a KiwiWorkflow.
 *
 * @param I Workflow input type.
 * @param O Workflow output type.
 */
class KiwiWorkflowKey<I, O>(
    val name: String
) {

    val id: RegistryId =
        RegistryId(NAMESPACE, name)

    internal val registryKey: RegistryKey<KiwiWorkflow<*, *>> =
        RegistryKey.of(
            id,
            KiwiWorkflow::class
        )

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }

    companion object {
        const val NAMESPACE = "kiwi-flow"

        fun <I, O> of(name: String): KiwiWorkflowKey<I, O> =
            KiwiWorkflowKey(name)
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is KiwiWorkflowKey<*, *> && id == other.id)

    override fun hashCode(): Int =
        id.hashCode()

    override fun toString(): String =
        "KiwiWorkflowKey(id='$id')"
}
