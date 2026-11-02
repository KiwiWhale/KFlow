package io.github.kiwiwhale.kiwiflow.ktor.registry

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class WorkflowRegistryTest {

    private val registry = WorkflowRegistry()

    private val workflow =
        KiwiFlow.workflow<String, String>("TestWorkflow") {
            input.uppercase()
        }

    @Test
    fun `Workflow를 등록하고 조회한다`() {
        registry.register("TestWorkflow", workflow)

        assertEquals(
            workflow,
            registry.get("TestWorkflow")
        )
    }

    @Test
    fun `중복된 이름으로 등록하면 예외가 발생한다`() {
        registry.register("TestWorkflow", workflow)

        assertFailsWith<IllegalStateException> {
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
    fun `등록되지 않은 Workflow를 getOrNull로 조회하면 null을 반환한다`() {
        assertNull(registry.getOrNull("Unknown"))
    }

    @Test
    fun `Workflow 등록 여부를 확인한다`() {
        registry.register("TestWorkflow", workflow)

        assertTrue(registry.contains("TestWorkflow"))
    }

    @Test
    fun `등록된 Workflow 개수를 반환한다`() {
        assertEquals(0, registry.size)

        registry.register("TestWorkflow", workflow)

        assertEquals(1, registry.size)
    }

    @Test
    fun `등록된 Workflow 이름을 반환한다`() {
        registry.register("TestWorkflow", workflow)

        assertEquals(
            setOf("TestWorkflow"),
            registry.names()
        )
    }

    @Test
    fun `빈 이름으로 등록하면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            registry.register("", workflow)
        }
    }

    @Test
    fun `공백 이름으로 등록하면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            registry.register("   ", workflow)
        }
    }

    @Test
    fun `타입을 지정하여 Workflow를 조회하고 실행한다`() = runBlocking {
        registry.register("TestWorkflow", workflow)

        val result =
            registry.workflow<String, String>("TestWorkflow")

        assertEquals(
            "HELLO",
            result.execute("hello")
        )
    }

    @Test
    fun `잘못된 이름으로 타입 지정 조회를 하면 예외가 발생한다`() {
        assertFailsWith<NoSuchElementException> {
            registry.workflow<String, String>("Unknown")
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

        assertEquals(
            setOf("First"),
            names
        )

        assertEquals(
            setOf("First", "Second"),
            registry.names()
        )
    }
}