package io.github.dnowak.order.taking.place.order.config

import arrow.core.andThen
import arrow.core.curried
import arrow.core.partially1
import io.github.dnowak.order.taking.common.OrderLineId
import io.github.dnowak.order.taking.common.OrderQuantity
import io.github.dnowak.order.taking.common.ProductCode
import io.github.dnowak.order.taking.place.order.PlaceOrder
import io.github.dnowak.order.taking.place.order.implementation.AcknowledgeOrder
import io.github.dnowak.order.taking.place.order.implementation.CheckProductCodeExists
import io.github.dnowak.order.taking.place.order.implementation.GetProductPrice
import io.github.dnowak.order.taking.place.order.implementation.PriceOrder
import io.github.dnowak.order.taking.place.order.implementation.ValidateOrder
import io.github.dnowak.order.taking.place.order.implementation.ValidateOrderLine
import io.github.dnowak.order.taking.place.order.implementation.ValidateProductCode
import io.github.dnowak.order.taking.place.order.implementation.checkProductCode
import io.github.dnowak.order.taking.place.order.implementation.createEvents
import io.github.dnowak.order.taking.place.order.implementation.placeOrder
import io.github.dnowak.order.taking.place.order.implementation.priceOrder
import io.github.dnowak.order.taking.place.order.implementation.priceOrderLine
import io.github.dnowak.order.taking.place.order.implementation.validateAddress
import io.github.dnowak.order.taking.place.order.implementation.validateCustomerInfo
import io.github.dnowak.order.taking.place.order.implementation.validateOrder
import io.github.dnowak.order.taking.place.order.implementation.validateOrderLine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PlaceOrderConfiguration {

    @Bean
    fun placeOrderFun(
        checkProductCodeExists: CheckProductCodeExists,
        getProductPrice: GetProductPrice,
        acknowledgeOrder: AcknowledgeOrder,
    ): PlaceOrder {
        val validateProductCode: ValidateProductCode = { code ->
            ProductCode.validate(code).andThen(::checkProductCode.partially1(checkProductCodeExists))
        }

        val validateLine: ValidateOrderLine =
            ::validateOrderLine.curried()(OrderLineId::validate)(validateProductCode)(OrderQuantity::validate)

        val validateOrder: ValidateOrder =
            ::validateOrder.curried()(::validateCustomerInfo)(::validateAddress)(validateLine)

        val priceOrder: PriceOrder = ::priceOrder.partially1(::priceOrderLine.partially1(getProductPrice))

        val placeOrder: PlaceOrder = ::placeOrder.curried()(validateOrder)(priceOrder)(acknowledgeOrder)(::createEvents)

        return placeOrder
    }
}
