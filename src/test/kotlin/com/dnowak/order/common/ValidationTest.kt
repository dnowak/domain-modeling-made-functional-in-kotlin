package com.dnowak.order.common

import io.kotest.assertions.arrow.core.shouldBeInvalid
import io.kotest.assertions.arrow.core.shouldBeValid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe

internal class ValidationTest: DescribeSpec({
    describe("validateEmail") {
        listOf("me@company.com", "john@google.com").forEach { email ->
            it("accepts <$email>") {
                validateEmail(email).shouldBeValid().value shouldBe email
            }
        }
        listOf("", " ", "@", "me@", "@google.com").forEach { email ->
            it("rejects <$email>") {
                validateEmail(email).shouldBeInvalid().all.shouldExist { error -> error.message.contains(email) }
            }
        }
    }
    describe("validateZipCode") {
        listOf("12345", "11223", "77766").forEach { zip ->
            it("accepts <$zip>") {
                validateZipCode(zip).shouldBeValid().value shouldBe zip
            }
        }
        listOf("", " ", "1", "12", "123", "1234", "abcde", "ABCDE").forEach { zip ->
            it("rejects <$zip>") {
                validateEmail(zip).shouldBeInvalid().all.shouldExist { error -> error.message.contains(zip) }
            }
        }
    }
})