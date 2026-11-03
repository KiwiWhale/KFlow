package io.github.kiwiwhale.kiwiflow.core.execution

/**
 * Identifies the type of lifecycle event emitted during execution.
 *
 * Workflow events describe the overall workflow lifecycle,
 * while step events describe individual step executions.
 */
enum class WorkflowEventType {

    /** Workflow execution has started. */
    WORKFLOW_STARTED,

    /** Workflow execution completed successfully. */
    WORKFLOW_COMPLETED,

    /** Workflow execution failed. */
    WORKFLOW_FAILED,

    /** A step execution attempt has started. */
    STEP_STARTED,

    /** A step execution attempt completed successfully. */
    STEP_COMPLETED,

    /** A step execution attempt failed. */
    STEP_FAILED,

    /** A failed step is scheduled for another attempt. */
    STEP_RETRYING
}