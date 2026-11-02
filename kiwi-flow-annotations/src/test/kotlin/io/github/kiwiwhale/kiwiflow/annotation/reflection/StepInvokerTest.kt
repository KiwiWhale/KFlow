package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import kotlinx.coroutines.runBlocking
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class StepInvokerTest {

    private val inspector = StepInspector()
    private val invoker = StepInvoker()

    class SampleSteps {

        @Step
        fun noInput(): String = "완료"

        @Step
        fun withInput(input: String): String = "$input 처리"

        @Step
        fun nullableInput(input: String?): String =
            input ?: "기본값"

        @Step
        fun suspendNoInput(): String = "비동기 완료"

        @Step
        fun suspendWithInput(input: String): String =
            "$input 비동기 처리"

        @Step
        fun returnNull(): String? = null

        @Step
        fun throwException(): Nothing {
            throw IllegalStateException("실행 실패")
        }
    }

    private fun metadata(
        functionName: String
    ): StepMetadata {
        val function = SampleSteps::class.memberFunctions
            .first { it.name == functionName }

        return requireNotNull(inspector.inspect(function))
    }

    @Test
    fun `입력 인자가 없는 일반 함수를 실행한다`() = runBlocking {
        val instance = SampleSteps()

        val result = invoker.invoke(
            metadata = metadata("noInput"),
            instance = instance,
            input = null
        )

        assertEquals("완료", result)
    }

    @Test
    fun `입력 인자가 있는 일반 함수를 실행한다`() = runBlocking {
        val instance = SampleSteps()

        val result = invoker.invoke(
            metadata = metadata("withInput"),
            instance = instance,
            input = "데이터"
        )

        assertEquals("데이터 처리", result)
    }

    @Test
    fun `null 입력을 전달할 수 있다`() = runBlocking {
        val instance = SampleSteps()

        val result = invoker.invoke(
            metadata = metadata("nullableInput"),
            instance = instance,
            input = null
        )

        assertEquals("기본값", result)
    }

    @Test
    fun `입력 인자가 없는 suspend 함수를 실행한다`() = runBlocking {
        val instance = SampleSteps()

        val result = invoker.invoke(
            metadata = metadata("suspendNoInput"),
            instance = instance,
            input = null
        )

        assertEquals("비동기 완료", result)
    }

    @Test
    fun `입력 인자가 있는 suspend 함수를 실행한다`() = runBlocking {
        val instance = SampleSteps()

        val result = invoker.invoke(
            metadata = metadata("suspendWithInput"),
            instance = instance,
            input = "데이터"
        )

        assertEquals("데이터 비동기 처리", result)
    }

    @Test
    fun `반환값이 null인 함수도 정상적으로 실행한다`() = runBlocking {
        val instance = SampleSteps()

        val result = invoker.invoke(
            metadata = metadata("returnNull"),
            instance = instance,
            input = null
        )

        assertNull(result)
    }

    @Test
    fun `실행 중 발생한 예외를 호출자에게 전달한다`() = runBlocking {
        val instance = SampleSteps()

        assertFailsWith<IllegalStateException> {
            invoker.invoke(
                metadata = metadata("throwException"),
                instance = instance,
                input = null
            )
        }
    }
}
