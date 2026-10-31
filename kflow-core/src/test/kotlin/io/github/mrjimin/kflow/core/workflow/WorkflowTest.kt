package io.github.mrjimin.kflow.core.workflow

import io.github.mrjimin.kflow.core.KFlow
import io.github.mrjimin.kflow.core.exception.StepException
import io.github.mrjimin.kflow.core.exception.WorkflowExecutionException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class WorkflowTest {

    @Test
    fun `워크플로우가 정상적인 결과를 반환한다`() = runTest {
        val workflow = KFlow.workflow<String, String>("인사") {
            step("인사말 생성") {
                execute { "안녕하세요, $input" }
            }
        }

        assertEquals("안녕하세요, KFlow", workflow.execute("KFlow"))
    }

    @Test
    fun `여러 단계를 순서대로 실행한다`() = runTest {
        val order = mutableListOf<String>()

        val workflow = KFlow.workflow<Unit, String>("순서") {
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
        val workflow = KFlow.workflow<Int, Int>("결과 전달") {
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
        val workflow = KFlow.workflow<Int, Int>("재사용") {
            step("계산") {
                execute { input * 2 }
            }
        }

        assertEquals(20, workflow.execute(10))
        assertEquals(40, workflow.execute(20))
    }

    @Test
    fun `워크플로우를 동시에 실행해도 결과가 섞이지 않는다`() = runTest {
        val workflow = KFlow.workflow<Int, Int>("동시 실행") {
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
    fun `워크플로우에서 발생한 예외가 전파된다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("실패") {
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

        assertIs<IllegalStateException>(stepException.cause)

        assertEquals("실패 테스트", stepException.cause?.message)
    }

    @Test
    fun `실패한 워크플로우는 다음 단계를 실행하지 않는다`() = runTest {
        var secondStepExecuted = false

        val workflow = KFlow.workflow<Unit, Unit>("실패 중단") {
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
}
