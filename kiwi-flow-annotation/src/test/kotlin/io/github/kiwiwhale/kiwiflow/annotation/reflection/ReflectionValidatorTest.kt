package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Retry
import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidStepException
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ReflectionValidatorTest {

    class ValidSteps {

        @Step
        fun noInput() {}

        @Step
        fun withInput(input: String): String = input

        @Step
        suspend fun suspendStep(input: String): String = input

        @Step
        fun nullableInput(input: String?): String = input ?: ""
    }

    class InvalidSteps {

        @Step
        private fun privateFunction() {}

        @Step
        fun withDefault(input: String = "default") {}

        @Step
        fun withVararg(vararg input: String) {}

        @Step
        fun withMultipleInputs(
            first: String,
            second: String
        ) {}

        @Step
        fun <T> generic(input: T) {}

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

    class ExtensionSteps {

        @Step
        fun String.extensionStep() {}
    }

    private fun validate(
        functionName: String,
        annotation: Step = Step()
    ) {
        val function = InvalidSteps::class.memberFunctions
            .first { it.name == functionName }

        ReflectionValidator.validateStep(
            function,
            annotation
        )
    }

    @Test
    fun `입력이 없는 공개 함수를 허용한다`() {
        val function = ValidSteps::class.memberFunctions
            .first { it.name == "noInput" }

        ReflectionValidator.validateStep(
            function,
            Step()
        )
    }

    @Test
    fun `입력이 하나인 공개 함수를 허용한다`() {
        val function = ValidSteps::class.memberFunctions
            .first { it.name == "withInput" }

        ReflectionValidator.validateStep(
            function,
            Step()
        )
    }

    @Test
    fun `suspend 함수를 허용한다`() {
        val function = ValidSteps::class.memberFunctions
            .first { it.name == "suspendStep" }

        ReflectionValidator.validateStep(
            function,
            Step()
        )
    }

    @Test
    fun `nullable 입력을 허용한다`() {
        val function = ValidSteps::class.memberFunctions
            .first { it.name == "nullableInput" }

        ReflectionValidator.validateStep(
            function,
            Step()
        )
    }

    @Test
    fun `private 함수를 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("privateFunction")
        }
    }

    @Test
    fun `기본 매개변수를 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("withDefault")
        }
    }

    @Test
    fun `vararg 매개변수를 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("withVararg")
        }
    }

    @Test
    fun `두 개 이상의 입력 매개변수를 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("withMultipleInputs")
        }
    }

    @Test
    fun `Generic 함수를 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate("generic")
        }
    }

    @Test
    fun `음수 timeout을 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "negativeTimeout",
                Step(timeoutMillis = -1)
            )
        }
    }

    @Test
    fun `최대 재시도 횟수가 1 미만이면 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "invalidAttempts",
                Step(retry = Retry(maxAttempts = 0))
            )
        }
    }

    @Test
    fun `음수 초기 재시도 지연을 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "negativeInitialDelay",
                Step(retry = Retry(initialDelayMillis = -1))
            )
        }
    }

    @Test
    fun `초기 지연보다 작은 최대 지연을 거부한다`() {
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
    fun `1보다 작은 재시도 배수를 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "invalidMultiplier",
                Step(retry = Retry(multiplier = 0.5))
            )
        }
    }

    @Test
    fun `NaN 재시도 배수를 거부한다`() {
        assertFailsWith<InvalidStepException> {
            validate(
                "nanMultiplier",
                Step(retry = Retry(multiplier = Double.NaN))
            )
        }
    }
}
