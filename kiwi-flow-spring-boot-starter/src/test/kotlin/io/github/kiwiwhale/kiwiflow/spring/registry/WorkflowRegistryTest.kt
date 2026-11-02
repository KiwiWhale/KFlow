package io.github.kiwiwhale.kiwiflow.spring.registry

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkflowRegistryTest {

    private val registry = WorkflowRegistry()

    @Test
    fun `Workflow를 등록하고 조회한다`() {
        val workflow = KiwiFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        registry.register("TestWorkflow", workflow)

        assertTrue(registry.contains("TestWorkflow"))
        assertEquals(workflow, registry.get("TestWorkflow"))
    }

    @Test
    fun `중복된 이름으로 등록하면 예외가 발생한다`() {
        val workflow = KiwiFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        registry.register("TestWorkflow", workflow)

        assertFailsWith<IllegalArgumentException> {
            registry.register("TestWorkflow", workflow)
        }
    }

    @Test
    fun `등록되지 않은 Workflow를 조회하면 예외가 발생한다`() {
        assertFailsWith<NoSuchElementException> {
            registry.get("Unknown")
        }
    }

    @Test
    fun `getOrNull로 등록된 Workflow를 조회한다`() {
        val workflow = KiwiFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        registry.register("TestWorkflow", workflow)

        assertEquals(workflow, registry.getOrNull("TestWorkflow"))
    }

    @Test
    fun `등록되지 않은 Workflow를 getOrNull로 조회하면 null을 반환한다`() {
        assertNull(registry.getOrNull("Unknown"))
    }

    @Test
    fun `등록된 Workflow 이름을 조회한다`() {
        val workflow = KiwiFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        registry.register("TestWorkflow", workflow)

        assertEquals(setOf("TestWorkflow"), registry.names())
    }

    @Test
    fun `빈 이름으로 Workflow를 등록하면 예외가 발생한다`() {
        val workflow = KiwiFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        assertFailsWith<IllegalArgumentException> {
            registry.register("", workflow)
        }
    }

    @Test
    fun `공백 이름으로 Workflow를 등록하면 예외가 발생한다`() {
        val workflow = KiwiFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        assertFailsWith<IllegalArgumentException> {
            registry.register("   ", workflow)
        }
    }

    @Test
    fun `names는 등록 시점의 Snapshot을 반환한다`() {
        val first = KiwiFlow.workflow<String, String>("First") {
            input
        }

        val second = KiwiFlow.workflow<String, String>("Second") {
            input
        }

        registry.register("First", first)

        val names = registry.names()

        registry.register("Second", second)

        assertEquals(setOf("First"), names)
        assertEquals(setOf("First", "Second"), registry.names())
    }
}
