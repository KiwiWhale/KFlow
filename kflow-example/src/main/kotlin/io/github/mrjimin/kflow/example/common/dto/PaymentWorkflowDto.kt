package io.github.mrjimin.kflow.example.common.dto

data class PaymentRequest(
    val orderId: String,
    val amount: Long
)

data class PaymentReceipt(
    val transactionId: String,
    val amount: Long
)
