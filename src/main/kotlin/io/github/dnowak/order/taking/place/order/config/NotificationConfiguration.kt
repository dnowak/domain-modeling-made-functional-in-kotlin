package io.github.dnowak.order.taking.place.order.config

import arrow.core.curried
import io.github.dnowak.order.taking.place.order.implementation.AcknowledgeOrder
import io.github.dnowak.order.taking.place.order.implementation.CreateOrderAcknowledgementLetter
import io.github.dnowak.order.taking.place.order.implementation.HtmlString
import io.github.dnowak.order.taking.place.order.implementation.SendOrderAcknowledgment
import io.github.dnowak.order.taking.place.order.implementation.SendResult
import io.github.dnowak.order.taking.place.order.implementation.acknowledgeOrder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationConfiguration {
    @Bean
    fun sendOrderAcknowledgmentFun(): SendOrderAcknowledgment = { SendResult.Sent }

    @Bean
    fun createAcknowledgementLetter(): CreateOrderAcknowledgementLetter =
        { order -> HtmlString("ACK Letter for $order") }

    @Bean
    fun acknowledgeOrderFun(
        createOrderAcknowledgementLetter: CreateOrderAcknowledgementLetter,
        sendOrderAcknowledgment: SendOrderAcknowledgment,
    ): AcknowledgeOrder = ::acknowledgeOrder.curried()(createOrderAcknowledgementLetter)(sendOrderAcknowledgment)
}
