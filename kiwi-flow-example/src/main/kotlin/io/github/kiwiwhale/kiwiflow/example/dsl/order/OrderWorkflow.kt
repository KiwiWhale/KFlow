package io.github.kiwiwhale.kiwiflow.example.dsl.order

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.example.common.dto.Order
import io.github.kiwiwhale.kiwiflow.example.common.dto.OrderResult
import io.github.kiwiwhale.kiwiflow.example.common.dto.Payment
import io.github.kiwiwhale.kiwiflow.example.common.dto.Shipment
import kotlinx.coroutines.runBlocking

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

    fun ship(order: Order): Shipment {
        return Shipment(
            orderId = order.id,
            trackingNumber = "TRACK-${order.id}"
        )
    }
}

fun main() = runBlocking {
    val service = OrderService()

    val workflow = KiwiFlow.workflow<Order, OrderResult>("OrderWorkflow") {

        val validated = step<Order>("Validate Order") {
            execute {
                service.validate(input)
            }
        }

        val payment = step<Payment>("Calculate Payment") {
            execute {
                service.calculate(validated)
            }
        }

        val shipment = step<Shipment>("Prepare Shipment") {
            execute {
                service.ship(validated)
            }
        }

        step<OrderResult>("Complete Order") {
            execute {
                OrderResult(
                    orderId = validated.id,
                    payment = payment,
                    shipment = shipment
                )
            }
        }
    }

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
