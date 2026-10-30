package io.github.mrjimin.kflow.step

import io.github.mrjimin.kflow.KFlow
import io.github.mrjimin.kflow.exception.StepTimeoutException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

class StepTimeoutTest {

    @Test
    fun `제한 시간 안에 완료하면 성공한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("시간 제한") {
            step("작업") {
                timeout(500.milliseconds)

                execute {
                    delay(100)
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
                    delay(200)
                    "완료"
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertFalse(result.successful)
        assertTrue(result.error is StepTimeoutException)
    }

    @Test
    fun `외부 코루틴 취소를 전파한다`() = runTest {
        val workflow = KFlow.workflow<Unit, Unit>("취소") {
            step("작업") {
                execute {
                    delay(10_000)
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

        assertFailsWith<IllegalArgumentException> {
            workflow.execute(Unit)
        }
    }

    @Test
    fun `제한 시간을 초과하면 StepTimeoutException을 기록한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("시간 초과") {
            step("작업") {
                timeout(100.milliseconds)

                execute {
                    delay(200)
                    "완료"
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertFalse(result.successful)

        val exception = requireNotNull(result.error)

        assertTrue(exception is StepTimeoutException)

        val timeoutException = exception as StepTimeoutException

        assertEquals("작업", timeoutException.stepName)
        assertEquals(100.milliseconds, timeoutException.timeout)

        assertEquals(1, result.steps.size)
        assertSame(exception, result.steps.first().error)
    }
}
