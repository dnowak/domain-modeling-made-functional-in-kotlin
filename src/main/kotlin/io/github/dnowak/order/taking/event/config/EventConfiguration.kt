package io.github.dnowak.order.taking.event.config

import arrow.core.partially1
import io.github.dnowak.order.taking.event.PublishEvent
import io.github.dnowak.order.taking.event.publishToSpring
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventConfiguration {
    @Bean
    fun publishEventFun(eventPublisher: ApplicationEventPublisher): PublishEvent =
        ::publishToSpring.partially1(eventPublisher)
}
