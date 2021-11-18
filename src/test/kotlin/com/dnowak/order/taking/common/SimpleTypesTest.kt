package com.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeInvalid
import io.kotest.assertions.arrow.core.shouldBeValid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

internal class SimpleTypesTest : DescribeSpec({
    describe("EmailAddress.validate") {
        listOf("me@company.com", "john@google.com").forEach { email ->
            it("accepts <$email>") {
                EmailAddress.validate(email).shouldBeValid().value shouldBe email
            }
        }
        listOf("", " ", "@", "me@", "@google.com").forEach { email ->
            it("rejects <$email>") {
                EmailAddress.validate(email)
                    .shouldBeInvalid().all.shouldExist { error -> error.message.contains(email) }
            }
        }
        it("checks equality") {
            EmailAddress.create("ja@me.edu.pl") shouldBe EmailAddress.create("ja@me.edu.pl")
        }
    }
    describe("ZipCode.validate") {
        listOf("12345", "11223", "77766").forEach { zip ->
            it("accepts <$zip>") {
                ZipCode.validate(zip).shouldBeValid().value shouldBe zip
            }
        }
        listOf("", " ", "1", "12", "123", "1234", "abcde", "ABCDE").forEach { zip ->
            it("rejects <$zip>") {
                ZipCode.validate(zip).shouldBeInvalid().all.shouldExist { error -> error.message.contains(zip) }
            }
        }
    }
    describe("OrderId.validate") {
        listOf("1", "A", "1234567890", "ABCDEFGHIJ").forEach { id ->
            it("accepts <$id>") {
                OrderId.validate(id).shouldBeValid().value shouldBe id
            }
        }
        listOf("", "12345678901", "ABCDEFGHIJK").forEach { id ->
            it("rejects <$id>") {
                OrderId.validate(id).shouldBeInvalid().all.shouldExist { error -> error.message.contains(id) }
            }
        }
    }
    describe("OrderLineId.validate") {
        listOf("1", "A", "1234567890", "ABCDEFGHIJ").forEach { id ->
            it("accepts <$id>") {
                OrderLineId.validate(id).shouldBeValid().value shouldBe id
            }
        }
        listOf("", "12345678901", "ABCDEFGHIJK").forEach { id ->
            it("rejects <$id>") {
                OrderLineId.validate(id).shouldBeInvalid().all.shouldExist { error -> error.message.contains(id) }
            }
        }
    }
    describe("WidgetCode.validate") {
        listOf("W1234", "W4444", "W2222").forEach { value ->
            it("accepts <$value>") {
                WidgetCode.validate(value).shouldBeValid().value shouldBe value
            }
        }
        listOf("", "W", "W1", "W123", "Z1234", "G1234").forEach { value ->
            it("rejects <$value>") {
                WidgetCode.validate(value).shouldBeInvalid().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("GizmoCode.validate") {
        listOf("G123", "G444", "G222").forEach { value ->
            it("accepts <$value>") {
                GizmoCode.validate(value).shouldBeValid().value shouldBe value
            }
        }
        listOf("", "G", "G1", "W123", "G1234", "G12345").forEach { value ->
            it("rejects <$value>") {
                GizmoCode.validate(value).shouldBeInvalid().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("UnitQuantity.validate") {
        listOf(1, 10, 500, 999, 1000).forEach { value ->
            it("accepts <$value>") {
                UnitQuantity.validate(value).shouldBeValid().value shouldBe value
            }
        }
        listOf(-10000, -100, -1, 0, 1001, 10000).forEach { value ->
            it("rejects <$value>") {
                UnitQuantity.validate(value)
                    .shouldBeInvalid().all.shouldExist { error -> error.message.contains(value.toString()) }
            }
        }
    }
    describe("KilogramQuantity.validate") {
        listOf("0.05", "0.10", "0.5", "10.50", "99.99", "100.00").forEach { value ->
            it("accepts <$value>") {
                val kg = BigDecimal(value)
                KilogramQuantity.validate(kg).shouldBeValid().value shouldBe kg
            }
        }
        listOf("0.00", "0.01", "0.04", "100.01", "101.00", "1000.00").forEach { value ->
            it("rejects <$value>") {
                val kg = BigDecimal(value)
                KilogramQuantity.validate(kg)
                    .shouldBeInvalid().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("Price.validate") {
        listOf("0.00", "0.01", "0.05", "0.10", "0.5", "10.50", "999.99", "1000.00").forEach { value ->
            it("accepts <$value>") {
                val price = BigDecimal(value)
                Price.validate(price).shouldBeValid().value shouldBe price
            }
        }
        listOf("-1000.00", "-0.01", "1000.01", "10000.00").forEach { value ->
            it("rejects <$value>") {
                val price = BigDecimal(value)
                Price.validate(price)
                    .shouldBeInvalid().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
    describe("BillingAmount.validate") {
        listOf("0.00", "0.01", "0.05", "0.10", "0.5", "10.50", "9999.99", "10000.00").forEach { value ->
            it("accepts <$value>") {
                val amount = BigDecimal(value)
                BillingAmount.validate(amount).shouldBeValid().value shouldBe amount
            }
        }
        listOf("-1000.00", "-0.01", "10000.01", "100000.00").forEach { value ->
            it("rejects <$value>") {
                val amount = BigDecimal(value)
                BillingAmount.validate(amount)
                    .shouldBeInvalid().all.shouldExist { error -> error.message.contains(value) }
            }
        }
    }
})
