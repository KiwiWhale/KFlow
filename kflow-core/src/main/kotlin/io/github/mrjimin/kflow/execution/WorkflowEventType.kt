package io.github.mrjimin.kflow.execution

enum class WorkflowEventType {
    WORKFLOW_STARTED, // Workflow execution started
    WORKFLOW_COMPLETED, // Workflow execution completed successfully
    WORKFLOW_FAILED, // Workflow execution failed

    STEP_STARTED, // Step execution started
    STEP_COMPLETED, // Step execution completed successfully
    STEP_FAILED // Step execution failed
}