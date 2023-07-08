package io.github.dnowak.order.taking.place.order.implementation

import arrow.core.*
import io.github.dnowak.order.taking.common.*
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldExist
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.github.dnowak.order.taking.place.order.implementation.validateOrderLine as toOrderLine

internal class ValidateOrderTest : DescribeSpec({
    beforeTest { clearAllMocks() }
    val fixture = OrderFixture
    describe("validateOrder") {
        val validateCustomerInfo: ValidateCustomerInfo = mockk()
        val validateAddress: ValidateAddress = mockk()
        val validateOrderLine: ValidateOrderLine = mockk()
        val validate: ValidateOrder =
            ::validateOrder
                .partially1(validateCustomerInfo)
                .partially1(validateAddress)
                .partially1(validateOrderLine)

        beforeTest {
            every { validateCustomerInfo(fixture.unvalidatedCustomerInfo) } returns fixture.validatedCustomerInfo.right()
            every { validateAddress(fixture.unvalidatedShippingAddress) } returns fixture.validatedShippingAddress.right()
            every { validateAddress(fixture.unvalidatedBillingAddress) } returns fixture.validatedBillingAddress.right()
            every { validateOrderLine(fixture.unvalidatedOrderLine1) } returns fixture.validatedOrderLine1.right()
            every { validateOrderLine(fixture.unvalidatedOrderLine2) } returns fixture.validatedOrderLine2.right()
        }
        it("validates correct order") {
            validate(fixture.unvalidatedOrder) shouldBeRight fixture.validatedOrder
        }
        it("validates orderId") {
            validate(fixture.unvalidatedOrder.copy(orderId = "")).shouldBeLeft().all shouldExist { error ->
                error.path == nonEmptyListOf(Property("orderId"))
            }
        }
        it("validates properties") {
            every { validateCustomerInfo(any()) } returns PropertyValidationError(
                Property("customerProperty").nel(),
                "Left"
            ).left().toEitherNel()
            every { validateAddress(fixture.unvalidatedShippingAddress) } returns nonEmptyListOf(
                PropertyValidationError(Property("line1").nel(), "Left line 1"),
                PropertyValidationError(Property("city1").nel(), "Left city 1"),
            ).left()
            every { validateAddress(fixture.unvalidatedBillingAddress) } returns nonEmptyListOf(
                PropertyValidationError(Property("line2").nel(), "Left line 2"),
                PropertyValidationError(Property("zip2").nel(), "Left zip 2"),
            ).left()
            every { validateOrderLine(fixture.unvalidatedOrderLine1) } returns nonEmptyListOf(
                PropertyValidationError(Property("product1").nel(), "Left product 1"),
                PropertyValidationError(Property("quantity1").nel(), "Left quantity 1"),
            ).left()
            every { validateOrderLine(fixture.unvalidatedOrderLine2) } returns nonEmptyListOf(
                PropertyValidationError(Property("id2").nel(), "Left id 2"),
            ).left()

            val expectedErrors = listOf(
                PropertyValidationError(
                    nonEmptyListOf(Property("customerInfo"), Property("customerProperty")),
                    "Left"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("shippingAddress"), Property("line1")),
                    "Left line 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("shippingAddress"), Property("city1")),
                    "Left city 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("billingAddress"), Property("line2")),
                    "Left line 2"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("billingAddress"), Property("zip2")),
                    "Left zip 2"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("lines[0]"), Property("quantity1")),
                    "Left quantity 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("lines[0]"), Property("product1")),
                    "Left product 1"
                ),
                PropertyValidationError(
                    nonEmptyListOf(Property("lines[1]"), Property("id2")),
                    "Left id 2"
                ),
            )
            val reportedErrors = validate(fixture.unvalidatedOrder).shouldBeLeft().all
//            reportedErrors shouldHaveSize expectedErrors.size
            withClue(reportedErrors) {
                reportedErrors shouldContainOnly expectedErrors
            }
        }
        it("validates order with real dependencies") {
            val validateProductCode: ValidateProductCode = { code ->
                ProductCode.validate(code).flatMap(::checkProductCode.partially1 { _ -> true })
            }

            val validateLine: ValidateOrderLine =
                ::toOrderLine
                    .partially1(OrderLineId::validate)
                    .partially1(validateProductCode)
                    .partially1(OrderQuantity::validate)

            validateOrder(
                ::validateCustomerInfo,
                ::validateAddress,
                validateLine,
                fixture.unvalidatedOrder
            ) shouldBeRight fixture.validatedOrder
        }
    }
    describe("validateOrderLine") {
        val validateProductCode: ValidateProductCode = mockk()
        //TODO: mock the rest of validations
        val validate: ValidateOrderLine = ::toOrderLine
            .partially1(OrderLineId::validate)
            .partially1(validateProductCode)
            .partially1(OrderQuantity::validate)
        context("Right line") {
            lateinit var result: EitherNel<PropertyValidationError, ValidatedOrderLine>
            beforeTest {
                every { validateProductCode(fixture.unvalidatedOrderLine1.productCode) } returns fixture.validatedOrderLine1.productCode.right()

                result = validate(fixture.unvalidatedOrderLine1)
            }

            it("returns Either order line") {
                result shouldBeRight fixture.validatedOrderLine1
            }
        }
        context("Left product code") {
            lateinit var result: EitherNel<PropertyValidationError, ValidatedOrderLine>
            val error = ValidationError("Left code")
            beforeTest {
                every { validateProductCode(fixture.unvalidatedOrderLine1.productCode) } returns error.left()
                    .toEitherNel()

                result = validate(fixture.unvalidatedOrderLine1)
            }

            it("returns validation error") {
                result.shouldBeLeft().all shouldContainExactlyInAnyOrder listOf(
                    PropertyValidationError(
                        Property("productCode").nel(),
                        error.message
                    )
                )
            }
        }
    }
})
