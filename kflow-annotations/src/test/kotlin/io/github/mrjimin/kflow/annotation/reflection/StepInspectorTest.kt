package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.Retry
import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.exception.InvalidStepException
import kotlin.reflect.full.memberFunctions
import kotlin.test.*

class StepInspectorTest {

    private val inspector = StepInspector()

    class SampleSteps {

        fun unannotated() {}

        @Step
        fun defaultName() {}

        @Step(name = "custom", order = 10)
        fun customName(input: String): String = input

        @Step(timeoutMillis = 500)
        fun timeoutStep() {}

        @Step(timeoutMillis = 0)
        fun noTimeout() {}

        @Step(retry = Retry(maxAttempts = 1))
        fun noRetry() {}

        @Step(
            retry = Retry(
                maxAttempts = 5,
                initialDelayMillis = 200,
                maxDelayMillis = 5000,
                multiplier = 3.0
            )
        )
        fun retryStep() {}
    }

    @Test
    fun `Step 어노테이션이 없는 함수는 null을 반환한다`() {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == "unannotated" }

        assertNull(inspector.inspect(function))
    }

    @Test
    fun `기본 이름을 사용하는 단계의 메타데이터를 생성한다`() {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == "defaultName" }

        val metadata = inspector.inspect(function)

        assertNotNull(metadata)
        assertEquals("defaultName", metadata.name)
        assertEquals(0, metadata.order)
        assertNull(metadata.timeout)
        assertNotNull(metadata.retryPolicy)
        assertNull(metadata.inputParameter)
    }

    @Test
    fun `사용자 지정 이름과 순서를 메타데이터에 반영한다`() {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == "customName" }

        val metadata = inspector.inspect(function)

        assertNotNull(metadata)
        assertEquals("custom", metadata.name)
        assertEquals(10, metadata.order)
        assertNotNull(metadata.inputParameter)
        assertEquals("input", metadata.inputParameter.name)
    }

    @Test
    fun `timeout 설정을 Duration으로 변환한다`() {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == "timeoutStep" }

        val metadata = inspector.inspect(function)

        assertNotNull(metadata)
        assertEquals(500, metadata.timeout?.inWholeMilliseconds)
    }

    @Test
    fun `timeout이 0이면 제한 시간을 설정하지 않는다`() {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == "noTimeout" }

        val metadata = inspector.inspect(function)

        assertNotNull(metadata)
        assertNull(metadata.timeout)
    }

    @Test
    fun `최대 시도 횟수가 1이면 재시도 정책을 설정하지 않는다`() {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == "noRetry" }

        val metadata = inspector.inspect(function)

        assertNotNull(metadata)
        assertNull(metadata.retryPolicy)
    }

    @Test
    fun `재시도 설정이 있으면 재시도 정책을 생성한다`() {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == "retryStep" }

        val metadata = inspector.inspect(function)

        assertNotNull(metadata)
        assertNotNull(metadata.retryPolicy)
    }

    @Test
    fun `유효하지 않은 timeout은 예외가 발생한다`() {
        class InvalidTimeout {
            @Step(timeoutMillis = -1)
            fun execute() {}
        }

        val function = InvalidTimeout::class.memberFunctions
            .first { it.name == "execute" }

        assertFailsWith<InvalidStepException> {
            inspector.inspect(function)
        }
    }
}
