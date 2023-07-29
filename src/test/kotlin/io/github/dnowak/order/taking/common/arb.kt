package io.github.dnowak.order.taking.common

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map

fun Arb.Companion.gizmoCode(): Arb<String> = Arb.int(0 .. 999).map { "G" + it.toString().padStart(3, '0') }

fun Arb.Companion.widgetCode(): Arb<String> = Arb.int(0 .. 9999).map { "W" + it.toString().padStart(4, '0') }
