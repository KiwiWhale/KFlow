
package io.github.mrjimin.kflow

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WorkflowTest {

    @Test
    fun `워크플로우가 정상적으로 결과를 반환한다`() = runTest {
        val workflow = KFlow.workflow<String, String>("인사") {
            step("인사말 생성") {
                "안녕하세요, $input"
            }
        }

        val result = workflow.execute("KFlow")

        assertEquals("안녕하세요, KFlow", result)
    }

    @Test
    fun `워크플로우에서 여러 단계를 순서대로 실행한다`() = runTest {
        val executionOrder = mutableListOf<String>()

        val workflow = KFlow.workflow<Unit, String>("순차 실행") {
            step("첫 번째 단계") {
                executionOrder.add("첫 번째")
                "첫 번째 결과"
            }

            step("두 번째 단계") {
                executionOrder.add("두 번째")
                "두 번째 결과"
            }

            step("세 번째 단계") {
                executionOrder.add("세 번째")
                "완료"
            }
        }

        val result = workflow.execute(Unit)

        assertEquals("완료", result)
        assertEquals(
            listOf("첫 번째", "두 번째", "세 번째"),
            executionOrder
        )
    }

    @Test
    fun `이전 단계의 결과를 다음 단계에서 사용할 수 있다`() = runTest {
        val workflow = KFlow.workflow<String, Int>("결과 전달") {
            val greeting = step("인사말 생성") {
                "안녕하세요, $input"
            }

            val uppercase = step("대문자 변환") {
                greeting.uppercase()
            }

            step("문자열 길이 계산") {
                uppercase.length
            }
        }

        val result = workflow.execute("KFlow")

        assertEquals("안녕하세요, KFlow".uppercase().length, result)
    }

    @Test
    fun `워크플로우에서 Suspend 함수를 사용할 수 있다`() = runTest {
        val workflow = KFlow.workflow<Int, Int>("비동기 실행") {
            step("지연 실행") {
                delay(100)
                input * 2
            }
        }

        val result = workflow.execute(21)

        assertEquals(42, result)
    }

    @Test
    fun `워크플로우를 여러 번 실행할 수 있다`() = runTest {
        val workflow = KFlow.workflow<Int, Int>("재사용") {
            step("두 배 계산") {
                input * 2
            }
        }

        assertEquals(20, workflow.execute(10))
        assertEquals(40, workflow.execute(20))
        assertEquals(60, workflow.execute(30))
    }

    @Test
    fun `워크플로우 이름이 비어 있으면 예외가 발생한다`() {
        assertThrows(IllegalArgumentException::class.java) {
            KFlow.workflow<Unit, Unit>("") {
                step("빈 작업") {}
            }
        }
    }

    @Test
    fun `Step 이름이 비어 있으면 예외가 발생한다`() = runTest {
        val workflow = KFlow.workflow<Unit, Unit>("이름 검증") {
            step("") {}
        }

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                workflow.execute(Unit)
            }
        }
    }

    @Test
    fun `Step에서 발생한 예외가 외부로 전파된다`() = runTest {
        val workflow = KFlow.workflow<Unit, String>("예외 처리") {
            step("예외 발생") {
                error("테스트 예외")
            }
        }

        val exception = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                workflow.execute(Unit)
            }
        }

        assertEquals("테스트 예외", exception.message)
    }

    @Test
    fun `워크플로우 실행마다 새로운 Scope를 생성한다`() = runTest {
        var executionCount = 0

        val workflow = KFlow.workflow<Int, Int>("독립 실행") {
            step("실행 횟수 증가") {
                executionCount++
                input + executionCount
            }
        }

        assertEquals(11, workflow.execute(10))
        assertEquals(12, workflow.execute(10))
    }
}
