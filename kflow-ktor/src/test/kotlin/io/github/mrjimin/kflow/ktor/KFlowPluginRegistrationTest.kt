package io.github.mrjimin.kflow.ktor

import io.github.mrjimin.kflow.core.KFlow
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KFlowPluginRegistrationTest {

    private val helloWorkflow =
        KFlow.workflow<String, String>("Hello") {
            "Hello $input"
        }

    private val paymentWorkflow =
        KFlow.workflow<String, String>("Payment") {
            "Payment $input"
        }

    @Test
    fun `Plugin 설정에서 Workflow를 등록한다`() = testApplication {
        application {
            install(KFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            assertTrue(kFlow.contains("Hello"))

            assertEquals(
                setOf("Hello"),
                kFlow.names()
            )
        }
    }

    @Test
    fun `여러 Workflow를 DSL로 등록한다`() = testApplication {
        application {
            install(KFlowPlugin) {
                workflows(
                    mapOf(
                        "Hello" to helloWorkflow,
                        "Payment" to paymentWorkflow
                    )
                )
            }

            assertEquals(
                setOf("Hello", "Payment"),
                kFlow.names()
            )

            assertEquals(2, kFlow.size)
        }
    }

    @Test
    fun `등록한 Workflow를 타입 지정으로 조회한다`() = testApplication {
        application {
            install(KFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            val result =
                kFlow.workflow<String, String>("Hello")

            assertEquals(
                "Hello Ktor",
                result.execute("Ktor")
            )
        }
    }
}
