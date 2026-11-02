package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertFailsWith

class KiwiFlowPluginDuplicateTest {

    private val firstWorkflow =
        KiwiFlow.workflow<String, String>("First") {
            "First $input"
        }

    private val secondWorkflow =
        KiwiFlow.workflow<String, String>("Second") {
            "Second $input"
        }

    @Test
    fun `중복된 이름으로 Workflow를 등록하면 예외가 발생한다`() {
        assertFailsWith<IllegalStateException> {
            testApplication {
                application {
                    install(KiwiFlowPlugin) {
                        workflow("Duplicate", firstWorkflow)
                        workflow("Duplicate", secondWorkflow)
                    }
                }
            }
        }
    }
}
