package io.github.mrjimin.kflow.core.execution

import io.github.mrjimin.kflow.core.KFlow
import io.github.mrjimin.kflow.core.exception.WorkflowExecutionException
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ExecutionResultTest {

    @Test
    fun `성공한 실행 결과를 확인할 수 있다`() = runTest {
        val workflow = KFlow.workflow<Int, Int>("계산") {
            step("두 배") {
                execute { input * 2 }
            }
        }

        val result = workflow.executeWithResult(10)

        assertEquals(20, result.value)
        assertEquals(ExecutionStatus.SUCCESS, result.status)
        assertTrue(result.successful)
        assertFalse(result.isFailure)
    }

    @Test
    fun `실패한 실행 결과를 확인할 수 있다`() = runTest {
        val workflow = KFlow.workflow<Unit, Unit>("실패") {
            step("실패") {
                execute {
                    error("실패")
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals(ExecutionStatus.FAILED, result.status)
        assertFalse(result.successful)
        assertTrue(result.isFailure)
        assertNotNull(result.error)
    }

    @Test
    fun `각 실행은 서로 다른 ID를 가진다`() = runTest {
        val workflow = KFlow.workflow<Unit, Unit>("ID") {}

        val first = workflow.executeWithResult(Unit)
        val second = workflow.executeWithResult(Unit)

        assertNotEquals(first.executionId, second.executionId)
    }

    @Test
    fun `실행 결과에서 값을 꺼낼 수 있다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("결과") {
            step("작업") {
                execute { "완료" }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertEquals("완료", result.getOrThrow())
    }

    @Test
    fun `실패한 결과에서 값을 꺼내면 예외가 발생한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("실패") {
            step("작업") {
                execute {
                    error("오류")
                }
            }
        }

        val result = workflow.executeWithResult(Unit)

        val exception = assertFailsWith<WorkflowExecutionException> {
            result.getOrThrow()
        }

        assertEquals("실패", exception.workflowName)
        assertNotNull(exception.cause)
    }

    @Test
    fun `성공한 null 결과도 정상적으로 반환한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String?>("null") {
            step("작업") {
                execute { null }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertTrue(result.successful)
        assertNull(result.getOrThrow())
        assertNull(result.getOrNull())
    }

    @Test
    fun `실패한 결과의 getOrNull은 null을 반환한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("실패") {
            step("작업") {
                execute { error("오류") }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertNull(result.getOrNull())
    }

    @Test
    fun `exceptionOrNull은 실패 원인을 반환한다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("실패") {
            step("작업") {
                execute { error("오류") }
            }
        }

        val result = workflow.executeWithResult(Unit)

        assertSame(result.error, result.exceptionOrNull())
    }
}