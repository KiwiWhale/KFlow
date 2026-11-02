package io.github.mrjimin.kflow.example.annotations

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.mrjimin.kflow.example.common.dto.SalesRecord
import io.github.mrjimin.kflow.example.common.dto.SalesReport
import kotlinx.coroutines.runBlocking

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

    println("분석 결과: $report")
}
