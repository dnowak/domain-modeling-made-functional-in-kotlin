package io.github.dnowak.order.taking.common

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import java.math.BigDecimal

fun Arb.Companion.gizmoCodeValue(): Arb<String> = Arb.int(0..999).map { "G" + it.toString().padStart(3, '0') }

fun Arb.Companion.widgetCodeValue(): Arb<String> = Arb.int(0..9999).map { "W" + it.toString().padStart(4, '0') }

fun Arb.Companion.unitQuantityValue(): Arb<Int> = Arb.int(1..1000)

fun validUnitQuantityValue(value: Int): Boolean = value in 1..1000

fun invalidUnitQuantityValue(value: Int): Boolean = !validUnitQuantityValue(value)

fun Arb.Companion.kilogramQuantityValue(): Arb<BigDecimal> = Arb.bigDecimal(BigDecimal("0.05"), BigDecimal("100.00"))

fun validKilogramQuantityValue(value: BigDecimal): Boolean = value in BigDecimal("0.05")..BigDecimal("100.00")

fun invalidKilogramQuantityValue(value: BigDecimal): Boolean = !validKilogramQuantityValue(value)


