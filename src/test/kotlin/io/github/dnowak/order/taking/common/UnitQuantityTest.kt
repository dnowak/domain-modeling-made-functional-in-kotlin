package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.assume
import io.kotest.property.checkAll

class UnitQuantityTest: FreeSpec( {
    "UnitQuantity" - {
        "validate" - {
            "accept" {
                Arb.unitQuantityValue().checkAll {
                    UnitQuantity.validate(it).shouldBeRight().value shouldBe it
                }
            }
            "reject" {
                Arb.int().checkAll {
                    assume(!validUnitQuantityValue(it))
                    UnitQuantity.validate(it).shouldBeLeft().all.shouldExist { error -> error.message.contains("<$it>") }
                }
            }
        }
        "create" - {
            "accept" {
                Arb.unitQuantityValue().checkAll {
                    UnitQuantity.create(it).value shouldBe it
                }
            }
            "reject" {
                Arb.int().checkAll {
                    assume(!validUnitQuantityValue(it))
                    val exception = shouldThrow<ValidationException> {
                       UnitQuantity.create(it)
                    }
                    exception.message shouldContain "<$it>"
                }
            }
        }
    }
})