package io.github.kiwiwhale.kiwiflow.ktor

import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class KiwiFlowPluginTest {

    @Test
    fun `KFlowPlugin을 설치할 수 있다`() = testApplication {
        application {
            install(KiwiFlowPlugin)

            assertEquals(0, KiwiFlow.size)
            assertEquals(emptySet(), KiwiFlow.names())
        }
    }
}