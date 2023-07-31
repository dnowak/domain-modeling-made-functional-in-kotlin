package io.github.dnowak.order.taking.place.order.implementation

import io.github.dnowak.order.taking.common.Address
import io.github.dnowak.order.taking.common.BillingAmount
import io.github.dnowak.order.taking.common.City
import io.github.dnowak.order.taking.common.CustomerInfo
import io.github.dnowak.order.taking.common.EmailAddress
import io.github.dnowak.order.taking.common.GizmoCode
import io.github.dnowak.order.taking.common.KilogramQuantity
import io.github.dnowak.order.taking.common.OrderId
import io.github.dnowak.order.taking.common.OrderLineId
import io.github.dnowak.order.taking.common.OrderQuantity
import io.github.dnowak.order.taking.common.PersonalName
import io.github.dnowak.order.taking.common.Price
import io.github.dnowak.order.taking.common.ProductCode
import io.github.dnowak.order.taking.common.UnitQuantity
import io.github.dnowak.order.taking.common.WidgetCode
import io.github.dnowak.order.taking.common.ZipCode
import io.github.dnowak.order.taking.place.order.PricedOrder
import io.github.dnowak.order.taking.place.order.PricedOrderLine
import io.github.dnowak.order.taking.place.order.UnvalidatedAddress
import io.github.dnowak.order.taking.place.order.UnvalidatedCustomerInfo
import io.github.dnowak.order.taking.place.order.UnvalidatedOrder
import io.github.dnowak.order.taking.place.order.UnvalidatedOrderLine
import java.math.BigDecimal

object OrderFixture {
    val unvalidatedCustomerInfo = UnvalidatedCustomerInfo(
        firstName = "John",
        lastName = "Doe",
        emailAddress = "john@doe.com"
    )
    val unvalidatedShippingAddress = UnvalidatedAddress(
        "Some Street",
        addressLine2 = null,
        addressLine3 = null,
        addressLine4 = null,
        city = "Los Angeles",
        zipCode = "12456"
    )
    val unvalidatedBillingAddress = UnvalidatedAddress(
        "Some Street",
        addressLine2 = null,
        addressLine3 = null,
        addressLine4 = null,
        city = "Los Angeles",
        zipCode = "72456"
    )
    val unvalidatedOrderLine1 = UnvalidatedOrderLine(
        orderLineId = "LN1",
        productCode = "G134",
        quantity = BigDecimal("10.55")
    )
    val unvalidatedOrderLine2 = UnvalidatedOrderLine(
        orderLineId = "LN2",
        productCode = "W1344",
        quantity = BigDecimal("124")
    )
    val unvalidatedOrder = UnvalidatedOrder(
        orderId = "ORD1",
        customerInfo = unvalidatedCustomerInfo,
        shippingAddress = unvalidatedShippingAddress,
        billingAddress = unvalidatedBillingAddress,
        lines = listOf(
            unvalidatedOrderLine1,
            unvalidatedOrderLine2
        )
    )
    val validatedCustomerInfo = CustomerInfo(
        name = PersonalName(firstName = "John", lastName = "Doe"),
        emailAddress = EmailAddress.create("john@doe.com")
    )
    val validatedShippingAddress = Address(
        "Some Street",
        addressLine2 = null,
        addressLine3 = null,
        addressLine4 = null,
        city = City("Los Angeles"),
        zipCode = ZipCode.create("12456"),
    )
    val validatedBillingAddress = Address(
        "Some Street",
        addressLine2 = null,
        addressLine3 = null,
        addressLine4 = null,
        city = City("Los Angeles"),
        zipCode = ZipCode.create("72456")
    )
    val validatedOrderLine1 = ValidatedOrderLine(
        orderLineId = OrderLineId.create("LN1"),
        productCode = ProductCode.Gizmo(GizmoCode.create("G134")),
        quantity = OrderQuantity.Kilogram(KilogramQuantity.create(BigDecimal("10.55")))
    )
    val validatedOrderLine2 = ValidatedOrderLine(
        orderLineId = OrderLineId.create("LN2"),
        productCode = ProductCode.Widget(WidgetCode.create("W1344")),
        quantity = OrderQuantity.Unit(UnitQuantity.create(124))
    )
    val validatedOrder = ValidatedOrder(
        orderId = OrderId.create("ORD1"),
        customerInfo = validatedCustomerInfo,
        shippingAddress = validatedShippingAddress,
        billingAddress = validatedBillingAddress,
        lines = listOf(
            validatedOrderLine1,
            validatedOrderLine2
        )
    )
    val pricedOrderLine1 = PricedOrderLine(
        orderLineId = OrderLineId.create("LN1"),
        productCode = ProductCode.Gizmo(GizmoCode.create("G134")),
        quantity = OrderQuantity.Kilogram(KilogramQuantity.create("10.55")),
        //1.12 * 10.55 = 11.816
        linePrice = Price.create("11.82")
    )
    val pricedOrderLine2 = PricedOrderLine(
        orderLineId = OrderLineId.create("LN2"),
        productCode = ProductCode.Widget(WidgetCode.create("W1344")),
        quantity = OrderQuantity.Unit(UnitQuantity.create(124)),
        //3.71 * 124 = 460.04
        linePrice = Price.create("460.04")
    )
    val pricedOrder = PricedOrder(
        orderId = OrderId.create("ORD1"),
        customerInfo = validatedCustomerInfo,
        shippingAddress = validatedShippingAddress,
        billingAddress = validatedBillingAddress,
        lines = listOf(
            pricedOrderLine1,
            pricedOrderLine2
        ),
        amountToBill = BillingAmount.create("471.86")
    )
}
