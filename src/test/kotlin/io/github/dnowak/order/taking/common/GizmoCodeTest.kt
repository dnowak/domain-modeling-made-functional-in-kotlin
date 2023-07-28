package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class GizmoCodeTest : FreeSpec({
    "GizmoCode" - {
        val validCodes = listOf("G123", "G444", "G222")
        val invalidCodes = listOf("", "G", "G1", "W123", "G1234", "G12345")
        "validate" - {
            validCodes.forEach { code ->
                "accepts <$code>" {
                    GizmoCode.validate(code).shouldBeRight().value shouldBe code
                }
            }
            invalidCodes.forEach { code ->
                "rejects <$code>" {
                    GizmoCode.validate(code).shouldBeLeft().all.shouldExist { error -> error.message.contains(code) }
                }
            }
        }
        "create" - {
            validCodes.forEach { code ->
                "accepts <$code>" {
                    GizmoCode.create(code).value shouldBe code
                }
            }
            invalidCodes.forEach { code ->
                "rejects <$code>" {
                    val exception = shouldThrow<ValidationException> {
                        GizmoCode.create(code)
                    }
                    exception.message shouldContain "<$code>"
                }
            }
        }
        "equality" - {
            validCodes.forEach { code ->
                "checks <$code>" {
                    GizmoCode.create(code) shouldBe GizmoCode.create(code)
                }
            }
        }
    }
})