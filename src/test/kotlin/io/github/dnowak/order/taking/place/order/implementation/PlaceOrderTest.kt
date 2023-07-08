package io.github.dnowak.order.taking.place.order.implementation

import arrow.core.*
import io.github.dnowak.order.taking.common.Property
import io.github.dnowak.order.taking.common.PropertyValidationError
import io.github.dnowak.order.taking.place.order.*
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

internal class PlaceOrderTest : DescribeSpec({
    beforeTest { clearAllMocks() }
    describe("placeOrder") {
        val validateOrder: ValidateOrder = mockk()
        val priceOrder: PriceOrder = mockk()
        val acknowledgeOrder: AcknowledgeOrder = mockk()
        val createEvents: CreateEvents = mockk()

        val placeOrder: PlaceOrder = ::placeOrder
            .partially1(validateOrder)
            .partially1(priceOrder)
            .partially1(acknowledgeOrder)
            .partially1(createEvents)

        val validatedOrder: ValidatedOrder = mockk()
        val pricedOrder: PricedOrder = mockk()
        val orderAcknowledgmentSent: OrderAcknowledgmentSent = mockk()
        val events: List<PlaceOrderEvent> = mockk()
        val unvalidatedOrder: UnvalidatedOrder = mockk()

        beforeTest {
            coEvery { validateOrder(any()) } returns validatedOrder.right()
            coEvery { priceOrder(any()) } returns pricedOrder.right()
            every { acknowledgeOrder(any()) } returns orderAcknowledgmentSent
            every { createEvents(any(), any()) } returns events
        }

        context("successful order placement") {
            lateinit var placeResult: Either<PlaceOrderError, List<PlaceOrderEvent>>

            beforeTest {
                placeResult = placeOrder(unvalidatedOrder)
            }
            it("returns events") {
                placeResult.shouldBeRight() shouldBe events
            }
            it("validates order") {
                coVerify { validateOrder(unvalidatedOrder) }
            }
            it("prices order") {
                coVerify { priceOrder(validatedOrder) }
            }
            it("acknowledges order") {
                verify { acknowledgeOrder(pricedOrder) }
            }
            it("creates events") {
                verify { createEvents(pricedOrder, orderAcknowledgmentSent) }
            }
        }
        context("validation error") {
            lateinit var placeResult: Either<PlaceOrderError, List<PlaceOrderEvent>>

            val validationErrors = PropertyValidationError(Property("name").nel(), "Left name").nel()

            beforeTest {
                coEvery { validateOrder(any()) } returns validationErrors.left()

                placeResult = placeOrder(unvalidatedOrder)
            }
            it("returns error") {
                placeResult.shouldBeLeft() shouldBe PlaceOrderError.Validation(validationErrors)
            }
        }
        context("pricing error") {
            lateinit var placeResult: Either<PlaceOrderError, List<PlaceOrderEvent>>

            val pricingError = PricingError("Left price")

            beforeTest {
                coEvery { priceOrder(any()) } returns pricingError.left()

                placeResult = placeOrder(unvalidatedOrder)
            }
            it("returns error") {
                placeResult.shouldBeLeft() shouldBe PlaceOrderError.Pricing(pricingError)
            }
        }
    }
})
