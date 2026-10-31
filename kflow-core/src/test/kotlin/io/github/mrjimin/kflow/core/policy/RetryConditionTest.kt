package io.github.mrjimin.kflow.core.policy

import io.github.mrjimin.kflow.core.KFlow
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RetryConditionTest {

    @Test
    fun `ALWAYS는 모든 예외를 재시도한다`() {
        assertTrue(
            RetryCondition.ALWAYS.shouldRetry(
                IllegalStateException()
            )
        )
    }

    @Test
    fun `NEVER는 재시도하지 않는다`() {
        assertFalse(
            RetryCondition.NEVER.shouldRetry(
                IllegalStateException()
            )
        )
    }

    @Test
    fun `특정 예외만 재시도할 수 있다`() {
        val condition = RetryCondition { exception ->
            exception is IOException
        }

        assertTrue(
            condition.shouldRetry(IOException())
        )

        assertFalse(
            condition.shouldRetry(IllegalArgumentException())
        )
    }

    @Test
    fun `재시도 조건이 false이면 즉시 실패한다`() = runTest {
        var attempts = 0

        val workflow = KFlow.workflow<Unit, String>("재시도 조건") {
            step("작업") {
                retry(
                    RetryPolicy(
                        maxAttempts = 5,
                        condition = RetryCondition.NEVER
                    )
                )

                execute {
                    attempts++
                    error("실패")
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(1, attempts)
        assertEquals(1, result.steps.size)
        assertTrue(result.steps.first().error is IllegalStateException)
    }
}
