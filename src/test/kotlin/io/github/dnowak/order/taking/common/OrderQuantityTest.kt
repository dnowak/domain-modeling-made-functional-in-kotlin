package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.int
import io.kotest.property.assume
import io.kotest.property.checkAll

class OrderQuantityTest : FreeSpec({
    "OrderQuantity" - {
        "validate" - {
            "unit" - {
                "accept" {
                    checkAll(Arb.widgetCode(), Arb.unitQuantityValue()) { widgetCode, quantityValue ->
                        val expectedQuantity = OrderQuantity.Unit(UnitQuantity.create(quantityValue))
                        OrderQuantity.validate(
                            ProductCode.Widget(widgetCode),
                            quantityValue.toBigDecimal()
                        ) shouldBeRight expectedQuantity
                    }
                }
                "reject" {
                    checkAll(Arb.widgetCode(), Arb.int()) { widgetCode, quantityValue ->
                        assume(invalidUnitQuantityValue(quantityValue))
                        OrderQuantity.validate(
                            ProductCode.Widget(widgetCode),
                            quantityValue.toBigDecimal()
                        ).shouldBeLeft().all.shouldExist { error -> error.message.contains("<$quantityValue>") }
                    }
                }
            }
            "kilogram" - {
                "accept" {
                    checkAll(Arb.gizmoCode(), Arb.kilogramQuantityValue()) { gizmoCode, quantityValue ->
                        val expectedQuantity = OrderQuantity.Kilogram(KilogramQuantity.create(quantityValue))
                        OrderQuantity.validate(
                            ProductCode.Gizmo(gizmoCode),
                            quantityValue
                        ) shouldBeRight expectedQuantity
                    }
                }
                "reject" {
                    checkAll(Arb.gizmoCode(), Arb.bigDecimal()) { gizmoCode, quantityValue ->
                        assume(invalidKilogramQuantityValue(quantityValue))
                        OrderQuantity.validate(
                            ProductCode.Gizmo(gizmoCode),
                            quantityValue
                        ).shouldBeLeft().all.shouldExist { error -> error.message.contains("<$quantityValue>") }
                    }
                }
            }
        }
    }
})