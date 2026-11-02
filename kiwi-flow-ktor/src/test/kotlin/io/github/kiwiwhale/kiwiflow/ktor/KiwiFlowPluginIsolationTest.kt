package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.*

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
        lateinit var firstRegistry: WorkflowRegistry
        lateinit var secondRegistry: WorkflowRegistry

        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow("First", firstWorkflow)
                }

                firstRegistry = KiwiFlow.registry

                assertTrue(KiwiFlow.contains("First"))
            }
        }

        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow("Second", secondWorkflow)
                }

                secondRegistry = KiwiFlow.registry

                assertTrue(KiwiFlow.contains("Second"))
                assertFalse(KiwiFlow.contains("First"))
            }
        }

        assertNotSame(firstRegistry, secondRegistry)
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
                    KiwiFlow.names()
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
                    KiwiFlow.names()
                )
            }
        }
    }
}
