package io.github.mrjimin.kflow.example.dsl

import io.github.mrjimin.kflow.core.KFlow
import io.github.mrjimin.kflow.core.policy.RetryPolicy
import io.github.mrjimin.kflow.example.common.dto.PaymentReceipt
import io.github.mrjimin.kflow.example.common.dto.PaymentRequest
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

class PaymentGateway(
    private val failureCount: Int = 3
) {

    private var attempts = 0

    fun charge(request: PaymentRequest): PaymentReceipt {
        println("결제 요청: ${request.orderId}")

        attempts++

        if (attempts <= failureCount) {
            throw IllegalStateException("결제 서버 일시 오류")
        }

        return PaymentReceipt(
            transactionId = "TX-${request.orderId}",
            amount = request.amount
        )
    }
}

fun main() = runBlocking {
    val gateway = PaymentGateway()

    val workflow = KFlow.workflow<PaymentRequest, PaymentReceipt>(
        "PaymentWorkflow"
    ) {
        step<PaymentReceipt>("결제 요청") {
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

    println("결제 완료: $receipt")
}
