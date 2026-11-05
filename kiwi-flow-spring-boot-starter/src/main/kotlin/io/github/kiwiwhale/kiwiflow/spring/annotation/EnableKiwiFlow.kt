package io.github.kiwiwhale.kiwiflow.spring.annotation

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.spring.autoconfigure.KiwiFlowAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Enables KiwiFlow integration in a Spring application.
 *
 * Imports [KiwiFlowAutoConfiguration] to configure the infrastructure
 * required for Spring-managed workflow discovery and registration.
 *
 * Spring beans annotated with [Workflow] are discovered automatically
 * and registered in the Core workflow registry.
 *
 * Apply this annotation to a Spring configuration class, typically
 * the main application class.
 *
 * Example:
 * ```
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