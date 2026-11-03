package io.github.kiwiwhale.kiwiflow.annotation.metadata

/**
 * Represents the resolved metadata of an annotated workflow.
 *
 * Contains the workflow name and its ordered step definitions.
 *
 * The supplied step collection is defensively copied to prevent
 * modifications to the original collection from affecting
 * this metadata instance.
 *
 * @property name Resolved workflow name.
 * @property steps Immutable view of the workflow's ordered steps.
 *
 * @see StepMetadata
 */
internal class WorkflowMetadata(
    val name: String,
    steps: List<StepMetadata>
) {

    /**
     * Ordered workflow steps.
     *
     * The collection is copied during construction.
     */
    val steps: List<StepMetadata> = steps.toList()

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }
}
