package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class KiwiFlowPluginExecutionTest {

    private val helloWorkflow =
        KiwiFlow.workflow<String, String>("Hello") {
            "Hello $input"
        }

    private val helloWorkflowKey =
        KiwiWorkflowKey<String, String>("Hello")

    @Test
    fun `등록된 Workflow를 HTTP 요청에서 실행한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            routing {
                get("/hello/{name}") {
                    val name = call.parameters["name"]!!

                    val result = call.application.kiwiFlow
                        .workflow(helloWorkflowKey)
                        .execute(name)

                    call.respondText(result)
                }
            }
        }

        val response = client.get("/hello/Ktor")

        assertEquals(
            "Hello Ktor",
            response.bodyAsText()
        )
    }

    @Test
    fun `동일한 Workflow를 여러 HTTP 요청에서 실행한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            routing {
                get("/hello/{name}") {
                    val name = call.parameters["name"]!!

                    val result = call.application.kiwiFlow
                        .workflow(helloWorkflowKey)
                        .execute(name)

                    call.respondText(result)
                }
            }
        }

        assertEquals(
            "Hello Ktor",
            client.get("/hello/Ktor").bodyAsText()
        )

        assertEquals(
            "Hello Spring",
            client.get("/hello/Spring").bodyAsText()
        )
    }
}
