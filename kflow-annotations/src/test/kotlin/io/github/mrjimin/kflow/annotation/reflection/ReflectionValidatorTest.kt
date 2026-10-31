package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.exception.InvalidStepException
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReflectionValidatorTest {

    class ValidSteps {

        @Step
        fun execute() {}

        @Step
        fun transform(input: String): String = input
    }

    class InvalidSteps {

        @Step
        private fun privateStep() {}

        @Step
        fun multipleInputs(first: String, second: String) {}

        @Step
        fun defaultInput(input: String = "default") {}

        @Step
        fun varargInput(vararg inputs: String) {}
    }

    class InvalidConfiguration {

        @Step(timeoutMillis = -1)
        fun negativeTimeout() {}

        @Step(retry = io.github.mrjimin.kflow.annotation.StepRetry(maxAttempts = 0))
        fun invalidAttempts() {}

        @Step(retry = io.github.mrjimin.kflow.annotation.StepRetry(initialDelayMillis = -1))
        fun negativeInitialDelay() {}

        @Step(
            retry = io.github.mrjimin.kflow.annotation.StepRetry(
                initialDelayMillis = 100,
                maxDelayMillis = 50
            )
        )
        fun invalidMaxDelay() {}

        @Step(
            retry = io.github.mrjimin.kflow.annotation.StepRetry(
                multiplier = 0.5
            )
        )
        fun invalidMultiplier() {}

        @Step(
            retry = io.github.mrjimin.kflow.annotation.StepRetry(
                multiplier = Double.NaN
            )
        )
        fun nanMultiplier() {}

        @Step(
            retry = io.github.mrjimin.kflow.annotation.StepRetry(
                multiplier = Double.POSITIVE_INFINITY
            )
        )
        fun infiniteMultiplier() {}
    }

    private fun validate(
        instance: Any,
        functionName: String
    ) {
        val function = instance::class.memberFunctions
            .first { it.name == functionName }

        val annotation = function.annotations
            .filterIsInstance<Step>()
            .first()

        ReflectionValidator.validateStep(function, annotation)
    }

    @Test
    fun `인자가 없는 공개 멤버 함수는 검증에 성공한다`() {
        validate(ValidSteps(), "execute")
    }

    @Test
    fun `입력 인자가 하나인 공개 멤버 함수는 검증에 성공한다`() {
        validate(ValidSteps(), "transform")
    }

    @Test
    fun `비공개 함수는 예외가 발생한다`() {
        val exception = assertFailsWith<InvalidStepException> {
            validate(InvalidSteps(), "privateStep")
        }

        assertEquals(
            "Invalid KFlow step 'privateStep': Step method must be public.",
            exception.message
        )
    }

    @Test
    fun `입력 인자가 두 개인 함수는 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidSteps(), "multipleInputs")
        }
    }

    @Test
    fun `기본값이 지정된 매개변수는 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidSteps(), "defaultInput")
        }
    }

    @Test
    fun `가변 인자 매개변수는 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidSteps(), "varargInput")
        }
    }

    @Test
    fun `음수 timeout은 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidConfiguration(), "negativeTimeout")
        }
    }

    @Test
    fun `최대 시도 횟수가 1보다 작으면 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidConfiguration(), "invalidAttempts")
        }
    }

    @Test
    fun `초기 지연 시간이 음수이면 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidConfiguration(), "negativeInitialDelay")
        }
    }

    @Test
    fun `최대 지연 시간이 초기 지연 시간보다 작으면 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidConfiguration(), "invalidMaxDelay")
        }
    }

    @Test
    fun `배수가 1보다 작으면 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidConfiguration(), "invalidMultiplier")
        }
    }

    @Test
    fun `배수가 NaN이면 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidConfiguration(), "nanMultiplier")
        }
    }

    @Test
    fun `배수가 무한대이면 예외가 발생한다`() {
        assertFailsWith<InvalidStepException> {
            validate(InvalidConfiguration(), "infiniteMultiplier")
        }
    }
}