package io.github.mrjimin.kflow.example.annotations

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
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

@Workflow(name = "AnalyticsWorkflow")
class AnalyticsWorkflow {

    @Step(order = 1)
    fun filter(records: List<SalesRecord>): List<SalesRecord> {
        return records.filter { it.amount > 0 }
    }

    @Step(order = 2)
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
    val workflow = AnnotatedWorkflowFactory
        .fromInstance<List<SalesRecord>, SalesReport>(
            AnalyticsWorkflow()
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
