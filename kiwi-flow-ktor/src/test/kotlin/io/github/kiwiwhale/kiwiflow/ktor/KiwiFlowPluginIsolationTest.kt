package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KiwiFlowPluginIsolationTest {

    private val firstWorkflow =
        KiwiFlow.workflow<String, String>("First") {
            "First $input"
        }

    private val secondWorkflow =
        KiwiFlow.workflow<String, String>("Second") {
            "Second $input"
        }

    @Test
    fun `각 Application은 독립적인 Registry를 가진다`() {
        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow("First", firstWorkflow)
                }

                assertTrue(kiwiFlow.contains("First"))
                assertFalse(kiwiFlow.contains("Second"))
            }
        }

        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow("Second", secondWorkflow)
                }

                assertTrue(kiwiFlow.contains("Second"))
                assertFalse(kiwiFlow.contains("First"))
            }
        }
    }

    @Test
    fun `서로 다른 Application의 Workflow 등록 상태가 공유되지 않는다`() {
        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow("First", firstWorkflow)
                }

                assertEquals(
                    setOf("First"),
                    kiwiFlow.keys
                )
            }
        }

        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow("Second", secondWorkflow)
                }

                assertEquals(
                    setOf("Second"),
                    kiwiFlow.keys
                )
            }
        }
    }
}
