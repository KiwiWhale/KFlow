package io.github.mrjimin.kflow.ktor.config

import io.github.mrjimin.kflow.core.KFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KFlowConfigTest {

    private val config = KFlowConfig()

    private val firstWorkflow =
        KFlow.workflow<String, String>("First") {
            input
        }

    private val secondWorkflow =
        KFlow.workflow<String, String>("Second") {
            input.uppercase()
        }

    @Test
    fun `Workflow를 등록한다`() {
        config.workflow("First", firstWorkflow)

        assertEquals(
            mapOf("First" to firstWorkflow),
            config.registeredWorkflows()
        )
    }

    @Test
    fun `여러 Workflow를 등록한다`() {
        config.workflows(
            mapOf(
                "First" to firstWorkflow,
                "Second" to secondWorkflow
            )
        )

        assertEquals(
            setOf("First", "Second"),
            config.registeredWorkflows().keys
        )
    }

    @Test
    fun `중복된 이름으로 등록하면 예외가 발생한다`() {
        config.workflow("First", firstWorkflow)

        assertFailsWith<IllegalStateException> {
            config.workflow("First", secondWorkflow)
        }
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
    fun `등록된 Workflow의 Snapshot을 반환한다`() {
        config.workflow("First", firstWorkflow)

        val snapshot = config.registeredWorkflows()

        config.workflow("Second", secondWorkflow)

        assertEquals(
            setOf("First"),
            snapshot.keys
        )

        assertEquals(
            setOf("First", "Second"),
            config.registeredWorkflows().keys
        )
    }

    @Test
    fun `여러 Workflow 등록 중 중복이 발생하면 이전 등록은 유지한다`() {
        config.workflow("First", firstWorkflow)

        assertFailsWith<IllegalStateException> {
            config.workflows(
                linkedMapOf(
                    "Second" to secondWorkflow,
                    "First" to firstWorkflow
                )
            )
        }

        assertEquals(
            setOf("First", "Second"),
            config.registeredWorkflows().keys
        )
    }
}