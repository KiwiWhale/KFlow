package io.github.kiwiwhale.kiwiflow.ktor

import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class KiwiFlowPluginTest {

    @Test
    fun `KiwiFlowPlugin을 설치할 수 있다`() = testApplication {
        application {
            install(KiwiFlowPlugin)

            assertEquals(0, kiwiFlow.size)
            assertEquals(emptySet(), kiwiFlow.keys())
        }
    }
}