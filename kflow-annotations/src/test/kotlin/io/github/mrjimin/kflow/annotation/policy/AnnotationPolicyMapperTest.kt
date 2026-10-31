package io.github.mrjimin.kflow.annotation.policy

import io.github.mrjimin.kflow.annotation.StepRetry
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AnnotationPolicyMapperTest {

    @Test
    fun `최대 시도 횟수가 1이면 재시도 정책을 생성하지 않는다`() {
        val annotation = StepRetry(
            maxAttempts = 1
        )

        val policy = AnnotationPolicyMapper.toRetryPolicy(annotation)

        assertNull(policy)
    }

    @Test
    fun `기본 재시도 설정으로 정책을 생성한다`() {
        val annotation = StepRetry()

        val policy = AnnotationPolicyMapper.toRetryPolicy(annotation)

        assertNotNull(policy)
    }

    @Test
    fun `사용자 지정 재시도 설정으로 정책을 생성한다`() {
        val annotation = StepRetry(
            maxAttempts = 5,
            initialDelayMillis = 200,
            maxDelayMillis = 5000,
            multiplier = 3.0
        )

        val policy = AnnotationPolicyMapper.toRetryPolicy(annotation)

        assertNotNull(policy)
    }

    @Test
    fun `최대 시도 횟수가 2이면 재시도 정책을 생성한다`() {
        val annotation = StepRetry(
            maxAttempts = 2
        )

        val policy = AnnotationPolicyMapper.toRetryPolicy(annotation)

        assertNotNull(policy)
    }

    @Test
    fun `재시도 지연 시간이 0이어도 정책을 생성한다`() {
        val annotation = StepRetry(
            maxAttempts = 3,
            initialDelayMillis = 0,
            maxDelayMillis = 0
        )

        val policy = AnnotationPolicyMapper.toRetryPolicy(annotation)

        assertNotNull(policy)
    }

    @Test
    fun `최대 지연 시간과 초기 지연 시간이 같아도 정책을 생성한다`() {
        val annotation = StepRetry(
            maxAttempts = 3,
            initialDelayMillis = 100,
            maxDelayMillis = 100
        )

        val policy = AnnotationPolicyMapper.toRetryPolicy(annotation)

        assertNotNull(policy)
    }
}
