package io.github.kiwiwhale.kiwiflow.spring.annotation

import io.github.kiwiwhale.kiwiflow.spring.autoconfigure.KiwiFlowAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Enables KiwiFlow integration in a Spring application.
 *
 * Imports [KiwiFlowAutoConfiguration], which automatically discovers
 * Spring beans annotated with KiwiFlow's [Workflow] annotation
 * and registers them as executable workflows.
 *
 * Example:
 *
 * ```kotlin
 * @SpringBootApplication
 * @EnableKiwiFlow
 * class Application
 * ```
 *
 * @see Workflow
 * @see KiwiFlowAutoConfiguration
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(KiwiFlowAutoConfiguration::class)
annotation class EnableKiwiFlow