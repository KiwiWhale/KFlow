package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.exception.StepException
import io.github.kiwiwhale.kiwiflow.core.exception.WorkflowException
import io.github.kiwiwhale.kiwiflow.core.exception.WorkflowExecutionException
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class WorkflowTest {

    @Test
    fun `워크플로우가 정상적인 결과를 반환한다`() = runTest {
        val workflow = KiwiFlow.workflow<String, String>("인사") {
            step("인사말 생성") {
                execute { "안녕하세요, $input" }
            }
        }

        assertEquals("안녕하세요, KiwiFlow", workflow.execute("KiwiFlow"))
    }

    @Test
    fun `여러 단계를 순서대로 실행한다`() = runTest {
        val order = mutableListOf<String>()

        val workflow = KiwiFlow.workflow<Unit, String>("순서") {
            step("첫 번째") {
                execute {
                    order.add("첫 번째")
                    "A"
                }
            }

            step("두 번째") {
                execute {
                    order.add("두 번째")
                    "B"
                }
            }

            step("세 번째") {
                execute {
                    order.add("세 번째")
                    "완료"
                }
            }
        }

        workflow.execute(Unit)

        assertEquals(
            listOf("첫 번째", "두 번째", "세 번째"),
            order
        )
    }

    @Test
    fun `이전 단계의 결과를 다음 단계에서 사용할 수 있다`() = runTest {
        val workflow = KiwiFlow.workflow<Int, Int>("결과 전달") {
            val first = step("두 배") {
                execute { input * 2 }
            }

            step("더하기") {
                execute { first + 10 }
            }
        }

        assertEquals(30, workflow.execute(10))
    }

    @Test
    fun `워크플로우를 여러 번 실행할 수 있다`() = runTest {
        val workflow = KiwiFlow.workflow<Int, Int>("재사용") {
            step("계산") {
                execute { input * 2 }
            }
        }

        assertEquals(20, workflow.execute(10))
        assertEquals(40, workflow.execute(20))
    }

    @Test
    fun `워크플로우를 동시에 실행해도 결과가 섞이지 않는다`() = runTest {
        val workflow = KiwiFlow.workflow<Int, Int>("동시 실행") {
            step("계산") {
                execute {
                    delay(100.milliseconds)
                    input * 2
                }
            }
        }

        val results = (1..10).map { input ->
            async {
                workflow.execute(input)
            }
        }.awaitAll()

        assertEquals(
            (2..20 step 2).toList(),
            results
        )
    }

    @Test
    fun `워크플로우에서 발생한 일반 예외가 실행 예외로 변환된다`() = runTest {
        val workflow = KiwiFlow.workflow<Unit, String>("실패") {
            step("실패") {
                execute {
                    error("실패 테스트")
                }
            }
        }

        val exception = assertFailsWith<WorkflowExecutionException> {
            workflow.execute(Unit)
        }

        assertEquals("실패", exception.workflowName)
        assertNotNull(exception.executionId)
        assertTrue(exception.executionId.isNotBlank())
    }

    @Test
    fun `StepException의 원인이 보존된다`() = runTest {
        val workflow = KiwiFlow.workflow<Unit, String>("실패") {
            step("실패") {
                execute {
                    error("실패 테스트")
                }
            }
        }

        val exception = assertFailsWith<WorkflowExecutionException> {
            workflow.execute(Unit)
        }

        val stepException = assertIs<StepException>(exception.cause)

        val original = assertIs<IllegalStateException>(stepException.cause)

        assertEquals("실패 테스트", original.message)
    }

    @Test
    fun `WorkflowException은 워크플로우 이름을 포함한다`() {
        val exception = WorkflowException(
            workflowName = "order-workflow",
            message = "Order processing failed"
        )

        assertEquals(
            "Workflow 'order-workflow': Order processing failed",
            exception.message
        )

        assertEquals("order-workflow", exception.workflowName)
    }

    @Test
    fun `WorkflowExecutionException은 WorkflowException을 상속한다`() {
        val cause = IllegalStateException("실패")

        val exception = WorkflowExecutionException(
            executionId = "execution-1",
            workflowName = "order-workflow",
            cause = cause
        )

        assertIs<WorkflowException>(exception)
        assertEquals("order-workflow", exception.workflowName)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `WorkflowException이 발생하면 실행 결과에 실패가 기록된다`() = runTest {
        val workflow = KiwiFlow.workflow<Unit, String>("사용자 정의 예외") {
            step("실패") {
                execute {
                    throw WorkflowException(
                        workflowName = "custom-workflow",
                        message = "사용자 정의 실패"
                    )
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(ExecutionStatus.FAILED, result.status)

        val error = assertIs<WorkflowExecutionException>(result.error)

        assertEquals("사용자 정의 예외", error.workflowName)

        val stepException = assertIs<StepException>(error.cause)

        val original = assertIs<WorkflowException>(stepException.cause)

        assertEquals("custom-workflow", original.workflowName)
        assertEquals(
            "Workflow 'custom-workflow': 사용자 정의 실패",
            original.message
        )
    }

    @Test
    fun `이미 생성된 WorkflowExecutionException이 Step에서 발생하면 원인이 보존된다`() = runTest {
        val original = WorkflowExecutionException(
            executionId = "execution-original",
            workflowName = "original-workflow",
            cause = IllegalStateException("실패")
        )

        val workflow = KiwiFlow.workflow<Unit, String>("실행 예외") {
            step("실패") {
                execute {
                    throw original
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(ExecutionStatus.FAILED, result.status)

        val error = assertIs<WorkflowExecutionException>(result.error)

        assertEquals("실행 예외", error.workflowName)

        val stepException = assertIs<StepException>(error.cause)

        val originalError = assertIs<WorkflowExecutionException>(
            stepException.cause
        )

        assertEquals("execution-original", originalError.executionId)
        assertEquals("original-workflow", originalError.workflowName)
    }

    @Test
    fun `실패한 워크플로우는 다음 단계를 실행하지 않는다`() = runTest {
        var secondStepExecuted = false

        val workflow = KiwiFlow.workflow<Unit, Unit>("실패 중단") {
            step("실패") {
                execute {
                    error("실패")
                }
            }

            step("다음 단계") {
                execute {
                    secondStepExecuted = true
                }
            }
        }

        assertFailsWith<WorkflowExecutionException> {
            workflow.execute(Unit)
        }

        assertFalse(secondStepExecuted)
    }

    @Test
    fun `실패한 실행 결과에 실행 정보가 포함된다`() = runTest {
        val workflow = KiwiFlow.workflow<Unit, String>("실패 정보") {
            step("실패") {
                execute {
                    error("실패")
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(ExecutionStatus.FAILED, result.status)
        assertEquals("실패 정보", result.workflowName)
        assertTrue(result.executionId.isNotBlank())
        assertNotNull(result.error)
        assertTrue(result.duration.isFinite())
    }
}
