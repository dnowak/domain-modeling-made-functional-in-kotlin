package com.dnowak.order.taking.place.order.implementation

import arrow.core.curried
import arrow.core.invalid
import arrow.core.invalidNel
import arrow.core.nel
import arrow.core.nonEmptyListOf
import arrow.core.validNel
import com.dnowak.order.taking.common.Property
import com.dnowak.order.taking.common.PropertyValidationError
import io.kotest.assertions.arrow.core.shouldBeInvalid
import io.kotest.assertions.arrow.core.shouldBeValid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

internal class ValidateOrderTest : DescribeSpec({
    beforeTest { clearAllMocks() }
    val fixture = OrderFixture
    describe("validateOrder") {
        val validateCustomerInfo: ValidateCustomerInfo = mockk()
        val validateAddress: ValidateAddress = mockk()
        val validateOrderLine: ValidateOrderLine = mockk()
        val validate: ValidateOrder =
            ::validateOrder.curried()(validateCustomerInfo)(validateAddress)(validateOrderLine)

        beforeTest {
            every { validateCustomerInfo(fixture.unvalidatedCustomerInfo) } returns fixture.validatedCustomerInfo.validNel()
            every { validateAddress(fixture.unvalidatedShippingAddress) } returns fixture.validatedShippingAddress.validNel()
            every { validateAddress(fixture.unvalidatedBillingAddress) } returns fixture.validatedBillingAddress.validNel()
            every { validateOrderLine(fixture.unvalidatedOrderLine1) } returns fixture.validatedOrderLine1.validNel()
            every { validateOrderLine(fixture.unvalidatedOrderLine2) } returns fixture.validatedOrderLine2.validNel()
        }
        it("validates correct order") {
            validate(fixture.unvalidatedOrder).shouldBeValid() shouldBe fixture.validatedOrder
        }
        it("validates orderId") {
            validate(fixture.unvalidatedOrder.copy(orderId = "")).shouldBeInvalid().all shouldExist { error ->
                error.path == nonEmptyListOf(Property("orderId"))
            }
        }
        it("validates properties") {
            every { validateCustomerInfo(any()) } returns PropertyValidationError(
                Property("customerProperty").nel(),
                "invalid"
            ).invalidNel()
            every { validateAddress(fixture.unvalidatedShippingAddress) } returns nonEmptyListOf(
                PropertyValidationError(Property("line1").nel(), "invalid line 1"),
                PropertyValidationError(Property("city1").nel(), "invalid city 1"),
            ).invalid()
            every { validateAddress(fixture.unvalidatedBillingAddress) } returns nonEmptyListOf(
                PropertyValidationError(Property("line2").nel(), "invalid line 2"),
                PropertyValidationError(Property("zip2").nel(), "invalid zip 2"),
            ).invalid()
            every { validateOrderLine(fixture.unvalidatedOrderLine1) } returns nonEmptyListOf(
                PropertyValidationError(Property("product1").nel(), "invalid product 1"),
                PropertyValidationError(Property("quantity1").nel(), "invalid quantity 1"),
            ).invalid()
            every { validateOrderLine(fixture.unvalidatedOrderLine2) } returns nonEmptyListOf(
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
            val reportedErrors = validate(fixture.unvalidatedOrder).shouldBeInvalid().all
            reportedErrors shouldHaveSize expectedErrors.size
            reportedErrors shouldContainAll expectedErrors
        }
        it("validates order with real dependencies") {
            validateOrder(
                ::validateCustomerInfo,
                ::validateAddress,
                ::validateOrderLine,
                fixture.unvalidatedOrder
            ).shouldBeValid() shouldBe fixture.validatedOrder
        }
    }
})
