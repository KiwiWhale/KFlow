package io.github.mrjimin.kflow.core.policy

import io.github.mrjimin.kflow.core.policy.RetryPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

class RetryPolicyTest {

    @Test
    fun `기본 재시도 설정을 생성한다`() {
        val policy = RetryPolicy()

        assertEquals(3, policy.maxAttempts)
        assertEquals(100.milliseconds, policy.initialDelay)
    }

    @Test
    fun `재시도 횟수는 최소 한 번이어야 한다`() {
        assertFailsWith<IllegalArgumentException> {
            RetryPolicy(maxAttempts = 0)
        }
    }

    @Test
    fun `최대 지연 시간은 초기 지연 시간보다 작을 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            RetryPolicy(
                initialDelay = 500.milliseconds,
                maxDelay = 100.milliseconds
            )
        }
    }

    @Test
    fun `배수가 1보다 작으면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            RetryPolicy(multiplier = 0.5)
        }
    }

    @Test
    fun `재시도 지연 시간이 최대 지연 시간을 초과하지 않는다`() {
        val policy = RetryPolicy(
            initialDelay = 100.milliseconds,
            multiplier = 2.0,
            maxDelay = 500.milliseconds
        )

        assertEquals(100.milliseconds, policy.delayFor(1))
        assertEquals(200.milliseconds, policy.delayFor(2))
        assertEquals(400.milliseconds, policy.delayFor(3))
        assertEquals(500.milliseconds, policy.delayFor(4))
    }
}