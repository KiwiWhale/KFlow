package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiregistry.core.RegistryId
import io.github.kiwiwhale.kiwiregistry.core.RegistryKey

/**
 * Typed identifier for a [KiwiWorkflow].
 *
 * A workflow key uniquely identifies a workflow by its name
 * and preserves its input and output types at compile time.
 *
 * @param I Workflow input type.
 * @param O Workflow output type.
 */
class KiwiWorkflowKey<I, O>(
    val name: String
) {

    val id: RegistryId = RegistryId(NAMESPACE, name)

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }

    companion object {
        const val NAMESPACE = "kiwi-flow"

        fun <I, O> of(name: String): KiwiWorkflowKey<I, O> =
            KiwiWorkflowKey(name)

        val WORKFLOWS: RegistryKey<String, KiwiWorkflow<*, *>> =
            RegistryKey.simple(
                RegistryId(NAMESPACE, "workflows")
            )
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is KiwiWorkflowKey<*, *> && id == other.id)

    override fun hashCode(): Int =
        id.hashCode()

    override fun toString(): String =
        "KiwiWorkflowKey(id='$id')"
}
