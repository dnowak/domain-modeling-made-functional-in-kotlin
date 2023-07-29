package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.assume
import io.kotest.property.checkAll

class KilogramQuantityTest: FreeSpec( {
    "KilogramQuantity" - {
        "validate" - {
            "accept" {
                Arb.kilogramQuantityValue().checkAll {
                    KilogramQuantity.validate(it).shouldBeRight().value shouldBe it
                }
            }
            "reject" {
                Arb.bigDecimal().checkAll {
                    assume(invalidKilogramQuantityValue(it))
                    KilogramQuantity.validate(it).shouldBeLeft().all.shouldExist { error -> error.message.contains("<$it>") }
                }
            }
        }
        "create" - {
            "accept" {
                Arb.kilogramQuantityValue().checkAll {
                    KilogramQuantity.create(it).value shouldBe it
                }
            }
            "reject" {
                Arb.bigDecimal().checkAll {
                    assume(invalidKilogramQuantityValue(it))
                    val exception = shouldThrow<ValidationException> {
                       KilogramQuantity.create(it)
                    }
                    exception.message shouldContain "<$it>"
                }
            }
        }
    }
})