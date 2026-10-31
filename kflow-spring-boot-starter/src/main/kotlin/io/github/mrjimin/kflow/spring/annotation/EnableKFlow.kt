package io.github.mrjimin.kflow.spring.annotation

import io.github.mrjimin.kflow.spring.autoconfigure.KFlowAutoConfiguration
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(KFlowAutoConfiguration::class)
annotation class EnableKFlow