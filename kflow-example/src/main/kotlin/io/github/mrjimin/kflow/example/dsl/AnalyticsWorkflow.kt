package io.github.mrjimin.kflow.example.dsl

import io.github.mrjimin.kflow.KFlow
import kotlinx.coroutines.runBlocking

data class SalesRecord(
    val product: String,
    val amount: Long
)

data class SalesReport(
    val totalSales: Long,
    val averageSales: Double,
    val productCount: Int
)

class AnalyticsService {

    fun filter(records: List<SalesRecord>): List<SalesRecord> {
        return records.filter { it.amount > 0 }
    }

    fun calculate(records: List<SalesRecord>): SalesReport {
        require(records.isNotEmpty()) {
            "분석할 데이터가 없습니다."
        }

        val total = records.sumOf { it.amount }

        return SalesReport(
            totalSales = total,
            averageSales = total.toDouble() / records.size,
            productCount = records.size
        )
    }
}

fun main() = runBlocking {
    val service = AnalyticsService()

    val workflow = KFlow.workflow<List<SalesRecord>, SalesReport>(
        "AnalyticsWorkflow"
    ) {
        val filtered = step<List<SalesRecord>>("데이터 정제") {
            execute {
                service.filter(input)
            }
        }

        step<SalesReport>("매출 분석") {
            execute {
                service.calculate(filtered)
            }
        }
    }

    val report = workflow.execute(
        listOf(
            SalesRecord("Keyboard", 50_000),
            SalesRecord("Mouse", 30_000),
            SalesRecord("Monitor", 300_000),
            SalesRecord("Invalid", -100)
        )
    )

    println("분석 결과: $report")
}
