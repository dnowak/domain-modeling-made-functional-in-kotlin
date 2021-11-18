package com.dnowak.order.taking.place.order.implementation

import arrow.core.curried
import arrow.core.invalid
import arrow.core.invalidNel
import arrow.core.nel
import arrow.core.nonEmptyListOf
import arrow.core.validNel
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
import com.dnowak.order.taking.common.Property
import com.dnowak.order.taking.common.PropertyValidationError
import com.dnowak.order.taking.common.UnitQuantity
import com.dnowak.order.taking.common.WidgetCode
import com.dnowak.order.taking.common.ZipCode
import com.dnowak.order.taking.common.assign
import com.dnowak.order.taking.place.order.UnvalidatedAddress
import com.dnowak.order.taking.place.order.UnvalidatedCustomerInfo
import com.dnowak.order.taking.place.order.UnvalidatedOrder
import com.dnowak.order.taking.place.order.UnvalidatedOrderLine
import io.kotest.assertions.arrow.core.shouldBeInvalid
import io.kotest.assertions.arrow.core.shouldBeValid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal

internal class ValidateOrderTest : DescribeSpec({
    beforeTest { clearAllMocks() }
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
        quantity = BigDecimal.ONE
    )
    val unvalidatedOrderLine2 = UnvalidatedOrderLine(
        orderLineId = "line-2",
        productCode = "W1344",
        quantity = BigDecimal.ONE
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
        quantity = OrderQuantity.Kilogram(KilogramQuantity.create(BigDecimal.ONE))
    )
    val validatedOrderLine2 = ValidatedOrderLine(
        orderLineId = OrderLineId.create("line-2"),
        productCode = ProductCode.Widget(WidgetCode.create("W1344")),
        quantity = OrderQuantity.Unit(UnitQuantity.create(1))
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
    describe("validateOrder") {
        val validateCustomerInfo: ValidateCustomerInfo = mockk()
        val validateAddress: ValidateAddress = mockk()
        val validateOrderLine: ValidateOrderLine = mockk()
        val validate: ValidateOrder =
            ::validateOrder.curried()(validateCustomerInfo)(validateAddress)(validateOrderLine)

        beforeTest {
            every { validateCustomerInfo(unvalidatedCustomerInfo) } returns validatedCustomerInfo.validNel()
            every { validateAddress(unvalidatedShippingAddress) } returns validatedShippingAddress.validNel()
            every { validateAddress(unvalidatedBillingAddress) } returns validatedBillingAddress.validNel()
            every { validateOrderLine(unvalidatedOrderLine1) } returns validatedOrderLine1.validNel()
            every { validateOrderLine(unvalidatedOrderLine2) } returns validatedOrderLine2.validNel()
        }
        it("validates correct order") {
            validate(unvalidatedOrder).shouldBeValid() shouldBe validatedOrder
        }
        it("validates orderId") {
            validate(unvalidatedOrder.copy(orderId = "")).shouldBeInvalid().all shouldExist { error ->
                error.path == nonEmptyListOf(Property("orderId"))
            }
        }
        it("validates properties") {
            every { validateCustomerInfo(any()) } returns PropertyValidationError(
                Property("customerProperty").nel(),
                "invalid"
            ).invalidNel()
            every { validateAddress(unvalidatedShippingAddress) } returns nonEmptyListOf(
                PropertyValidationError(Property("line1").nel(), "invalid line 1"),
                PropertyValidationError(Property("city1").nel(), "invalid city 1"),
            ).invalid()
            every { validateAddress(unvalidatedBillingAddress) } returns nonEmptyListOf(
                PropertyValidationError(Property("line2").nel(), "invalid line 2"),
                PropertyValidationError(Property("zip2").nel(), "invalid zip 2"),
            ).invalid()
            every { validateOrderLine(unvalidatedOrderLine1) } returns nonEmptyListOf(
                PropertyValidationError(Property("product1").nel(), "invalid product 1"),
                PropertyValidationError(Property("quantity1").nel(), "invalid quantity 1"),
            ).invalid()
            every { validateOrderLine(unvalidatedOrderLine2) } returns nonEmptyListOf(
                PropertyValidationError(Property("id2").nel(), "invalid id 2"),
            ).invalid()

            val expectedErrors = listOf(
                PropertyValidationError(
                    nonEmptyListOf(Property("customerInfo"), Property("customerProperty")),
                    "invalid"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("shippingAddress"), Property("line1")),
                    "invalid line 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("shippingAddress"), Property("city1")),
                    "invalid city 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("billingAddress"), Property("line2")),
                    "invalid line 2"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("billingAddress"), Property("zip2")),
                    "invalid zip 2"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("lines[0]"), Property("quantity1")),
                    "invalid quantity 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("lines[0]"), Property("product1")),
                    "invalid product 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("lines[1]"), Property("id2")),
                    "invalid id 2"
                ),
            )
            val reportedErrors = validate(unvalidatedOrder).shouldBeInvalid().all
            reportedErrors shouldHaveSize expectedErrors.size
            reportedErrors shouldContainAll expectedErrors
        }
        it("validates order with real dependencies") {
            validateOrder(
                ::validateCustomerInfo,
                ::validateAddress,
                ::validateOrderLine,
                unvalidatedOrder
            ).shouldBeValid() shouldBe validatedOrder
        }
    }
})
