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

class ProductCodeTest: FreeSpec({
    "ProductCode" - {
        "validate" - {
            "widget code" {
                Arb.widgetCodeValue().checkAll { code ->
                    ProductCode.validate(code) shouldBeRight ProductCode.Widget(WidgetCode.create(code))
                }
            }
            "gizmo code" {
                Arb.gizmoCodeValue().checkAll { code ->
                    ProductCode.validate(code) shouldBeRight ProductCode.Gizmo(GizmoCode.create(code))
                }
            }
            "invalid code" {
                Arb.string().checkAll { code ->
                    assume {
                        WidgetCode.validate(code).shouldBeLeft()
                        GizmoCode.validate(code).shouldBeLeft()
                    }
                    ProductCode.validate(code).shouldBeLeft().all shouldExist { error -> error.message.contains(code) }
                }
            }
        }
        "create" - {
            "widget code" {
                Arb.widgetCodeValue().checkAll { code ->
                    ProductCode.create(code) shouldBe ProductCode.Widget(WidgetCode.create(code))
                }
            }
            "gizmo code" {
                Arb.gizmoCodeValue().checkAll { code ->
                    ProductCode.create(code) shouldBe ProductCode.Gizmo(GizmoCode.create(code))
                }
            }
            "invalid code" {
                Arb.string().checkAll { code ->
                    assume {
                        WidgetCode.validate(code).shouldBeLeft()
                        GizmoCode.validate(code).shouldBeLeft()
                    }
                    val exception = shouldThrow<ValidationException> {
                        ProductCode.create(code)
                    }
                    exception.message shouldContain code
                }
            }
        }
    }
})