package io.github.mrjimin.kflow.ktor

import io.github.mrjimin.kflow.core.KFlow
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class KFlowPluginExecutionTest {

    private val helloWorkflow =
        KFlow.workflow<String, String>("Hello") {
            "Hello $input"
        }

    @Test
    fun `등록된 Workflow를 HTTP 요청에서 실행한다`() = testApplication {
        application {
            install(KFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            routing {
                get("/hello/{name}") {
                    val name = call.parameters["name"]!!

                    val result = call.application.kFlow
                        .workflow<String, String>("Hello")
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
            install(KFlowPlugin) {
                workflow("Hello", helloWorkflow)
            }

            routing {
                get("/hello/{name}") {
                    val name = call.parameters["name"]!!

                    val result = call.application.kFlow
                        .workflow<String, String>("Hello")
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
