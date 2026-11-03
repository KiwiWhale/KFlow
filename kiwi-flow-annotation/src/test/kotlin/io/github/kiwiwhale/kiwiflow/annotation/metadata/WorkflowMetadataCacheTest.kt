package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class WorkflowMetadataCacheTest {

    @Workflow
    class SampleWorkflow {

        @Step
        fun first() {}

        @Step
        fun second() {}
    }

    @Workflow
    class AnotherWorkflow {

        @Step
        fun execute() {}
    }

    private val cache = WorkflowMetadataCache(
        inspector = WorkflowInspector()
    )

    @Test
    fun `동일한 클래스의 메타데이터는 캐싱된다`() {
        val first = cache.get(SampleWorkflow::class)
        val second = cache.get(SampleWorkflow::class)

        assertSame(first, second)
    }

    @Test
    fun `서로 다른 클래스는 별도의 메타데이터를 생성한다`() {
        val first = cache.get(SampleWorkflow::class)
        val second = cache.get(AnotherWorkflow::class)

        assertNotSame(first, second)
    }

    @Test
    fun `캐싱된 메타데이터의 워크플로우 이름을 유지한다`() {
        val metadata = cache.get(SampleWorkflow::class)

        assertEquals("SampleWorkflow", metadata.name)
    }

    @Test
    fun `캐싱된 메타데이터의 단계 목록을 유지한다`() {
        val metadata = cache.get(SampleWorkflow::class)

        assertEquals(2, metadata.steps.size)
        assertEquals(
            listOf("first", "second"),
            metadata.steps.map { it.name }
        )
    }

    @Test
    fun `여러 번 조회해도 단계 정보가 변경되지 않는다`() {
        val first = cache.get(SampleWorkflow::class)

        repeat(5) {
            cache.get(SampleWorkflow::class)
        }

        val last = cache.get(SampleWorkflow::class)

        assertSame(first, last)
        assertEquals(2, last.steps.size)
    }
}
