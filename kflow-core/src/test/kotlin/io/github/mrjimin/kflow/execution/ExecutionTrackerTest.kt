package io.github.mrjimin.kflow.execution

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class ExecutionTrackerTest {

    private fun createTracker(
        listeners: List<ExecutionListener> = emptyList()
    ) = ExecutionTracker(
        executionId = "test-id",
        workflowName = "test-workflow",
        listeners = listeners
    )

    @Test
    fun `실행 기록을 저장한다`() = runTest {
        val tracker = createTracker()

        tracker.record(
            name = "작업",
            status = ExecutionStatus.SUCCESS,
            duration = 100.milliseconds
        )

        val records = tracker.snapshot()

        assertEquals(1, records.size)
        assertEquals("작업", records.first().name)
    }

    @Test
    fun `여러 실행 기록을 순서대로 저장한다`() = runTest {
        val tracker = createTracker()

        tracker.record(
            "첫 번째",
            ExecutionStatus.SUCCESS,
            10.milliseconds
        )

        tracker.record(
            "두 번째",
            ExecutionStatus.SUCCESS,
            20.milliseconds
        )

        assertEquals(
            listOf("첫 번째", "두 번째"),
            tracker.snapshot().map { it.name }
        )
    }

    @Test
    fun `기록의 스냅샷은 이후 변경에 영향을 받지 않는다`() = runTest {
        val tracker = createTracker()

        tracker.record(
            "첫 번째",
            ExecutionStatus.SUCCESS,
            10.milliseconds
        )

        val snapshot = tracker.snapshot()

        tracker.record(
            "두 번째",
            ExecutionStatus.SUCCESS,
            20.milliseconds
        )

        assertEquals(1, snapshot.size)
        assertEquals(2, tracker.snapshot().size)
    }

    @Test
    fun `워크플로우 이벤트를 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()

        val tracker = createTracker(
            listeners = listOf(
                ExecutionListener { events.add(it) }
            )
        )

        tracker.workflowStarted()
        tracker.workflowCompleted(100.milliseconds)

        assertEquals(2, events.size)

        assertEquals(
            WorkflowEventType.WORKFLOW_STARTED,
            events[0].type
        )

        assertEquals(
            WorkflowEventType.WORKFLOW_COMPLETED,
            events[1].type
        )
    }

    @Test
    fun `일반 Listener 예외는 실행을 중단시키지 않는다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()

        val tracker = createTracker(
            listeners = listOf(
                ExecutionListener {
                    throw IllegalStateException("Listener failure")
                },
                ExecutionListener {
                    events.add(it)
                }
            )
        )

        tracker.workflowStarted()

        assertEquals(1, events.size)
    }
}