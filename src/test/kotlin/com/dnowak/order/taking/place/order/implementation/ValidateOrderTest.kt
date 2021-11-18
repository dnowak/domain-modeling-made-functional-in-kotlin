package com.dnowak.order.taking.place.order.implementation

import com.dnowak.order.taking.common.Address
import com.dnowak.order.taking.common.City
import com.dnowak.order.taking.common.CustomerInfo
import com.dnowak.order.taking.common.EmailAddress
import com.dnowak.order.taking.common.GizmoCode
import com.dnowak.order.taking.common.KilogramQuantity
import com.dnowak.order.taking.common.OrderId
import com.dnowak.order.taking.common.OrderLineId
import com.dnowak.order.taking.common.OrderQuantity
import com.dnowak.order.taking.common.PersonalName
import com.dnowak.order.taking.common.ProductCode
import com.dnowak.order.taking.common.UnitQuantity
import com.dnowak.order.taking.common.WidgetCode
import com.dnowak.order.taking.common.ZipCode
import com.dnowak.order.taking.place.order.UnvalidatedAddress
import com.dnowak.order.taking.place.order.UnvalidatedCustomerInfo
import com.dnowak.order.taking.place.order.UnvalidatedOrder
import com.dnowak.order.taking.place.order.UnvalidatedOrderLine
import io.kotest.assertions.arrow.core.shouldBeInvalid
import io.kotest.assertions.arrow.core.shouldBeValid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

internal class ValidateOrderTest : DescribeSpec({
    describe("validateOrder") {
        it("reports missing orderId") {
            val unvalidatedOrder = UnvalidatedOrder(
                orderId = "order-1",
                customerInfo = UnvalidatedCustomerInfo(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com"
                ),
                shippingAddress = UnvalidatedAddress(
                    "Some Street",
                    addressLine2 = null,
                    addressLine3 = null,
                    addressLine4 = null,
                    city = "Los Angeles",
                    zipCode = "12456"
                ),
                billingAddress = UnvalidatedAddress(
                    "Some Street",
                    addressLine2 = null,
                    addressLine3 = null,
                    addressLine4 = null,
                    city = "Los Angeles",
                    zipCode = "72456"
                ),
                lines = listOf(
                    UnvalidatedOrderLine(
                        orderLineId = "line-1",
                        productCode = "G134",
                        quantity = BigDecimal.ONE
                    ),
                    UnvalidatedOrderLine(
                        orderLineId = "line-2",
                        productCode = "W1344",
                        quantity = BigDecimal.ONE
                    )
                )
            )
            val validatedOrder = ValidatedOrder(
                orderId = OrderId.create("order-1"),
                customerInfo = CustomerInfo(
                    name = PersonalName(firstName = "John", lastName = "Doe"),
                    emailAddress = EmailAddress.create("john@doe.com")
                ),
                shippingAddress = Address(
                    "Some Street",
                    addressLine2 = null,
                    addressLine3 = null,
                    addressLine4 = null,
                    city = City("Los Angeles"),
                    zipCode = ZipCode.create("12456"),
                ),
                billingAddress = Address(
                    "Some Street",
                    addressLine2 = null,
                    addressLine3 = null,
                    addressLine4 = null,
                    city = City("Los Angeles"),
                    zipCode = ZipCode.create("72456")
                ),
                lines = listOf(
                    ValidatedOrderLine(
                        orderLineId = OrderLineId.create("line-1"),
                        productCode = ProductCode.Gizmo(GizmoCode.create("G134")),
                        quantity = OrderQuantity.Kilogram(KilogramQuantity.create(BigDecimal.ONE))
                    ),
                    ValidatedOrderLine(
                        orderLineId = OrderLineId.create("line-2"),
                        productCode = ProductCode.Widget(WidgetCode.create("W1344")),
                        quantity = OrderQuantity.Unit(UnitQuantity.create(1))
                    )
                )
            )
            validateOrder(unvalidatedOrder).shouldBeValid() shouldBe validatedOrder
        }
    }
})
