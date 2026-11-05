package io.github.kiwiwhale.kiwiflow.annotation.metadata

import kotlin.test.Test
import kotlin.test.assertFailsWith

class WorkflowMetadataTest {

    @Test
    fun `빈 워크플로우 이름은 거부한다`() {
        assertFailsWith<IllegalArgumentException> {
            WorkflowMetadata(
                name = "",
                steps = emptyList()
            )
        }
    }

    @Test
    fun `공백으로만 구성된 워크플로우 이름은 거부한다`() {
        assertFailsWith<IllegalArgumentException> {
            WorkflowMetadata(
                name = "   ",
                steps = emptyList()
            )
        }
    }
}