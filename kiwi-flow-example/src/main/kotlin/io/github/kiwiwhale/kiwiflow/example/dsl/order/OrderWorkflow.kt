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

        val validated = step<Order>("주문 검증") {
            execute {
                service.validate(input)
            }
        }

        val payment = step<Payment>("결제 금액 계산") {
            execute {
                service.calculate(validated)
            }
        }

        val shipment = step<Shipment>("배송 준비") {
            execute {
                service.ship(validated)
            }
        }

        step<OrderResult>("주문 완료") {
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
