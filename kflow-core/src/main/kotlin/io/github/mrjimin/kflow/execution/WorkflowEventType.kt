package io.github.mrjimin.kflow.execution

enum class WorkflowEventType {
    WORKFLOW_STARTED,
    WORKFLOW_COMPLETED,
    WORKFLOW_FAILED,
    STEP_STARTED,
    STEP_COMPLETED,
    STEP_FAILED
}