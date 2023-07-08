package io.github.dnowak.order.taking.place.order.config

import arrow.core.curried
import arrow.core.flatMap
import arrow.core.partially1
import io.github.dnowak.order.taking.common.OrderLineId
import io.github.dnowak.order.taking.common.OrderQuantity
import io.github.dnowak.order.taking.common.ProductCode
import io.github.dnowak.order.taking.place.order.PlaceOrder
import io.github.dnowak.order.taking.place.order.implementation.*
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
            ProductCode.validate(code).flatMap(::checkProductCode.partially1(checkProductCodeExists))
        }

        val validateLine: ValidateOrderLine =
            ::validateOrderLine
                .partially1(OrderLineId::validate)
                .partially1(validateProductCode)
                .partially1(OrderQuantity::validate)

        val validateOrder: ValidateOrder =
            ::validateOrder.curried()(::validateCustomerInfo)(::validateAddress)(validateLine)

        val priceOrder: PriceOrder = ::priceOrder.partially1(::priceOrderLine.partially1(getProductPrice))

        val placeOrder: PlaceOrder = ::placeOrder.curried()(validateOrder)(priceOrder)(acknowledgeOrder)(::createEvents)

        return placeOrder
    }
}
