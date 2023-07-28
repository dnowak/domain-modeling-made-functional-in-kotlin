package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class EmailAddressTest: FreeSpec({
    "EmailAddress" - {
        val validEmailAddresses = listOf("me@company.com", "john@google.com")
        val invalidEmailAddresses = listOf("", " ", "@", "me@", "@google.com")
        "validate" - {
            validEmailAddresses.forEach { email ->
                "accepts <$email>" {
                    EmailAddress.validate(email).shouldBeRight().value shouldBe email
                }
            }
            invalidEmailAddresses.forEach { email ->
                "rejects <$email>" {
                    EmailAddress.validate(email)
                        .shouldBeLeft().all.shouldExist { error -> error.message.contains(email) }
                }
            }

        }
        "create" - {
            validEmailAddresses.forEach { email ->
                "accepts <$email>" {
                    EmailAddress.create(email).value shouldBe email
                }
            }
            invalidEmailAddresses.forEach { email ->
                "rejects <$email>" {
                    val exception = shouldThrow<ValidationException> {
                        EmailAddress.create(email)
                    }
                    exception.message shouldContain "<$email>"
                }
            }
        }
        "equality" - {
            validEmailAddresses.forEach { email ->
                "checks <$email>" {
                    EmailAddress.create(email) shouldBe EmailAddress.create(email)
                }
            }
        }
    }
})