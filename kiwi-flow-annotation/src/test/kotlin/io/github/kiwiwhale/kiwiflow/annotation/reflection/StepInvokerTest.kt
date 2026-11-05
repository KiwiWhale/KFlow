package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class StepInvokerTest {

    private val inspector = StepInspector()
    private val invoker = StepInvoker()

    class SampleSteps {

        @Step
        fun noInput(): String = "완료"

        @Step
        fun withInput(input: String): String =
            "$input 처리"

        @Step
        fun nullableInput(input: String?): String =
            input ?: "기본값"

        @Step
        suspend fun suspendNoInput(): String =
            "비동기 완료"

        @Step
        suspend fun suspendWithInput(input: String): String =
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
    fun `입력이 없는 일반 함수를 실행한다`() = runBlocking {
        val result = invoker.invoke(
            metadata("noInput"),
            SampleSteps(),
            null
        )

        assertEquals("완료", result)
    }

    @Test
    fun `입력이 있는 일반 함수를 실행한다`() = runBlocking {
        val result = invoker.invoke(
            metadata("withInput"),
            SampleSteps(),
            "데이터"
        )

        assertEquals("데이터 처리", result)
    }

    @Test
    fun `nullable 입력에 null을 전달한다`() = runBlocking {
        val result = invoker.invoke(
            metadata("nullableInput"),
            SampleSteps(),
            null
        )

        assertEquals("기본값", result)
    }

    @Test
    fun `suspend 함수에 입력 없이 호출한다`() = runBlocking {
        val result = invoker.invoke(
            metadata("suspendNoInput"),
            SampleSteps(),
            null
        )

        assertEquals("비동기 완료", result)
    }

    @Test
    fun `suspend 함수에 입력을 전달한다`() = runBlocking {
        val result = invoker.invoke(
            metadata("suspendWithInput"),
            SampleSteps(),
            "데이터"
        )

        assertEquals("데이터 비동기 처리", result)
    }

    @Test
    fun `반환값이 null인 함수를 실행한다`() = runBlocking {
        val result = invoker.invoke(
            metadata("returnNull"),
            SampleSteps(),
            null
        )

        assertNull(result)
    }

    @Test
    fun `잘못된 입력 타입을 거부한다`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            invoker.invoke(
                metadata("withInput"),
                SampleSteps(),
                123
            )
        }
    }

    @Test
    fun `null을 허용하지 않는 입력에 null을 전달하면 거부한다`() =
        runBlocking {
            assertFailsWith<IllegalArgumentException> {
                invoker.invoke(
                    metadata("withInput"),
                    SampleSteps(),
                    null
                )
            }
        }

    @Test
    fun `잘못된 호출 인스턴스를 거부한다`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            invoker.invoke(
                metadata("withInput"),
                Any(),
                "데이터"
            )
        }
    }

    @Test
    fun `실행 중 발생한 예외를 호출자에게 전달한다`() =
        runBlocking {
            assertFailsWith<IllegalStateException> {
                invoker.invoke(
                    metadata("throwException"),
                    SampleSteps(),
                    null
                )
            }
        }
}
