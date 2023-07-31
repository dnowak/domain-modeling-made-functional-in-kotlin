package io.github.dnowak.order.taking.common

import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.faker
import io.github.serpro69.kfaker.fakerConfig
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.ArbitraryBuilder
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import java.math.BigDecimal

fun Arb.Companion.gizmoCodeValue(): Arb<String> = Arb.int(0..999).map { "G" + it.toString().padStart(3, '0') }

fun Arb.Companion.gizmoCode(): Arb<GizmoCode> = gizmoCodeValue().map { GizmoCode.create(it) }

fun Arb.Companion.widgetCodeValue(): Arb<String> = Arb.int(0..9999).map { "W" + it.toString().padStart(4, '0') }

fun Arb.Companion.widgetCode(): Arb<WidgetCode> = widgetCodeValue().map { WidgetCode.create(it) }

fun Arb.Companion.unitQuantityValue(): Arb<Int> = Arb.int(1..1000)

fun Arb.Companion.kilogramQuantityValue(): Arb<BigDecimal> = Arb.bigDecimal(BigDecimal("0.05"), BigDecimal("100.00"))

fun Arb.Companion.priceValue(): Arb<BigDecimal> = Arb.bigDecimal(BigDecimal("0.00"), BigDecimal("100.00"))

fun validPriceValue(value: BigDecimal): Boolean = value in BigDecimal("0.00")..BigDecimal("100.00")

fun invalidPriceValue(value: BigDecimal): Boolean = !validPriceValue(value)

fun Arb.Companion.billingAmountValue(): Arb<BigDecimal> = Arb.bigDecimal(BigDecimal("0.00"), BigDecimal("10000.00"))

class JavaRandomAdapter(private val rs: RandomSource) : java.util.Random() {
    override fun next(bits: Int): Int = rs.random.nextInt(bits)
}

fun Arb.Companion.faker(): Arb<Faker> =
    ArbitraryBuilder.create { rs -> Faker(fakerConfig { random = JavaRandomAdapter(rs) }) }.build()

fun Arb.Companion.fakerEmailAddress(): Arb<String> = Arb.faker().map { it.internet.email() }