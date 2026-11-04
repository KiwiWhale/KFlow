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
) : RegistryKey<KiwiWorkflow<*, *>> {

    override val id = RegistryId(NAMESPACE, name)

    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is KiwiWorkflowKey<*, *> && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "KiwiWorkflowKey(id='$id')"

    private companion object {
        const val NAMESPACE = "kiwi-flow"
    }
}
