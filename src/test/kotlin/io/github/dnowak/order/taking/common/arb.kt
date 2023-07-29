package io.github.dnowak.order.taking.common

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map

fun Arb.Companion.gizmoCodeValue(): Arb<String> = Arb.int(0 .. 999).map { "G" + it.toString().padStart(3, '0') }

fun Arb.Companion.widgetCodeValue(): Arb<String> = Arb.int(0 .. 9999).map { "W" + it.toString().padStart(4, '0') }

fun Arb.Companion.unitQuantityValue(): Arb<Int> = Arb.int(1 .. 1000)

fun validUnitQuantityValue(value: Int): Boolean = value in 1 .. 1000
