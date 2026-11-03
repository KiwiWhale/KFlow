package io.github.kiwiwhale.kiwiflow.ktor.config

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KiwiFlowConfigTest {

    private val config = KiwiFlowConfig()

    private val firstWorkflow =
        KiwiFlow.workflow<String, String>("First") {
            input
        }

    private val secondWorkflow =
        KiwiFlow.workflow<String, String>("Second") {
            input.uppercase()
        }

    private val firstKey =
        KiwiWorkflowKey<String, String>("First")

    private val secondKey =
        KiwiWorkflowKey<String, String>("Second")

    @Test
    fun `Workflow 자체의 Key로 등록한다`() {
        config.workflow(firstWorkflow)

        val expected: Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>> =
            mapOf(firstKey to firstWorkflow)

        assertEquals(
            expected,
            config.registeredWorkflows()
        )
    }

    @Test
    fun `Typed Key로 Workflow를 등록한다`() {
        config.workflow(firstKey, firstWorkflow)

        assertEquals(
            firstWorkflow,
            config.registeredWorkflows()[firstKey]
        )
    }

    @Test
    fun `String 이름으로 Workflow를 등록한다`() {
        config.workflow("First", firstWorkflow)

        assertEquals(
            firstWorkflow,
            config.registeredWorkflows()[firstKey]
        )
    }

    @Test
    fun `Collection으로 여러 Workflow를 등록한다`() {
        config.workflows(
            listOf(firstWorkflow, secondWorkflow)
        )

        assertEquals(
            setOf(firstKey, secondKey),
            config.registeredWorkflows().keys
        )
    }

    @Test
    fun `Typed Key Map으로 여러 Workflow를 등록한다`() {
        config.workflowsByKey(
            mapOf(
                firstKey to firstWorkflow,
                secondKey to secondWorkflow
            )
        )

        assertEquals(
            setOf(firstKey, secondKey),
            config.registeredWorkflows().keys
        )
    }

    @Test
    fun `String Map으로 여러 Workflow를 등록한다`() {
        config.workflowsByName(
            mapOf(
                "First" to firstWorkflow,
                "Second" to secondWorkflow
            )
        )

        assertEquals(
            setOf(firstKey, secondKey),
            config.registeredWorkflows().keys
        )
    }

    @Test
    fun `Key가 Workflow의 Key와 다르면 예외가 발생한다`() {
        val invalidKey =
            KiwiWorkflowKey<String, String>("Different")

        assertFailsWith<IllegalArgumentException> {
            config.workflow(invalidKey, firstWorkflow)
        }

        assertTrue(config.registeredWorkflows().isEmpty())
    }

    @Test
    fun `String 이름이 Workflow 이름과 다르면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            config.workflow("Different", firstWorkflow)
        }

        assertTrue(config.registeredWorkflows().isEmpty())
    }

    @Test
    fun `빈 이름으로 등록하면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            config.workflow("", firstWorkflow)
        }
    }

    @Test
    fun `공백 이름으로 등록하면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            config.workflow("   ", firstWorkflow)
        }
    }

    @Test
    fun `중복된 Workflow를 등록하면 예외가 발생한다`() {
        config.workflow(firstWorkflow)

        assertFailsWith<IllegalStateException> {
            config.workflow(firstWorkflow)
        }

        assertEquals(
            setOf(firstKey),
            config.registeredWorkflows().keys
        )
    }

    @Test
    fun `등록된 Workflow의 Snapshot은 변경되지 않는다`() {
        config.workflow(firstWorkflow)

        val snapshot = config.registeredWorkflows()

        config.workflow(secondWorkflow)

        assertEquals(
            setOf(firstKey),
            snapshot.keys
        )

        assertEquals(
            setOf(firstKey, secondKey),
            config.registeredWorkflows().keys
        )
    }

    @Test
    fun `여러 Workflow 등록 중 중복이 발생하면 전체 등록이 취소된다`() {
        config.workflow(firstWorkflow)

        assertFailsWith<IllegalStateException> {
            config.workflows(
                listOf(secondWorkflow, firstWorkflow)
            )
        }

        assertEquals(
            setOf(firstKey),
            config.registeredWorkflows().keys
        )

        assertFalse(
            secondKey in config.registeredWorkflows()
        )
    }
}
