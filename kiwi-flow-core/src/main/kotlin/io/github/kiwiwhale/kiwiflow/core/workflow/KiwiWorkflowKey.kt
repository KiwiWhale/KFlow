package io.github.kiwiwhale.kiwiflow.core.workflow

/**
 * A typed identifier for a KiwiWorkflow.
 *
 * A workflow key associates a unique name with the expected
 * input and output types of a workflow.
 *
 * The generic parameters provide compile-time type information.
 * They do not guarantee runtime type safety because of JVM
 * type erasure.
 *
 * Two keys are considered equal when their names are equal,
 * regardless of their generic type arguments.
 *
 * @param I Expected workflow input type.
 * @param O Expected workflow output type.
 * @property name Unique workflow name.
 *
 * @see KiwiWorkflow
 */
class KiwiWorkflowKey<I, O>(
    val name: String
) {

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }

    /**
     * Compares this key with another object.
     *
     * Key equality is determined exclusively by the workflow name.
     *
     * @param other Object to compare.
     * @return True if both objects are workflow keys with the same name.
     */
    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is KiwiWorkflowKey<*, *> && name == other.name)

    /**
     * Returns the hash code derived from the workflow name.
     *
     * @return Hash code of the workflow name.
     */
    override fun hashCode(): Int =
        name.hashCode()

    /**
     * Returns a readable representation of this workflow key.
     *
     * @return String representation.
     */
    override fun toString(): String =
        "KiwiWorkflowKey(name='$name')"
}
