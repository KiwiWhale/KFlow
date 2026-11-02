package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest

class ExecutionTrackerTest {

    private fun createTracker(
        listeners: List<ExecutionListener> = emptyList()
    ) = ExecutionTracker(
        executionId = "test-id",
        workflowName = "test-workflow",
        listeners = listeners
    )

    @Test
    fun `초기 실행 기록은 비어 있다`() = runTest {
        val tracker = createTracker()

        assertTrue(tracker.snapshot().isEmpty())
    }

    @Test
    fun `성공한 실행 기록을 저장한다`() = runTest {
        val tracker = createTracker()

        tracker.record(
            name = "작업",
            status = ExecutionStatus.SUCCESS,
            duration = 100.milliseconds
        )

        val record = tracker.snapshot().single()

        assertEquals("작업", record.name)
        assertEquals(ExecutionStatus.SUCCESS, record.status)
        assertEquals(100.milliseconds, record.duration)
        assertEquals(1, record.attempt)
        assertTrue(record.successful)
        assertFalse(record.isFailure)
        assertNull(record.error)
    }

    @Test
    fun `실패한 실행 기록을 저장한다`() = runTest {
        val tracker = createTracker()
        val error = IllegalStateException("작업 실패")

        tracker.record(
            name = "실패 작업",
            status = ExecutionStatus.FAILED,
            duration = 50.milliseconds,
            attempt = 2,
            error = error
        )

        val record = tracker.snapshot().single()

        assertEquals("실패 작업", record.name)
        assertEquals(ExecutionStatus.FAILED, record.status)
        assertEquals(2, record.attempt)
        assertTrue(record.isFailure)
        assertFalse(record.successful)
        assertSame(error, record.error)
    }

    @Test
    fun `여러 실행 기록을 순서대로 저장한다`() = runTest {
        val tracker = createTracker()

        tracker.record("첫 번째", ExecutionStatus.SUCCESS, 10.milliseconds)
        tracker.record("두 번째", ExecutionStatus.SUCCESS, 20.milliseconds)
        tracker.record("세 번째", ExecutionStatus.SUCCESS, 30.milliseconds)

        assertEquals(
            listOf("첫 번째", "두 번째", "세 번째"),
            tracker.snapshot().map { it.name }
        )
    }

    @Test
    fun `스냅샷은 이후 실행 기록 변경에 영향을 받지 않는다`() = runTest {
        val tracker = createTracker()

        tracker.record("첫 번째", ExecutionStatus.SUCCESS, 10.milliseconds)

        val snapshot = tracker.snapshot()

        tracker.record("두 번째", ExecutionStatus.SUCCESS, 20.milliseconds)

        assertEquals(1, snapshot.size)
        assertEquals(2, tracker.snapshot().size)
        assertNotSame(snapshot, tracker.snapshot())
    }

    @Test
    fun `워크플로우 시작 이벤트를 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()
        val tracker = createTracker(
            listeners = listOf(ExecutionListener { events.add(it) })
        )

        tracker.workflowStarted()

        val event = events.single()

        assertEquals(WorkflowEventType.WORKFLOW_STARTED, event.type)
        assertEquals("test-id", event.executionId)
        assertEquals("test-workflow", event.workflowName)
    }

    @Test
    fun `워크플로우 완료 이벤트에 실행 시간을 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()
        val tracker = createTracker(
            listeners = listOf(ExecutionListener { events.add(it) })
        )

        tracker.workflowCompleted(100.milliseconds)

        val event = events.single()

