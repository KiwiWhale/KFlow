package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.time.Duration

/**
 * Represents an event emitted during workflow execution.
 *
 * Events contain execution identifiers and optional step-specific
 * information, allowing listeners to observe workflow progress.
 *
 * @property executionId Unique identifier of the workflow execution.
 * @property workflowName Name of the executing workflow.
 * @property type Lifecycle event type.
 * @property stepName Name of the related step, if applicable.
 * @property attempt Current step execution attempt, if applicable.
 * @property duration Execution duration, if applicable.
 * @property retryDelay Delay before the next retry, if applicable.
 * @property error Failure associated with the event, if applicable.
 */
data class WorkflowEvent(
    val executionId: String,
    val workflowName: String,
    val type: WorkflowEventType,
    val stepName: String? = null,
    val attempt: Int? = null,
    val duration: Duration? = null,
    val retryDelay: Duration? = null,
    val error: Throwable? = null
)