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

class GizmoCodeTest : FreeSpec({
    "GizmoCode" - {
        "validate" - {
            "accepts" {
                Arb.gizmoCodeValue().checkAll { code ->
                    GizmoCode.validate(code).shouldBeRight().value shouldBe code
                }
            }
            "rejects" {
                Arb.string().checkAll { code ->
                    assume(invalidGizmoCodeValue(code))
                    GizmoCode.validate(code)
                        .shouldBeLeft().all.shouldExist { error -> error.message.contains("<$code>") }
                }
            }
        }
        "create" - {
            "accepts" {
                Arb.gizmoCodeValue().checkAll { code ->
                    GizmoCode.create(code).value shouldBe code
                }
            }
            "rejects" {
                Arb.string().checkAll { code ->
                    assume(invalidGizmoCodeValue(code))
                    val exception = shouldThrow<ValidationException> {
                        GizmoCode.create(code)
                    }
                    exception.message shouldContain "<$code>"
                }
            }
        }
        //TODO: is it needed?
        "equality" - {
            Arb.gizmoCodeValue().checkAll { code ->
                GizmoCode.create(code) shouldBe GizmoCode.create(code)
            }
        }
    }
})