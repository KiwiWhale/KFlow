package io.github.kiwiwhale.kiwiflow.spring.annotation

import io.github.kiwiwhale.kiwiflow.spring.autoconfigure.KiwiFlowAutoConfiguration
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(KiwiFlowAutoConfiguration::class)
annotation class EnableKiwiFlow