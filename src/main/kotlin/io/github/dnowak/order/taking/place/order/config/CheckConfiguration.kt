package io.github.dnowak.order.taking.place.order.config

import io.github.dnowak.order.taking.place.order.implementation.CheckProductCodeExists
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CheckConfiguration {

    @Bean
    fun checkProductCodeExists(): CheckProductCodeExists = { _ -> true }
}
