package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class WorkflowMetadataCacheTest {

    @Workflow(name = "default")
    class TestWorkflow {

        @Step
        fun execute(input: String): String = input
    }

    private fun createCache() =
        WorkflowMetadataCache(WorkflowInspector())

    @Test
    fun `기본 메타데이터를 캐싱한다`() {
        val cache = createCache()

        val first = cache.get(TestWorkflow::class)
        val second = cache.get(TestWorkflow::class)

        assertSame(first, second)
        assertEquals(1, cache.size)
    }

    @Test
    fun `명시적 설정을 별도로 캐싱한다`() {
        val cache = createCache()

        val first = cache.get(
            TestWorkflow::class,
            Workflow(name = "explicit")
        )

        val second = cache.get(
            TestWorkflow::class,
            Workflow(name = "explicit")
        )

        assertSame(first, second)
        assertEquals("explicit", first.name)
        assertEquals(1, cache.size)
    }

    @Test
    fun `기본 설정과 명시적 설정은 분리된다`() {
        val cache = createCache()

        val default = cache.get(TestWorkflow::class)

        val explicit = cache.get(
            TestWorkflow::class,
            Workflow(name = "explicit")
        )

        assertNotSame(default, explicit)
        assertEquals("default", default.name)
        assertEquals("explicit", explicit.name)
        assertEquals(2, cache.size)
    }

    @Test
    fun `서로 다른 명시적 설정은 별도로 캐싱한다`() {
        val cache = createCache()

        val first = cache.get(
            TestWorkflow::class,
            Workflow(name = "first")
        )

        val second = cache.get(
            TestWorkflow::class,
            Workflow(name = "second")
        )

        assertNotSame(first, second)
        assertEquals(2, cache.size)
    }

    @Test
    fun `evict는 해당 클래스의 캐시만 제거한다`() {
        val cache = createCache()

        cache.get(TestWorkflow::class)
        cache.get(
            TestWorkflow::class,
            Workflow(name = "explicit")
        )

        cache.evict(TestWorkflow::class)

        assertEquals(0, cache.size)
    }

    @Test
    fun `clear는 모든 캐시를 제거한다`() {
        val cache = createCache()

        cache.get(TestWorkflow::class)
        cache.get(
            TestWorkflow::class,
            Workflow(name = "explicit")
        )

        cache.clear()

        assertEquals(0, cache.size)
    }

    @Test
    fun `메타데이터 검사 실패 결과는 캐싱되지 않는다`() {
        class InvalidWorkflow

        val cache = createCache()

        assertFailsWith<Exception> {
            cache.get(InvalidWorkflow::class)
        }

        assertEquals(0, cache.size)
    }

    @Test
    fun `동시 조회에서도 동일한 메타데이터를 반환한다`() {
        val cache = createCache()

        val executor = Executors.newFixedThreadPool(8)

        try {
            val results = (1..32).map {
                executor.submit<WorkflowMetadata> {
                    cache.get(TestWorkflow::class)
                }
            }.map { it.get() }

            val first = results.first()

            results.forEach {
                assertSame(first, it)
            }

            assertEquals(1, cache.size)
        } finally {
            executor.shutdownNow()
        }
    }
}
