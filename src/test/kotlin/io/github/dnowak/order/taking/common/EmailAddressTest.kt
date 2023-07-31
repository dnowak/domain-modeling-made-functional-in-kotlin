package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.assume
import io.kotest.property.checkAll

class EmailAddressTest : FreeSpec({
    "EmailAddress" - {
        "validate" - {
            "accepts" {
                Arb.fakerEmailAddress().checkAll { email ->
                    EmailAddress.validate(email).shouldBeRight().value shouldBe email
                }
            }
            "rejects" {
                Arb.string().checkAll { email ->
                    assume(invalidEmailAddressValue(email))
                    EmailAddress.validate(email)
                        .shouldBeLeft().all.shouldExist { error -> error.message.contains("<$email>") }
                }
            }
        }
        "create" - {
            "accepts" {
                Arb.fakerEmailAddress().checkAll { email ->
                    EmailAddress.create(email).value shouldBe email
                }
            }
            "rejects" {
                Arb.string().checkAll { email ->
                    assume(invalidEmailAddressValue(email))
                    val exception = shouldThrow<ValidationException> {
                        EmailAddress.create(email)
                    }
                    exception.message shouldContain "<$email>"
                }
            }
        }
        //TODO: is it necessary?
        "equality" - {
            Arb.fakerEmailAddress().checkAll { email ->
                EmailAddress.create(email) shouldBe EmailAddress.create(email)
            }
        }
    }
})