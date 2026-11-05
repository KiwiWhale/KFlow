package io.github.kiwiwhale.kiwiflow.annotation.metadata

import java.util.Collections

/**
 * Immutable metadata describing an annotated workflow.
 *
 * Contains the resolved workflow name and an immutable
 * snapshot of its ordered steps.
 */
internal class WorkflowMetadata(
    val name: String,
    steps: List<StepMetadata>
) {

    val steps: List<StepMetadata>

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }

        require(steps.isNotEmpty()) {
            "Workflow must contain at least one step."
        }

        val snapshot = ArrayList(steps)

        require(snapshot.all { it.name.isNotBlank() }) {
            "Workflow step names cannot be blank."
        }

        require(
            snapshot.map { it.name }.distinct().size == snapshot.size
        ) {
            "Workflow step names must be unique."
        }

        this.steps = Collections.unmodifiableList(snapshot)
    }

    override fun toString(): String =
        "WorkflowMetadata(name='$name', steps=${steps.size})"
}
