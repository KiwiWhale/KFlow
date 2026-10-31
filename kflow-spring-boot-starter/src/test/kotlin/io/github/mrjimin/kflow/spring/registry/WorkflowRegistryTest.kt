package io.github.mrjimin.kflow.spring.registry

import io.github.mrjimin.kflow.KFlow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkflowRegistryTest {

    private val registry = WorkflowRegistry()

    @Test
    fun `등록된 워크플로우 이름을 확인한다`() {
        val workflow = KFlow.workflow<Unit, Unit>("TestWorkflow") {}

        registry.register("TestWorkflow", workflow)

        assertTrue(registry.contains("TestWorkflow"))
        assertFalse(registry.contains("UnknownWorkflow"))
    }

    @Test
    fun `등록된 워크플로우 이름 목록을 반환한다`() {
        val first = KFlow.workflow<Unit, Unit>("First") {}
        val second = KFlow.workflow<Unit, Unit>("Second") {}

        registry.register("First", first)
        registry.register("Second", second)

        assertEquals(
            setOf("First", "Second"),
            registry.names()
        )
    }

    @Test
    fun `중복된 이름으로 등록하면 예외가 발생한다`() {
        val first = KFlow.workflow<Unit, Unit>("Test") {}
        val second = KFlow.workflow<Unit, Unit>("Test") {}

        registry.register("Test", first)

        assertFailsWith<IllegalArgumentException> {
            registry.register("Test", second)
        }
    }
}