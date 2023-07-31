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

class WidgetCodeTest : FreeSpec({
    "WidgetCode" - {
        "validate" - {
            "accept" {
                Arb.widgetCodeValue().checkAll { code ->
                    WidgetCode.validate(code).shouldBeRight().value shouldBe code
                }
            }
            "reject" {
                Arb.string().checkAll { code ->
                    assume(invalidWidgetCodeValue(code))
                    WidgetCode.validate(code)
                        .shouldBeLeft().all.shouldExist { error -> error.message.contains("<$code>") }
                }
            }
        }
        "create" - {
            "accept" {
                Arb.widgetCodeValue().checkAll { code ->
                    WidgetCode.create(code).value shouldBe code
                }
            }
            "reject" {
                Arb.string().checkAll { code ->
                    assume(invalidWidgetCodeValue(code))
                    val exception = shouldThrow<ValidationException> {
                        WidgetCode.create(code)
                    }
                    exception.message shouldContain "<$code>"
                }
            }
        }
        //TODO: is it needed?
        "equality" - {
            Arb.widgetCodeValue().checkAll { code ->
                WidgetCode.create(code) shouldBe WidgetCode.create(code)
            }
        }
    }
})
