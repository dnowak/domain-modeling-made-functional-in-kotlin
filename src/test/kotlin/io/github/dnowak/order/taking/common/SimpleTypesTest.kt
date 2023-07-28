package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

internal class SimpleTypesTest : DescribeSpec({

    describe("WidgetCode.validate") {
        listOf("W1234", "W4444", "W2222").forEach { value ->
            it("accepts <$value>") {
                WidgetCode.validate(value).shouldBeRight().value shouldBe value
            }
        }
        listOf("", "W", "W1", "W123", "Z1234", "G1234").forEach { value ->
            it("rejects <$value>") {
                WidgetCode.validate(value).shouldBeLeft().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("GizmoCode.validate") {
        listOf("G123", "G444", "G222").forEach { value ->
            it("accepts <$value>") {
                GizmoCode.validate(value).shouldBeRight().value shouldBe value
            }
        }
        listOf("", "G", "G1", "W123", "G1234", "G12345").forEach { value ->
            it("rejects <$value>") {
                GizmoCode.validate(value).shouldBeLeft().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("UnitQuantity.validate") {
        listOf(1, 10, 500, 999, 1000).forEach { value ->
            it("accepts <$value>") {
                UnitQuantity.validate(value).shouldBeRight().value shouldBe value
            }
        }
        listOf(-10000, -100, -1, 0, 1001, 10000).forEach { value ->
            it("rejects <$value>") {
                UnitQuantity.validate(value)
                    .shouldBeLeft().all.shouldExist { error -> error.message.contains(value.toString()) }
            }
        }
    }
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
