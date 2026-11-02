package io.github.kiwiwhale.kiwiflow.example.common.dto

data class SalesRecord(
    val product: String,
    val amount: Long
)

data class SalesReport(
    val totalSales: Long,
    val averageSales: Double,
    val productCount: Int
)