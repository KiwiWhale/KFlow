package io.github.kiwiwhale.kiwiflow.core.execution

/**
 * Represents the outcome of a workflow or step execution.
 */
enum class ExecutionStatus {

    /** Execution completed successfully. */
    SUCCESS,

    /** Execution failed. */
    FAILED
}