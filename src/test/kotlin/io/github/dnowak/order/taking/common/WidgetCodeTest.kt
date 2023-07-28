package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class WidgetCodeTest : FreeSpec({
    "WidgetCode" - {
        val validCodes = listOf("W1234", "W4444", "W2222")
        val invalidCodes = listOf("", "W", "W1", "W123", "Z1234", "G1234")
        "validate" - {
            validCodes.forEach { code ->
                "accepts <$code>" {
                    WidgetCode.validate(code).shouldBeRight().value shouldBe code
                }
            }
            invalidCodes.forEach { code ->
                "rejects <$code>" {
                    WidgetCode.validate(code).shouldBeLeft().all.shouldExist { error -> error.message.contains(code) }
                }
            }
        }
        "create" - {
            validCodes.forEach { code ->
                "accepts <$code>" {
                    WidgetCode.create(code).value shouldBe code
                }
            }
            invalidCodes.forEach { code ->
                "rejects <$code>" {
                    val exception = shouldThrow<ValidationException> {
                        WidgetCode.create(code)
                    }
                    exception.message shouldContain "<$code>"
                }
            }
        }
        "equality" - {
            validCodes.forEach { code ->
                "checks <$code>" {
                    WidgetCode.create(code) shouldBe WidgetCode.create(code)
                }
            }
        }
    }
})
