package io.github.kiwiwhale.kiwiflow.example.dsl.payment

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import io.github.kiwiwhale.kiwiflow.example.common.dto.PaymentReceipt
import io.github.kiwiwhale.kiwiflow.example.common.dto.PaymentRequest
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.runBlocking

class PaymentGateway(
    private val failureCount: Int = 3
) {

    private var attempts = 0

    fun charge(request: PaymentRequest): PaymentReceipt {
        println("Payment request: ${request.orderId}")

        attempts++

        if (attempts <= failureCount) {
            throw IllegalStateException("Temporary payment server error")
        }

        return PaymentReceipt(
            transactionId = "TX-${request.orderId}",
            amount = request.amount
        )
    }
}

fun main() = runBlocking {
    val gateway = PaymentGateway()

    val workflow = KiwiFlow.workflow<PaymentRequest, PaymentReceipt>(
        "PaymentWorkflow"
    ) {
        step<PaymentReceipt>("Process Payment") {
            retry(
                RetryPolicy(
                    maxAttempts = 3,
                    initialDelay = 100.milliseconds
                )
            )

            execute {
                gateway.charge(input)
            }
        }
    }

    val receipt = workflow.execute(
        PaymentRequest("ORDER-001", 50_000)
    )

    println("Payment completed: $receipt")
}
