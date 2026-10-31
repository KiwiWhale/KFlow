package io.github.mrjimin.kflow.core.step

import io.github.mrjimin.kflow.core.KFlow
import io.github.mrjimin.kflow.core.exception.StepTimeoutException
import io.github.mrjimin.kflow.core.exception.WorkflowExecutionException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class StepTimeoutTest {

    @Test
    fun `제한 시간 안에 완료하면 성공한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("시간 제한") {
            step("작업") {
                timeout(500.milliseconds)

                execute {
                    delay(100.milliseconds)
                    "완료"
                }
            }
        }

        assertEquals("완료", workflow.execute(Unit))
    }

    @Test
    fun `제한 시간을 초과하면 실패한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("시간 초과") {
            step("작업") {
                timeout(100.milliseconds)

                execute {
                    delay(200.milliseconds)
                    "완료"
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertFalse(result.successful)
        assertTrue(result.isFailure)
        assertNotNull(result.error)

        val stepException = requireNotNull(result.error.cause)
        val timeoutException = requireNotNull(stepException.cause)

        assertIs<StepTimeoutException>(timeoutException)
    }

    @Test
    fun `외부 코루틴 취소를 전파한다`() = runTest {
        val workflow = KFlow.workflow<Unit, Unit>("취소") {
            step("작업") {
                execute {
                    delay(10_000.milliseconds)
                }
            }
        }

        val job = backgroundScope.launch {
            workflow.execute(Unit)
        }

        job.cancel()
        job.join()

        assertTrue(job.isCancelled)
    }

    @Test
    fun `시간 제한은 양수여야 한다`() = runTest {
        val workflow = KFlow.workflow<Unit, Unit>("잘못된 시간") {
            step("작업") {
                timeout((-1).milliseconds)
                execute {}
            }
        }

        val exception = assertFailsWith<WorkflowExecutionException> {
            workflow.execute(Unit)
        }

        assertIs<IllegalArgumentException>(exception.cause)
    }

    @Test
    fun `제한 시간을 초과하면 StepTimeoutException을 기록한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("시간 초과") {
            step("작업") {
                timeout(100.milliseconds)

                execute {
                    delay(200.milliseconds)
                    "완료"
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertFalse(result.successful)

        val workflowException = requireNotNull(result.error)
        val stepException = requireNotNull(workflowException.cause)
        val timeoutException = requireNotNull(stepException.cause)

        assertIs<StepTimeoutException>(timeoutException)

        val recordedException = result.steps.first().error

        assertIs<StepTimeoutException>(recordedException)

        assertEquals("작업", recordedException.stepName)
        assertEquals(100.milliseconds, recordedException.timeout)

        assertEquals(1, result.steps.size)
        assertSame(timeoutException, recordedException)
    }
}
