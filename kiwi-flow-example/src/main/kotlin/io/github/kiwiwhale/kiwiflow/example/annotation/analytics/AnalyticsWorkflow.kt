package io.github.kiwiwhale.kiwiflow.example.annotation.analytics

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
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

@Workflow(name = "AnalyticsWorkflow")
class AnalyticsWorkflow(
    private val service: AnalyticsService
) {

    @Step(order = 1)
    fun filter(records: List<SalesRecord>): List<SalesRecord> {
        return service.filter(records)
    }

    @Step(order = 2)
    fun calculate(records: List<SalesRecord>): SalesReport {
        return service.calculate(records)
    }
}

fun main() = runBlocking {
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<List<SalesRecord>, SalesReport>(
            AnalyticsWorkflow(AnalyticsService())
        )

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
