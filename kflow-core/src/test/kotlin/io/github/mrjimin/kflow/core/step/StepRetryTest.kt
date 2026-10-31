package io.github.mrjimin.kflow.core.step

import io.github.mrjimin.kflow.core.KFlow
import io.github.mrjimin.kflow.core.policy.RetryPolicy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StepRetryTest {

    @Test
    fun `실패한 단계는 설정된 횟수만큼 재시도한다`() = runTest {
        var attempts = 0

        val workflow = KFlow.workflow<Unit, String>("재시도") {
            step("재시도 단계") {
                retry(RetryPolicy(maxAttempts = 3))

                execute {
                    attempts++
                    error("실패")
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(3, attempts)
        assertEquals(3, result.steps.size)
        assertTrue(result.isFailure)
    }

    @Test
    fun `재시도 중 성공하면 즉시 종료한다`() = runTest {
        var attempts = 0

        val workflow = KFlow.workflow<Unit, String>("재시도 성공") {
            step("재시도 단계") {
                retry(RetryPolicy(maxAttempts = 3))

                execute {
                    attempts++

                    if (attempts < 2) {
                        error("일시적인 오류")
                    }

                    "성공"
                }
            }
        }

        val result = workflow.execute(Unit)

        assertEquals("성공", result)
        assertEquals(2, attempts)
    }

    @Test
    fun `재시도하지 않으면 한 번만 실행한다`() = runTest {
        var attempts = 0

        val workflow = KFlow.workflow<Unit, String>("기본 실행") {
            step("작업") {
                execute {
                    attempts++
                    "완료"
                }
            }
        }

        workflow.execute(Unit)

        assertEquals(1, attempts)
    }

    @Test
    fun `재시도 기록에는 각 시도의 번호가 포함된다`() = runTest {
        var attempts = 0

        val workflow = KFlow.workflow<Unit, String>("실행 기록") {
            step("작업") {
                retry(RetryPolicy(maxAttempts = 3))

                execute {
                    attempts++

                    if (attempts < 3) {
                        error("실패")
                    }

                    "완료"
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(
            listOf(1, 2, 3),
            result.steps.map { it.attempt }
        )

        assertEquals(3, attempts)
        assertTrue(result.successful)
    }

    @Test
    fun `모든 재시도가 실패하면 마지막 예외를 기록한다`() = runTest {
        var attempts = 0

        val workflow = KFlow.workflow<Unit, String>("재시도 실패") {
            step("작업") {
                retry(RetryPolicy(maxAttempts = 3))

                execute {
                    attempts++
                    error("실패")
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(3, attempts)
        assertEquals(3, result.steps.size)

        assertEquals(
            listOf(1, 2, 3),
            result.steps.map { it.attempt }
        )

        assertTrue(result.isFailure)

        val workflowException = requireNotNull(result.error)

        val retryException = requireNotNull(workflowException.cause)

        assertEquals(
            "실패",
            retryException.cause?.message
        )

        assertEquals(
            "실패",
            result.steps.last().error?.message
        )
    }
}
