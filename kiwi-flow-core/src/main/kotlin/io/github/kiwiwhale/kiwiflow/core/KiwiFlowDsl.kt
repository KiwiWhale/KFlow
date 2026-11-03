package io.github.kiwiwhale.kiwiflow.core

/**
 * Marks declarations belonging to the KiwiFlow DSL.
 *
 * Prevents accidental access to outer DSL receivers when
 * multiple KiwiFlow DSL scopes are nested.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class KiwiFlowDsl