        assertEquals(WorkflowEventType.WORKFLOW_COMPLETED, event.type)
        assertEquals(100.milliseconds, event.duration)
    }

    @Test
    fun `워크플로우 실패 이벤트에 예외를 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()
        val error = IllegalStateException("워크플로우 실패")

        val tracker = createTracker(
            listeners = listOf(ExecutionListener { events.add(it) })
        )

        tracker.workflowFailed(100.milliseconds, error)

        val event = events.single()

        assertEquals(WorkflowEventType.WORKFLOW_FAILED, event.type)
        assertSame(error, event.error)
        assertEquals(100.milliseconds, event.duration)
    }

    @Test
    fun `Step 시작 이벤트에 이름과 시도 횟수를 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()
        val tracker = createTracker(
            listeners = listOf(ExecutionListener { events.add(it) })
        )

        tracker.stepStarted("작업", 2)

        val event = events.single()

        assertEquals(WorkflowEventType.STEP_STARTED, event.type)
        assertEquals("작업", event.stepName)
        assertEquals(2, event.attempt)
    }

    @Test
    fun `Step 재시도 이벤트에 대기 시간과 예외를 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()
        val error = IllegalStateException("일시적인 실패")

        val tracker = createTracker(
            listeners = listOf(ExecutionListener { events.add(it) })
        )

        tracker.stepRetrying(
            name = "작업",
            attempt = 1,
            retryDelay = 500.milliseconds,
            error = error
        )

        val event = events.single()

        assertEquals(WorkflowEventType.STEP_RETRYING, event.type)
        assertEquals("작업", event.stepName)
        assertEquals(1, event.attempt)
        assertEquals(500.milliseconds, event.retryDelay)
        assertNull(event.duration)
        assertSame(error, event.error)
    }

    @Test
    fun `성공한 Step 완료 이벤트를 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()
        val tracker = createTracker(
            listeners = listOf(ExecutionListener { events.add(it) })
        )

        tracker.record(
            name = "작업",
            status = ExecutionStatus.SUCCESS,
            duration = 100.milliseconds
        )

        assertEquals(
            WorkflowEventType.STEP_COMPLETED,
            events.single().type
        )
    }

    @Test
    fun `실패한 Step 이벤트를 전달한다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()
        val error = IllegalStateException("작업 실패")

        val tracker = createTracker(
            listeners = listOf(ExecutionListener { events.add(it) })
        )

        tracker.record(
            name = "작업",
            status = ExecutionStatus.FAILED,
            duration = 100.milliseconds,
            error = error
        )

        val event = events.single()

        assertEquals(WorkflowEventType.STEP_FAILED, event.type)
        assertSame(error, event.error)
    }

    @Test
    fun `일반 Listener 예외는 다른 Listener 실행을 중단시키지 않는다`() = runTest {
        val events = mutableListOf<WorkflowEvent>()

        val tracker = createTracker(
            listeners = listOf(
                ExecutionListener {
                    throw IllegalStateException("Listener 실패")
                },
                ExecutionListener {
                    events.add(it)
                }
            )
        )

        tracker.workflowStarted()

        assertEquals(1, events.size)
        assertEquals(WorkflowEventType.WORKFLOW_STARTED, events.single().type)
    }

    @Test
    fun `Listener에서 발생한 취소 예외는 전파한다`() = runTest {
        val tracker = createTracker(
            listeners = listOf(
                ExecutionListener {
                    throw CancellationException("Listener 취소")
                }
            )
        )

        assertFailsWith<CancellationException> {
            tracker.workflowStarted()
        }
    }

    @Test
    fun `여러 Listener는 등록된 순서대로 실행한다`() = runTest {
        val calls = mutableListOf<Int>()

        val tracker = createTracker(
            listeners = listOf(
                ExecutionListener { calls.add(1) },
                ExecutionListener { calls.add(2) },
                ExecutionListener { calls.add(3) }
            )
        )

        tracker.workflowStarted()

        assertEquals(listOf(1, 2, 3), calls)
    }

    @Test
    fun `여러 코루틴의 실행 기록을 안전하게 저장한다`() = runTest {
        val tracker = createTracker()

        (1..100).map { index ->
            async {
                tracker.record(
                    name = "작업-$index",
                    status = ExecutionStatus.SUCCESS,
                    duration = 10.milliseconds
                )
            }
        }.awaitAll()

        val records = tracker.snapshot()

        assertEquals(100, records.size)
        assertEquals(100, records.map { it.name }.distinct().size)
    }

    @Test
    fun `시도 횟수가 0이면 예외가 발생한다`() = runTest {
        val tracker = createTracker()

        assertFailsWith<IllegalArgumentException> {
            tracker.stepStarted("작업", 0)
        }

        assertFailsWith<IllegalArgumentException> {
            tracker.record(
                name = "작업",
                status = ExecutionStatus.SUCCESS,
                duration = 10.milliseconds,
                attempt = 0
            )
        }
    }
}