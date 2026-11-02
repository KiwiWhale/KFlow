package io.github.kiwiwhale.kiwiflow.annotation.metadata

internal class WorkflowMetadata(
    val name: String,
    steps: List<StepMetadata>
) {
    val steps: List<StepMetadata> = steps.toList()
}