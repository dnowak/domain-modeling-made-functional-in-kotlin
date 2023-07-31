package io.github.dnowak.order.taking.common

import java.math.BigDecimal

fun validZipCodeValue(value: String): Boolean = value.length == 5 && value.all { it.isDigit() }

fun invalidZipCodeValue(value: String): Boolean = !validZipCodeValue(value)

fun validEmailAddressValue(value: String): Boolean = value.contains("@") && value.contains(".")

fun invalidEmailAddressValue(value: String): Boolean = !validEmailAddressValue(value)

fun validGizmoCodeValue(value: String): Boolean = value.length == 4 && value.startsWith("G") && value.drop(1).all { it.isDigit() }

fun invalidGizmoCodeValue(value: String): Boolean = !validGizmoCodeValue(value)

fun validWidgetCodeValue(value: String): Boolean = value.length == 5 && value.startsWith("W") && value.drop(1).all { it.isDigit() }

fun invalidWidgetCodeValue(value: String): Boolean = !validWidgetCodeValue(value)

fun validUnitQuantityValue(value: Int): Boolean = value in 1..1000

fun invalidUnitQuantityValue(value: Int): Boolean = !validUnitQuantityValue(value)

fun validKilogramQuantityValue(value: BigDecimal): Boolean = value in BigDecimal("0.05")..BigDecimal("100.00")

fun invalidKilogramQuantityValue(value: BigDecimal): Boolean = !validKilogramQuantityValue(value)

fun validBillingAmountValue(value: BigDecimal): Boolean = value in BigDecimal("0.00")..BigDecimal("10000.00")

fun invalidBillingAmountValue(value: BigDecimal): Boolean = !validBillingAmountValue(value)

fun validOrderIdValue(value: String) = value.length in 1..10 && value.all { it.isDigit() || it.isUpperCase()}

fun invalidOrderIdValue(value: String) = !validOrderIdValue(value)

fun validOrderLineIdValue(value: String) = value.length in 1..10 && value.all { it.isDigit() || it.isUpperCase()}

fun invalidOrderLineIdValue(value: String) = !validOrderLineIdValue(value)