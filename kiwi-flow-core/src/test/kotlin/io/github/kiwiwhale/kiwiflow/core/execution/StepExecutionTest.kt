package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds

class StepExecutionTest {

    @Test
    fun `단계 실행 정보를 생성한다`() {
        val execution = StepExecution(
            name = "계산",
            status = ExecutionStatus.SUCCESS,
            duration = 100.milliseconds
        )

        assertEquals("계산", execution.name)
        assertEquals(ExecutionStatus.SUCCESS, execution.status)
        assertEquals(100.milliseconds, execution.duration)
        assertEquals(1, execution.attempt)
        assertNull(execution.error)
    }

    @Test
    fun `실패한 단계의 예외를 보관한다`() {
        val exception = IllegalStateException("실패")

        val execution = StepExecution(
            name = "실패 작업",
            status = ExecutionStatus.FAILED,
            duration = 100.milliseconds,
            error = exception
        )

        assertSame(exception, execution.error)
        assertEquals(ExecutionStatus.FAILED, execution.status)
    }

    @Test
    fun `실행 시도 번호를 보관한다`() {
        val execution = StepExecution(
            name = "재시도 작업",
            status = ExecutionStatus.FAILED,
            duration = 100.milliseconds,
            attempt = 3
        )

        assertEquals(3, execution.attempt)
    }
}