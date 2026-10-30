package io.github.mrjimin.kflow.exception

class WorkflowExecutionException(
    val executionId: String,
    val workflowName: String,
    cause: Throwable
) : KFlowException(
    message = "Execution '$executionId' of workflow '$workflowName' failed",
    cause = cause
)