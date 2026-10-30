package io.github.mrjimin.kflow.execution

import kotlin.time.Duration

data class StepExecution(
    val name: String,
    val status: ExecutionStatus,
    val duration: Duration,
    val attempt: Int = 1,
    val error: Throwable? = null
)