package io.github.kiwiwhale.kiwiflow.annotation.factory

import io.github.kiwiwhale.kiwiflow.annotation.Retry
import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.exception.WorkflowExecutionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class AnnotatedWorkflowFactoryTest {

    @Workflow(name = "regular-workflow")
    class RegularWorkflow {

        @Step(order = 1)
        fun first(input: String): String =
            "$input-first"

        @Step(order = 2)
        fun second(input: String): String =
            "$input-second"
    }

    @Workflow(name = "suspend-workflow")
    class SuspendWorkflow {

        @Step(order = 1)
        suspend fun first(input: String): String {
            delay(10)
            return "$input-first"
        }

        @Step(order = 2)
        suspend fun second(input: String): String {
            delay(10)
            return "$input-second"
        }
    }

    @Workflow(name = "no-input-workflow")
    class NoInputWorkflow {

        @Step(order = 1)
        fun initialize(): String = "initialized"

        @Step(order = 2)
        fun finish(): String = "completed"
    }

    open class BaseWorkflow {

        @Step(order = 1)
        fun validate(input: String): String =
            "$input-validated"
    }

    @Workflow(name = "inherited-workflow")
    class InheritedWorkflow : BaseWorkflow() {

        @Step(order = 2)
        fun process(input: String): String =
            "$input-processed"
    }

    @Workflow(name = "retry-workflow")
    class RetryWorkflow {

        var attempts = 0

        @Step(
            retry = Retry(
                maxAttempts = 3,
                initialDelayMillis = 0,
                maxDelayMillis = 0
            )
        )
        fun execute(input: String): String {
            attempts++

            if (attempts < 3) {
                error("일시적인 실행 실패")
            }

            return "$input-success"
        }
    }

    @Workflow(name = "timeout-workflow")
    class TimeoutWorkflow {

        @Step(timeoutMillis = 10)
        suspend fun execute(input: String): String {
            delay(100)
            return input
        }
    }

    @Test
    fun `일반 함수를 순차적으로 실행한다`() = runTest {
        val workflow =
            AnnotatedWorkflowFactory.fromInstance<String, String>(
                RegularWorkflow()
            )

        assertEquals(
            "input-first-second",
            workflow.execute("input")
        )
    }

    @Test
    fun `suspend 함수를 순차적으로 실행한다`() = runTest {
        val workflow =
            AnnotatedWorkflowFactory.fromInstance<String, String>(
                SuspendWorkflow()
            )

        assertEquals(
            "input-first-second",
            workflow.execute("input")
        )
    }

    @Test
    fun `입력 매개변수가 없는 단계를 실행한다`() = runTest {
        val workflow =
            AnnotatedWorkflowFactory.fromInstance<String, String>(
                NoInputWorkflow()
            )

        assertEquals(
            "completed",
            workflow.execute("ignored")
        )
    }

    @Test
    fun `상속된 단계 메서드를 실행한다`() = runTest {
        val workflow =
            AnnotatedWorkflowFactory.fromInstance<String, String>(
                InheritedWorkflow()
            )

        assertEquals(
            "input-validated-processed",
            workflow.execute("input")
        )
    }

    @Test
    fun `실패한 단계는 설정된 횟수만큼 재시도한다`() = runTest {
        val instance = RetryWorkflow()

        val workflow =
            AnnotatedWorkflowFactory.fromInstance<String, String>(
                instance
            )

        assertEquals(
            "input-success",
            workflow.execute("input")
        )

        assertEquals(3, instance.attempts)
    }

    @Test
    fun `단계 실행 시간이 timeout을 초과하면 실패한다`() =
        runTest {
            val workflow =
                AnnotatedWorkflowFactory.fromInstance<String, String>(
                    TimeoutWorkflow()
                )

            assertFailsWith<WorkflowExecutionException> {
                workflow.execute("input")
            }
        }
}
