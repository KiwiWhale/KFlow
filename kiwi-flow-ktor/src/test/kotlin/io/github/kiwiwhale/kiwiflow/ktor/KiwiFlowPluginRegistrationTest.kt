package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KiwiFlowPluginRegistrationTest {

    private val helloWorkflow =
        KiwiFlow.workflow<String, String>("Hello") {
            "Hello $input"
        }

    private val paymentWorkflow =
        KiwiFlow.workflow<String, String>("Payment") {
            "Payment $input"
        }

    @Test
    fun `Plugin 설정에서 Workflow를 등록한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            assertTrue(kiwiFlow.contains("Hello"))

            assertEquals(
                setOf("Hello"),
                kiwiFlow.keys
            )
        }
    }

    @Test
    fun `여러 Workflow를 DSL로 등록한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflowsByName(
                    mapOf(
                        "Hello" to helloWorkflow,
                        "Payment" to paymentWorkflow
                    )
                )
            }

            assertEquals(
                setOf("Hello", "Payment"),
                kiwiFlow.keys
            )

            assertEquals(2, kiwiFlow.size)
        }
    }

    @Test
    fun `등록한 Workflow를 타입 지정으로 조회한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            val key = KiwiWorkflowKey<String, String>("Hello")
            val result = kiwiFlow.workflow(key)

            assertEquals(
                "Hello Ktor",
                result.execute("Ktor")
            )
        }
    }
}
