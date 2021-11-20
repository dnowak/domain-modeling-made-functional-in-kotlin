package com.dnowak.order.taking.place.order.implementation

import com.dnowak.order.taking.common.Address
import com.dnowak.order.taking.common.BillingAmount
import com.dnowak.order.taking.common.City
import com.dnowak.order.taking.common.CustomerInfo
import com.dnowak.order.taking.common.EmailAddress
import com.dnowak.order.taking.common.GizmoCode
import com.dnowak.order.taking.common.KilogramQuantity
import com.dnowak.order.taking.common.OrderId
import com.dnowak.order.taking.common.OrderLineId
import com.dnowak.order.taking.common.OrderQuantity
import com.dnowak.order.taking.common.PersonalName
import com.dnowak.order.taking.common.Price
import com.dnowak.order.taking.common.ProductCode
import com.dnowak.order.taking.common.UnitQuantity
import com.dnowak.order.taking.common.WidgetCode
import com.dnowak.order.taking.common.ZipCode
import com.dnowak.order.taking.place.order.PricedOrder
import com.dnowak.order.taking.place.order.PricedOrderLine
import com.dnowak.order.taking.place.order.UnvalidatedAddress
import com.dnowak.order.taking.place.order.UnvalidatedCustomerInfo
import com.dnowak.order.taking.place.order.UnvalidatedOrder
import com.dnowak.order.taking.place.order.UnvalidatedOrderLine
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
        orderLineId = "line-1",
        productCode = "G134",
        quantity = BigDecimal("10.55")
    )
    val unvalidatedOrderLine2 = UnvalidatedOrderLine(
        orderLineId = "line-2",
        productCode = "W1344",
        quantity = BigDecimal("124")
    )
    val unvalidatedOrder = UnvalidatedOrder(
        orderId = "order-1",
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
        orderLineId = OrderLineId.create("line-1"),
        productCode = ProductCode.Gizmo(GizmoCode.create("G134")),
        quantity = OrderQuantity.Kilogram(KilogramQuantity.create(BigDecimal("10.55")))
    )
    val validatedOrderLine2 = ValidatedOrderLine(
        orderLineId = OrderLineId.create("line-2"),
        productCode = ProductCode.Widget(WidgetCode.create("W1344")),
        quantity = OrderQuantity.Unit(UnitQuantity.create(124))
    )
    val validatedOrder = ValidatedOrder(
        orderId = OrderId.create("order-1"),
        customerInfo = validatedCustomerInfo,
        shippingAddress = validatedShippingAddress,
        billingAddress = validatedBillingAddress,
        lines = listOf(
            validatedOrderLine1,
            validatedOrderLine2
        )
    )
    val pricedOrderLine1 = PricedOrderLine(
        orderLineId = OrderLineId.create("line-1"),
        productCode = ProductCode.Gizmo(GizmoCode.create("G134")),
        quantity = OrderQuantity.Kilogram(KilogramQuantity.create("10.55")),
        //1.12 * 10.55 = 11.816
        linePrice = Price.create("11.82")
    )
    val pricedOrderLine2 = PricedOrderLine(
        orderLineId = OrderLineId.create("line-2"),
        productCode = ProductCode.Widget(WidgetCode.create("W1344")),
        quantity = OrderQuantity.Unit(UnitQuantity.create(124)),
        //3.71 * 124 = 460.04
        linePrice = Price.create("460.04")
    )
    val pricedOrder = PricedOrder(
        orderId = OrderId.create("order-1"),
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
