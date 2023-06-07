package io.github.dnowak.order.taking.place.order.implementation

import arrow.core.Either
import arrow.core.partially1
import arrow.core.Either.Right
import arrow.core.right
import io.github.dnowak.order.taking.common.Price
import io.github.dnowak.order.taking.place.order.PricedOrder
import io.github.dnowak.order.taking.place.order.PricingError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk

internal class PriceOrderTest : DescribeSpec({
    val fixture = OrderFixture
    describe("priceOrder") {
        val priceOrderLine: PriceOrderLine = mockk()
        val priceOrder: PriceOrder = ::priceOrder.partially1(priceOrderLine)
        context("order that can be priced") {
            lateinit var result: Either<PricingError, PricedOrder>
            beforeTest {
                every { priceOrderLine(fixture.validatedOrderLine1) } returns fixture.pricedOrderLine1.right()
                every { priceOrderLine(fixture.validatedOrderLine2) } returns fixture.pricedOrderLine2.right()

                result = priceOrder(fixture.validatedOrder)
            }
            it("returns priced order") {
                result.shouldBeRight() shouldBe fixture.pricedOrder
            }
        }
        context("billing amount out of range") {
            lateinit var result: Either<PricingError, PricedOrder>
            val validatedLine = fixture.validatedOrderLine1
            val pricedLine = fixture.pricedOrderLine1.copy(linePrice = Price.create("1000"))
            val lines = List(11) { validatedLine }
            beforeTest {
                every { priceOrderLine(validatedLine) } returns pricedLine.right()

                result = priceOrder(fixture.validatedOrder.copy(lines = lines))
            }
            it("returns priced order") {
                val error = result.shouldBeLeft()
                error.message shouldContain "amountToBill"
            }
        }
    }
    describe("priceOrderLine") {
        val getProductPrice: GetProductPrice = mockk()
        val priceOrderLine: PriceOrderLine = ::priceOrderLine.partially1(getProductPrice)
        context("line that can be priced") {
            beforeTest {
                every { getProductPrice(fixture.validatedOrderLine1.productCode)} returns Price.create("1.12")
            }
            it("returns priced line") {
                priceOrderLine(fixture.validatedOrderLine1).shouldBeRight() shouldBe fixture.pricedOrderLine1
            }
        }
        context("line price out of range") {
            beforeTest {
                every { getProductPrice(fixture.validatedOrderLine1.productCode)} returns Price.create("1000.00")
            }
            it("returns pricing error") {
                priceOrderLine(fixture.validatedOrderLine1).shouldBeLeft()
            }
        }
    }
})
