package io.github.mrjimin.kflow.ktor

import io.github.mrjimin.kflow.core.KFlow
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

class KFlowPluginDuplicateTest {

    private val firstWorkflow =
        KFlow.workflow<String, String>("First") {
            "First $input"
        }

    private val secondWorkflow =
        KFlow.workflow<String, String>("Second") {
            "Second $input"
        }

    @Test
    fun `중복된 이름으로 Workflow를 등록하면 예외가 발생한다`() {
        assertFailsWith<IllegalStateException> {
            testApplication {
                application {
                    install(KFlowPlugin) {
                        workflow("Duplicate", firstWorkflow)
                        workflow("Duplicate", secondWorkflow)
                    }
                }
            }
        }
    }
}
