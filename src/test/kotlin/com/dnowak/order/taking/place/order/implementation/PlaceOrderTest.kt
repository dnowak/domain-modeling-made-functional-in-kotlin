package com.dnowak.order.taking.place.order.implementation

import arrow.core.Either
import arrow.core.curried
import arrow.core.invalid
import arrow.core.left
import arrow.core.nel
import arrow.core.right
import arrow.core.validNel
import com.dnowak.order.taking.common.Property
import com.dnowak.order.taking.common.PropertyValidationError
import com.dnowak.order.taking.place.order.OrderAcknowledmentSent
import com.dnowak.order.taking.place.order.PlaceOrder
import com.dnowak.order.taking.place.order.PlaceOrderError
import com.dnowak.order.taking.place.order.PlaceOrderEvent
import com.dnowak.order.taking.place.order.PricedOrder
import com.dnowak.order.taking.place.order.PricingError
import com.dnowak.order.taking.place.order.UnvalidatedOrder
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

internal class PlaceOrderTest : DescribeSpec({
    beforeTest { clearAllMocks() }
    describe("placeOrder") {
        val validateOrder: ValidateOrder = mockk()
        val priceOrder: PriceOrder = mockk()
        val acknowledgeOrder: AcknowledgeOrder = mockk()
        val createEvents: CreateEvents = mockk()

        val placeOrder: PlaceOrder = ::placeOrder.curried()(validateOrder)(priceOrder)(acknowledgeOrder)(createEvents)

        val validatedOrder: ValidatedOrder = mockk()
        val pricedOrder: PricedOrder = mockk()
        val orderAcknowledgmentSent: OrderAcknowledmentSent = mockk()
        val events: List<PlaceOrderEvent> = mockk()
        val unvalidatedOrder: UnvalidatedOrder = mockk()

        beforeTest {
            coEvery { validateOrder(any()) } returns validatedOrder.validNel()
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

            val validationErrors = PropertyValidationError(Property("name").nel(), "Invalid name").nel()

            beforeTest {
                coEvery { validateOrder(any()) } returns validationErrors.invalid()

                placeResult = placeOrder(unvalidatedOrder)
            }
            it("returns error") {
                placeResult.shouldBeLeft() shouldBe PlaceOrderError.Validation(validationErrors)
            }
        }
        context("pricing error") {
            lateinit var placeResult: Either<PlaceOrderError, List<PlaceOrderEvent>>

            val pricingError = PricingError("Invalid price")

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
