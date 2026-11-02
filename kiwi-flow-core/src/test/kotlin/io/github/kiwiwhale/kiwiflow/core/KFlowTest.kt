package io.github.kiwiwhale.kiwiflow.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KFlowTest {

    @Test
    fun `워크플로우를 생성할 수 있다`() = runTest {
        val workflow = KiwiFlow.workflow<Int, Int>("계산") {
            step("계산") {
                execute { input * 2 }
            }
        }

        assertEquals("계산", workflow.name)
        assertEquals(20, workflow.execute(10))
    }

    @Test
    fun `워크플로우 이름이 비어 있으면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            KiwiFlow.workflow<Unit, Unit>("") {}
        }
    }

    @Test
    fun `워크플로우 이름이 공백이면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            KiwiFlow.workflow<Unit, Unit>("   ") {}
        }
    }
}
