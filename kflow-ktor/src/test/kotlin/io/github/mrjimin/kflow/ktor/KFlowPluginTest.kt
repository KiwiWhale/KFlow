package io.github.mrjimin.kflow.ktor

import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class KFlowPluginTest {

    @Test
    fun `KFlowPlugin을 설치할 수 있다`() = testApplication {
        application {
            install(KFlowPlugin)

            assertEquals(0, kFlow.size)
            assertEquals(emptySet(), kFlow.names())
        }
    }
}