package io.github.mrjimin.kflow.example.annotations

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import kotlinx.coroutines.runBlocking

data class Order(
    val id: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Long
)

data class ValidatedOrder(
    val order: Order
)

data class Payment(
    val order: Order,
    val amount: Long
)

data class Shipment(
    val order: Order,
    val payment: Payment,
    val trackingNumber: String
)

data class OrderResult(
    val orderId: String,
    val amount: Long,
    val trackingNumber: String
)

@Workflow(name = "OrderWorkflow")
class OrderWorkflow {

    @Step(order = 1)
    fun validate(order: Order): ValidatedOrder {
        require(order.quantity > 0)
        require(order.unitPrice > 0)

        println("주문 검증 완료")

        return ValidatedOrder(order)
    }

    @Step(order = 2)
    fun calculatePayment(
        validated: ValidatedOrder
    ): Payment {
        val order = validated.order

        return Payment(
            order = order,
            amount = order.quantity * order.unitPrice
        )
    }

    @Step(order = 3)
    fun prepareShipment(payment: Payment): Shipment {
        return Shipment(
            order = payment.order,
            payment = payment,
            trackingNumber = "TRACK-${payment.order.id}"
        )
    }

    @Step(order = 4)
    fun complete(shipment: Shipment): OrderResult {
        println("주문 처리 완료")

        return OrderResult(
            orderId = shipment.order.id,
            amount = shipment.payment.amount,
            trackingNumber = shipment.trackingNumber
        )
    }
}

fun main() = runBlocking {
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<Order, OrderResult>(
            OrderWorkflow()
        )

    val result = workflow.execute(
        Order(
            id = "ORDER-001",
            productName = "Keyboard",
            quantity = 2,
            unitPrice = 50_000
        )
    )

    println(result)
}
