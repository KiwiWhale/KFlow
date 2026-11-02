package io.github.kiwiwhale.kiwiflow.example.dsl

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val workflow = KiwiFlow.workflow<Int, Int>("Calculation") {
        step("Multiply by 2") {
            execute { input * 2 }
        }
    }

    val result = workflow.execute(10)
    println("Result: $result")
}