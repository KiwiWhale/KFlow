package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.time.Duration

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