package io.github.kiwiwhale.kiwiflow.annotation.metadata

/**
 * Contains the resolved metadata of an annotated workflow.
 *
 * The step collection is defensively copied to prevent external
 * modifications to the metadata.
 *
 * @property name Resolved workflow name.
 * @property steps Ordered collection of workflow steps.
 */
internal class WorkflowMetadata(
    val name: String,
    steps: List<StepMetadata>
) {
    val steps: List<StepMetadata> = steps.toList()
}