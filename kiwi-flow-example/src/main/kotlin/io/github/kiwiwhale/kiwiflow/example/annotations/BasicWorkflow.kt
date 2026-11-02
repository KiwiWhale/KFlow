package io.github.kiwiwhale.kiwiflow.example.annotations

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import kotlinx.coroutines.runBlocking

@Workflow(name = "Calculation")
class CalculationWorkflow {

    @Step(name = "Multiply by 2")
    fun multiply(input: Int): Int {
        return input * 2
    }
}

fun main() = runBlocking {
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<Int, Int>(CalculationWorkflow())

    val result = workflow.execute(10)
    println("Result: $result")
}