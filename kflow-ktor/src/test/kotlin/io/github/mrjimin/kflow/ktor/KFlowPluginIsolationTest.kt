package io.github.mrjimin.kflow.ktor

import io.github.mrjimin.kflow.core.KFlow
import io.github.mrjimin.kflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.*

class KFlowPluginIsolationTest {

    private val firstWorkflow =
        KFlow.workflow<String, String>("First") {
            "First $input"
        }

    private val secondWorkflow =
        KFlow.workflow<String, String>("Second") {
            "Second $input"
        }

    @Test
    fun `각 Application은 독립적인 Registry를 가진다`() {
        lateinit var firstRegistry: WorkflowRegistry
        lateinit var secondRegistry: WorkflowRegistry

        testApplication {
            application {
                install(KFlowPlugin) {
                    workflow("First", firstWorkflow)
                }

                firstRegistry = kFlow.registry

                assertTrue(kFlow.contains("First"))
            }
        }

        testApplication {
            application {
                install(KFlowPlugin) {
                    workflow("Second", secondWorkflow)
                }

                secondRegistry = kFlow.registry

                assertTrue(kFlow.contains("Second"))
                assertFalse(kFlow.contains("First"))
            }
        }

        assertNotSame(firstRegistry, secondRegistry)
    }

    @Test
    fun `서로 다른 Application의 Workflow 등록 상태가 공유되지 않는다`() {
        testApplication {
            application {
                install(KFlowPlugin) {
                    workflow("First", firstWorkflow)
                }

                assertEquals(
                    setOf("First"),
                    kFlow.names()
                )
            }
        }

        testApplication {
            application {
                install(KFlowPlugin) {
                    workflow("Second", secondWorkflow)
                }

                assertEquals(
                    setOf("Second"),
                    kFlow.names()
                )
            }
        }
    }
}
