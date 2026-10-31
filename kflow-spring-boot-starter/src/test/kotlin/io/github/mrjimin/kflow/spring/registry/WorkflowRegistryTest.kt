package io.github.mrjimin.kflow.spring.registry

import io.github.mrjimin.kflow.KFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WorkflowRegistryTest {

    private val registry = WorkflowRegistry()

    @Test
    fun `Workflow를 등록하고 조회한다`() {
        val workflow = KFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        registry.register("TestWorkflow", workflow)

        assertTrue(registry.contains("TestWorkflow"))
        assertEquals(workflow, registry.get("TestWorkflow"))
    }

    @Test
    fun `중복된 이름으로 등록하면 예외가 발생한다`() {
        val workflow = KFlow.workflow<String, String>("TestWorkflow") {
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
    fun `등록된 Workflow 이름을 조회한다`() {
        val workflow = KFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        registry.register("TestWorkflow", workflow)

        assertEquals(setOf("TestWorkflow"), registry.names())
    }

    @Test
    fun `빈 이름으로 Workflow를 등록하면 예외가 발생한다`() {
        val workflow = KFlow.workflow<String, String>("TestWorkflow") {
            input
        }

        assertFailsWith<IllegalArgumentException> {
            registry.register("", workflow)
        }
    }
}
