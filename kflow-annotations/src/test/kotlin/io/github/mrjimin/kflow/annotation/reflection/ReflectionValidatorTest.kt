package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.Retry
import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.exception.InvalidStepException
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ReflectionValidatorTest {

    class ValidSteps {

        @Step
        fun noInput() {}

        @Step
        fun withInput(input: String): String = input
    }

    class InvalidSteps {

        @Step
        private fun privateFunction() {}

        @Step
        fun withDefault(input: String = "default") {}

        @Step
        fun withVararg(vararg input: String) {}

        @Step(timeoutMillis = -1)
        fun negativeTimeout() {}

        @Step(retry = Retry(maxAttempts = 0))
        fun invalidAttempts() {}

        @Step(retry = Retry(initialDelayMillis = -1))
        fun negativeInitialDelay() {}

        @Step(
            retry = Retry(
                initialDelayMillis = 1000,
                maxDelayMillis = 500
            )
        )
        fun invalidMaxDelay() {}

        @Step(retry = Retry(multiplier = 0.5))
        fun invalidMultiplier() {}

        @Step(retry = Retry(multiplier = Double.NaN))
        fun nanMultiplier() {}
    }

    private fun validate(
        functionName: String,
        annotation: Step
    ) {
        val function = InvalidSteps::class.memberFunctions
            .first { it.name == functionName }

        ReflectionValidator.validateStep(function, annotation)
    }

    @Test
    fun `입력이 없는 공개 함수를 허용한다`() {
        val function = ValidSteps::class.memberFunctions
            .first { it.name == "noInput" }

        ReflectionValidator.validateStep(
            function,
            function.annotations.filterIsInstance<Step>().first()
        )
    }

    @Test
    fun `입력이 하나인 공개 함수를 허용한다`() {
        val function = ValidSteps::class.memberFunctions
            .first { it.name == "withInput" }

        ReflectionValidator.validateStep(
            function,
            function.annotations.filterIsInstance<Step>().first()
        )
    }

    @Test
    fun `private 함수는 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("privateFunction", Step())
        }
    }

    @Test
    fun `기본 매개변수는 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("withDefault", Step())
        }
    }

    @Test
    fun `vararg 매개변수는 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("withVararg", Step())
        }
    }

    @Test
    fun `음수 timeout은 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("negativeTimeout", Step(timeoutMillis = -1))
        }
    }

    @Test
    fun `최대 재시도 횟수는 1 이상이어야 한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "invalidAttempts",
                Step(retry = Retry(maxAttempts = 0))
            )
        }
    }

    @Test
    fun `초기 재시도 지연은 음수일 수 없다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "negativeInitialDelay",
                Step(retry = Retry(initialDelayMillis = -1))
            )
        }
    }

    @Test
    fun `최대 지연은 초기 지연보다 작을 수 없다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "invalidMaxDelay",
                Step(
                    retry = Retry(
                        initialDelayMillis = 1000,
                        maxDelayMillis = 500
                    )
                )
            )
        }
    }

    @Test
    fun `재시도 배수는 1 이상이어야 한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "invalidMultiplier",
                Step(retry = Retry(multiplier = 0.5))
            )
        }
    }

    @Test
    fun `NaN 재시도 배수는 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "nanMultiplier",
                Step(retry = Retry(multiplier = Double.NaN))
            )
        }
    }
}