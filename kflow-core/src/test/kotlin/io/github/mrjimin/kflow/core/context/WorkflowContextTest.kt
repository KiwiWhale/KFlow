package io.github.mrjimin.kflow.core.context

import io.github.mrjimin.kflow.core.context.WorkflowContext
import kotlin.test.*

class WorkflowContextTest {

    @Test
    fun `getOrPut은 기존 값을 덮어쓰지 않는다`() {
        val context = WorkflowContext()
        context["count"] = 10

        val result = context.getOrPut<Int>("count") {
            20
        }

        assertEquals(10, result)
    }

    @Test
    fun `require는 타입이 맞지 않으면 예외를 발생시킨다`() {
        val context = WorkflowContext()
        context["count"] = 10

        assertFailsWith<ClassCastException> {
            context.require<String>("count")
        }
    }

    @Test
    fun `require는 존재하지 않는 키에 예외를 발생시킨다`() {
        val context = WorkflowContext()

        assertFailsWith<NoSuchElementException> {
            context.require<String>("missing")
        }
    }

    @Test
    fun `clear는 모든 값을 제거한다`() {
        val context = WorkflowContext()
        context["a"] = 1
        context["b"] = 2

        context.clear()

        assertEquals(0, context.size)
        assertTrue(context.isEmpty)
    }

    @Test
    fun `get은 타입이 맞지 않으면 null을 반환한다`() {
        val context = WorkflowContext()
        context["count"] = 10

        assertNull(context.get<String>("count"))
    }
}
