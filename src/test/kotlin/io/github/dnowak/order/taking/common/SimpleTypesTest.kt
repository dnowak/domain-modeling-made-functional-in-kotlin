package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

internal class SimpleTypesTest : DescribeSpec({

    describe("KilogramQuantity.validate") {
        listOf("0.05", "0.10", "0.5", "10.50", "99.99", "100.00").forEach { value ->
            it("accepts <$value>") {
                val kg = BigDecimal(value)
                KilogramQuantity.validate(kg).shouldBeRight().value shouldBe kg
            }
        }
        listOf("0.00", "0.01", "0.04", "100.01", "101.00", "1000.00").forEach { value ->
            it("rejects <$value>") {
                val kg = BigDecimal(value)
                KilogramQuantity.validate(kg)
                    .shouldBeLeft().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("Price.validate") {
        listOf("0.00", "0.01", "0.05", "0.10", "0.5", "10.50", "999.99", "1000.00").forEach { value ->
            it("accepts <$value>") {
                val price = BigDecimal(value)
                Price.validate(price).shouldBeRight().value shouldBe price
            }
        }
        listOf("-1000.00", "-0.01", "1000.01", "10000.00").forEach { value ->
            it("rejects <$value>") {
                val price = BigDecimal(value)
                Price.validate(price)
                    .shouldBeLeft().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("BillingAmount.validate") {
        listOf("0.00", "0.01", "0.05", "0.10", "0.5", "10.50", "9999.99", "10000.00").forEach { value ->
            it("accepts <$value>") {
                val amount = BigDecimal(value)
                BillingAmount.validate(amount).shouldBeRight().value shouldBe amount
            }
        }
        listOf("-1000.00", "-0.01", "10000.01", "100000.00").forEach { value ->
            it("rejects <$value>") {
                val amount = BigDecimal(value)
                BillingAmount.validate(amount)
                    .shouldBeLeft().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
})
