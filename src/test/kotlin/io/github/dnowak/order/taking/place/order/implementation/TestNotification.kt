package io.github.dnowak.order.taking.place.order.implementation

import arrow.core.partially1
import io.github.dnowak.order.taking.common.BillingAmount
import io.github.dnowak.order.taking.place.order.BillableOrderPlaced
import io.github.dnowak.order.taking.place.order.OrderAcknowledgmentSent
import io.github.dnowak.order.taking.place.order.PlaceOrderEvent
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal

internal class TestNotification : DescribeSpec({
    beforeTest { clearAllMocks() }
    describe("acknowledgeOrder") {
        val createOrderAcknowledgementLetter: CreateOrderAcknowledgementLetter = mockk()
        val sendAcknowledgement: SendOrderAcknowledgment = mockk()
        val acknowledgeOrder: AcknowledgeOrder =
            ::acknowledgeOrder
                .partially1(createOrderAcknowledgementLetter)
                .partially1(sendAcknowledgement)

        val letter = HtmlString("ack letter")
        val fixture = OrderFixture
        val order = fixture.pricedOrder
        val email = order.customerInfo.emailAddress
        beforeTest {
            every { createOrderAcknowledgementLetter(any()) } returns letter
        }
        context("acknowledgement sent") {
            var result: OrderAcknowledgmentSent? = null
            beforeTest {
                every { sendAcknowledgement(any()) } returns SendResult.Sent

                result = acknowledgeOrder(fixture.pricedOrder)
            }
            it("creates letter") {
                verify { createOrderAcknowledgementLetter(order) }
            }
            it("sends acknowledgement") {
                verify { sendAcknowledgement(OrderAcknowledgement(email, letter)) }
            }
            it("returns acknowledgement") {
                result shouldBe OrderAcknowledgmentSent(order.orderId, email)
            }
        }
        context("acknowledgement not sent") {
            var result: OrderAcknowledgmentSent? = null
            beforeTest {
                every { sendAcknowledgement(any()) } returns SendResult.NotSent

                result = acknowledgeOrder(fixture.pricedOrder)
            }
            it("creates letter") {
                verify { createOrderAcknowledgementLetter(order) }
            }
            it("sends acknowledgement") {
                verify { sendAcknowledgement(OrderAcknowledgement(email, letter)) }
            }
            it("returns null") {
                result shouldBe null
            }
        }
    }
    describe("createEvents") {
        val fixture = OrderFixture
        val order = fixture.pricedOrder
        val orderWithZeroBillingAmount = order.copy(amountToBill = BillingAmount.Companion.create(BigDecimal.ZERO))
        val orderAcknowledgmentSent = OrderAcknowledgmentSent(order.orderId, order.customerInfo.emailAddress)
        val sentEvent = PlaceOrderEvent.AcknowledgmentSent(orderAcknowledgmentSent)
        val placedEventZeroAmount = PlaceOrderEvent.OrderPlaced(orderWithZeroBillingAmount)
        val placedEventSomeAmount = PlaceOrderEvent.OrderPlaced(order)
        val billingEvent = PlaceOrderEvent.BillableOrderPlaced(BillableOrderPlaced(
            order.orderId,
            order.billingAddress,
            order.amountToBill)
        )
        context("order with zero billing amount, acknowledgement not sent") {
            it("creates events: placed") {
                createEvents(orderWithZeroBillingAmount, null) shouldContainExactlyInAnyOrder listOf(
                    placedEventZeroAmount
                )
            }
        }
        context("order with zero billing amount, acknowledgement sent") {
            it("creates events: placed, sent") {
                createEvents(orderWithZeroBillingAmount, orderAcknowledgmentSent) shouldContainExactlyInAnyOrder listOf(
                    placedEventZeroAmount,
                    sentEvent
                )
            }
        }
        context("order with billing amount greater than zero, acknowledgement not sent") {
            it("creates events: placed, billing") {
                createEvents(order, null) shouldContainExactlyInAnyOrder listOf(
                    placedEventSomeAmount,
                    billingEvent
                )
            }
        }
        context("order with billing amount greater than zero, acknowledgement sent") {
            it("creates events: sent, placed, billing") {
                createEvents(order, orderAcknowledgmentSent) shouldContainExactlyInAnyOrder listOf(
                    sentEvent,
                    placedEventSomeAmount,
                    billingEvent
                )
            }
        }
    }

})
