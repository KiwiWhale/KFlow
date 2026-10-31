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

data class Payment(
    val orderId: String,
    val amount: Long
)

data class Shipment(
    val orderId: String,
    val trackingNumber: String
)

data class OrderResult(
    val orderId: String,
    val payment: Payment,
    val shipment: Shipment
)

class OrderService {

    fun validate(order: Order): Order {
        require(order.quantity > 0)
        require(order.unitPrice > 0)
        return order
    }

    fun calculate(order: Order): Payment {
        return Payment(
            orderId = order.id,
            amount = order.quantity * order.unitPrice
        )
    }

    fun ship(payment: Payment): Shipment {
        return Shipment(
            orderId = payment.orderId,
            trackingNumber = "TRACK-${payment.orderId}"
        )
    }
}

@Workflow(name = "OrderWorkflow")
class OrderWorkflow(
    private val service: OrderService
) {

    @Step(order = 1)
    fun validate(order: Order): Order {
        return service.validate(order)
    }

    @Step(order = 2)
    fun calculate(order: Order): Payment {
        return service.calculate(order)
    }

    @Step(order = 3)
    fun ship(payment: Payment): Shipment {
        return service.ship(payment)
    }

    @Step(order = 4)
    fun complete(shipment: Shipment): OrderResult {
        val payment = Payment(
            orderId = shipment.orderId,
            amount = 100_000L
        )

        return OrderResult(
            orderId = shipment.orderId,
            payment = payment,
            shipment = shipment
        )
    }
}

fun main() = runBlocking {
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<Order, OrderResult>(
            OrderWorkflow(OrderService())
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
