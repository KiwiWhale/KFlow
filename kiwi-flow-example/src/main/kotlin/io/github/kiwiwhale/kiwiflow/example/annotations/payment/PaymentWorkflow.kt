package io.github.kiwiwhale.kiwiflow.example.annotations.payment

import io.github.kiwiwhale.kiwiflow.annotation.Retry
import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.kiwiwhale.kiwiflow.core.policy.RetryJitter
import io.github.kiwiwhale.kiwiflow.example.common.dto.PaymentReceipt
import io.github.kiwiwhale.kiwiflow.example.common.dto.PaymentRequest
import kotlinx.coroutines.runBlocking

class PaymentGateway(
    private val failureCount: Int = 3
) {

    private var attempts = 0

    fun charge(request: PaymentRequest): PaymentReceipt {
        attempts++

        if (attempts < failureCount) {
            throw IllegalStateException("결제 서버 일시 오류")
        }

        return PaymentReceipt(
            transactionId = "TX-${request.orderId}",
            amount = request.amount
        )
    }
}

@Workflow(name = "PaymentWorkflow")
class PaymentWorkflow(
    private val gateway: PaymentGateway
) {

    @Step(
        order = 1,
        retry = Retry(
            maxAttempts = 3,
            initialDelayMillis = 100,
            maxDelayMillis = 1000,
            multiplier = 2.0,
            jitter = RetryJitter.NONE
        )
    )
    fun charge(request: PaymentRequest): PaymentReceipt {
        println("결제 요청: ${request.orderId}")

        return gateway.charge(request)
    }
}

fun main() = runBlocking {
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<PaymentRequest, PaymentReceipt>(
            PaymentWorkflow(PaymentGateway())
        )

    val receipt = workflow.execute(
        PaymentRequest(
            orderId = "ORDER-001",
            amount = 50_000
        )
    )

    println("결제 완료: $receipt")
}
