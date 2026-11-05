package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KiwiFlowApplicationTest {

    private val workflow =
        KiwiFlow.workflow<String, String>("Test") {
            input.uppercase()
        }

    @Test
    fun `Workflow 등록 여부를 확인한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Test", workflow)
            }

            assertTrue(kiwiFlow.contains("Test"))
            assertFalse(kiwiFlow.contains("Unknown"))
        }
    }

    @Test
    fun `등록된 Workflow 이름을 조회한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Test", workflow)
            }

            assertEquals(
                setOf("Test"),
                kiwiFlow.keys()
            )
        }
    }

    @Test
    fun `등록된 Workflow 개수를 조회한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow("Test", workflow)
            }

            assertEquals(1, kiwiFlow.size)
        }
    }
}