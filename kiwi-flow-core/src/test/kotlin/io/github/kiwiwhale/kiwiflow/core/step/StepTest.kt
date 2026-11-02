package io.github.kiwiwhale.kiwiflow.core.step

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.exception.WorkflowExecutionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class StepTest {

    @Test
    fun `단계의 결과를 반환한다`() = runTest {
        val workflow = KiwiFlow.workflow<Int, Int>("결과") {
            step("계산") {
                execute { input + 10 }
            }
        }

        assertEquals(20, workflow.execute(10))
    }

    @Test
    fun `단계 이름이 비어 있으면 예외가 발생한다`() = runTest {
        val workflow = KiwiFlow.workflow<Unit, Unit>("이름 검증") {
            step("") {
                execute {}
            }
        }

        val exception = assertFailsWith<WorkflowExecutionException> {
            workflow.execute(Unit)
        }

        assertIs<IllegalArgumentException>(exception.cause)
    }

    @Test
    fun `단계 이름이 공백이면 예외가 발생한다`() = runTest {
        val workflow = KiwiFlow.workflow<Unit, Unit>("이름 검증") {
            step("   ") {
                execute {}
            }
        }

        val exception = assertFailsWith<WorkflowExecutionException> {
            workflow.execute(Unit)
        }

        assertTrue(exception.cause is IllegalArgumentException)
    }

    @Test
    fun `실행 동작을 중복 정의할 수 없다`() {
        val builder = StepBuilder<Unit, Unit>()

        builder.execute {}

        assertFailsWith<IllegalStateException> {
            builder.execute {}
        }
    }

    @Test
    fun `실행 동작을 정의하지 않으면 예외가 발생한다`() = runTest {
        val workflow = KiwiFlow.workflow<Unit, Unit>("동작 누락") {
            step("빈 단계") {}
        }

        val exception = assertFailsWith<WorkflowExecutionException> {
            workflow.execute(Unit)
        }

        assertIs<IllegalStateException>(exception.cause)
    }
}
