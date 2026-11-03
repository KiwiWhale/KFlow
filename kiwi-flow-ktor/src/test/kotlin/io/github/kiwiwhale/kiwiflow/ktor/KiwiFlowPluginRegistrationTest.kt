package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
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

            assertTrue(KiwiFlow.contains("Hello"))

            assertEquals(
                setOf("Hello"),
                KiwiFlow.names()
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
                KiwiFlow.names()
            )

            assertEquals(2, KiwiFlow.size)
        }
    }

    @Test
    fun `등록한 Workflow를 타입 지정으로 조회한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            val result =
                KiwiFlow.workflow<String, String>("Hello")

            assertEquals(
                "Hello Ktor",
                result.execute("Ktor")
            )
        }
    }
}
