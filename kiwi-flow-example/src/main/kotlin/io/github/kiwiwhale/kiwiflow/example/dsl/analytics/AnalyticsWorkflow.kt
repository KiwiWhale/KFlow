package io.github.kiwiwhale.kiwiflow.example.dsl.analytics

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.example.common.dto.SalesRecord
import io.github.kiwiwhale.kiwiflow.example.common.dto.SalesReport
import kotlinx.coroutines.runBlocking

class AnalyticsService {

    fun filter(records: List<SalesRecord>): List<SalesRecord> {
        return records.filter { it.amount > 0 }
    }

    fun calculate(records: List<SalesRecord>): SalesReport {
        require(records.isNotEmpty()) {
            "There is no data to analyze."
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

    val workflow = KiwiFlow.workflow<List<SalesRecord>, SalesReport>(
        "AnalyticsWorkflow"
    ) {
        val filtered = step<List<SalesRecord>>("Filter Sales Records") {
            execute {
                service.filter(input)
            }
        }

        step<SalesReport>("Analyze Sales") {
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

    println("Analytics Report: $report")
}
