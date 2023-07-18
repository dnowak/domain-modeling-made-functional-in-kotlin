package io.github.dnowak.order.taking.place.order.config

import io.github.dnowak.order.taking.common.Price
import io.github.dnowak.order.taking.place.order.implementation.GetProductPrice
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PricingConfiguration {
    @Bean
    fun getProductPriceFun(): GetProductPrice = { _ -> Price.create("10") }
}
