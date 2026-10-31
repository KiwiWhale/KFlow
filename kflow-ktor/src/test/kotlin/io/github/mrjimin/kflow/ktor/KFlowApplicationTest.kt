package io.github.mrjimin.kflow.ktor

import io.github.mrjimin.kflow.core.KFlow
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KFlowApplicationTest {

    private val workflow =
        KFlow.workflow<String, String>("Test") {
            input.uppercase()
        }

    @Test
    fun `Workflow 등록 여부를 확인한다`() = testApplication {
        application {
            install(KFlowPlugin) {
                workflow("Test", workflow)
            }

            assertTrue(kFlow.contains("Test"))
            assertFalse(kFlow.contains("Unknown"))
        }
    }

    @Test
    fun `등록된 Workflow 이름을 조회한다`() = testApplication {
        application {
            install(KFlowPlugin) {
                workflow("Test", workflow)
            }

            assertEquals(
                setOf("Test"),
                kFlow.names()
            )
        }
    }

    @Test
    fun `등록된 Workflow 개수를 조회한다`() = testApplication {
        application {
            install(KFlowPlugin) {
                workflow("Test", workflow)
            }

            assertEquals(1, kFlow.size)
        }
    }
}
