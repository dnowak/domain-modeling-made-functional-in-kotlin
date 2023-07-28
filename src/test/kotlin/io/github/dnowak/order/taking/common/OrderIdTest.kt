package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class OrderIdTest: FreeSpec({
    "OrderId" - {
        val validIds = listOf("1", "A", "1234567890", "ABCDEFGHIJ")
        val invalidIds = listOf("", "12345678901", "ABCDEFGHIJK")
        "validate" - {
            validIds.forEach { id ->
                "accepts <$id>" {
                    OrderId.validate(id).shouldBeRight().value shouldBe id
                }
            }
            invalidIds.forEach { id ->
                "rejects <$id>" {
                    OrderId.validate(id).shouldBeLeft().all.shouldExist { error -> error.message.contains(id) }
                }
            }
        }
        "create" - {
            validIds.forEach { id ->
                "accepts <$id>" {
                    OrderId.create(id).value shouldBe id
                }
            }
            invalidIds.forEach { id ->
                "rejects <$id>" {
                    val exception = shouldThrow<ValidationException> {
                        OrderId.create(id)
                    }
                    exception.message shouldContain "<$id>"
                }
            }
        }
        "equality" - {
            validIds.forEach { id ->
                "checks <$id>" {
                    OrderId.create(id) shouldBe OrderId.create(id)
                }
            }
        }
    }
})