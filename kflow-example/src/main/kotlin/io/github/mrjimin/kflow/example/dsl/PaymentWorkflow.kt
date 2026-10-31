package io.github.mrjimin.kflow.example.dsl

import io.github.mrjimin.kflow.KFlow
import io.github.mrjimin.kflow.policy.RetryPolicy
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

data class PaymentRequest(
    val orderId: String,
    val amount: Long
)

data class PaymentReceipt(
    val transactionId: String,
    val amount: Long
)

class PaymentGateway {

    private var attempts = 0

    fun charge(request: PaymentRequest): PaymentReceipt {
        attempts++

        if (attempts < 3) {
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
