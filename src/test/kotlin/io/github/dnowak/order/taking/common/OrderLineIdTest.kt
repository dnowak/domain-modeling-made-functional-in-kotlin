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

class OrderLineIdTest: FreeSpec({
    "OrderLineId" - {
        "validate" - {
            "accepts" {
                Arb.orderLineIdValues().checkAll { id ->
                    OrderLineId.validate(id).shouldBeRight().value shouldBe id
                }
            }
            "rejects" {
                Arb.string().checkAll { id ->
                    assume(invalidOrderLineIdValue(id))
                    OrderLineId.validate(id).shouldBeLeft().all.shouldExist { error -> error.message.contains("<$id>") }
                }
            }
        }
        "create" - {
            "accepts" {
                Arb.orderLineIdValues().checkAll { id ->
                    OrderLineId.create(id).value shouldBe id
                }
            }
            "rejects" {
                Arb.string().checkAll { id ->
                    assume(invalidOrderLineIdValue(id))
                    val exception = shouldThrow<ValidationException> {
                        OrderLineId.create(id)
                    }
                    exception.message shouldContain "<$id>"
                }
            }
        }
        //TODO: is it necessary?
        "equality" - {
            Arb.orderLineIdValues().checkAll {
                OrderLineId.create(it) shouldBe OrderLineId.create(it)
            }
        }
    }
})
