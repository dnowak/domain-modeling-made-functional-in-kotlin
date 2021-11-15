package com.dnowak.order.taking.common

import arrow.core.*
import java.math.BigDecimal

/// Constrained to be 50 chars or less, not null
//type String50 = private String50 of string

class String50 internal constructor(val value: String)

/// An email address
//type EmailAddress = private EmailAddress of string
class EmailAddress private constructor(val value: String) {
    companion object {
        fun validate(email: String): ValidatedNel<ValidationError, EmailAddress> =
            validateRegExp(".+@.+", email).bimap(::nonEmptyListOf, ::EmailAddress)
    }
}

/// A zip code
//type ZipCode = private ZipCode of string
class ZipCode private constructor(val value: String) {
    companion object {
        fun validate(zip: String): ValidatedNel<ValidationError, ZipCode> =
            validateRegExp("\\d{5}", zip).bimap(::nonEmptyListOf, ::ZipCode)
    }
}

/// An Id for Orders. Constrained to be a non-empty string < 10 chars
//type OrderId = private OrderId of string
class OrderId private constructor(val value: String) {
    companion object {
        fun validate(id: String): ValidatedNel<ValidationError, OrderId> =
            validateStringLength(1, 10, id).bimap(::nonEmptyListOf, ::OrderId)
    }
}

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
//type OrderLineId = private OrderLineId of string
class OrderLineId private constructor(val value: String) {
    companion object {
        fun validate(id: String): ValidatedNel<ValidationError, OrderLineId> =
            validateStringLength(1, 10, id).bimap(::nonEmptyListOf, ::OrderLineId)
    }
}

/// The codes for Widgets start with a "W" and then four digits
//type WidgetCode = private WidgetCode of string
class WidgetCode private constructor(val value: String) {
    companion object {
        fun validate(code: String): ValidatedNel<ValidationError, WidgetCode> =
            validateRegExp("W\\d{4}", code).bimap(::nonEmptyListOf, ::WidgetCode)
    }
}

/// The codes for Gizmos start with a "G" and then three digits.
//type GizmoCode = private GizmoCode of string
class GizmoCode private constructor(val value: String) {
    companion object {
        fun validate(code: String): ValidatedNel<ValidationError, GizmoCode> =
            validateRegExp("G\\d{3}", code).bimap(::nonEmptyListOf, ::GizmoCode)
    }
}

/// A ProductCode is either a Widget or a Gizmo
/*
type ProductCode =
| Widget of WidgetCode
| Gizmo of GizmoCode
 */
sealed class ProductCode {
    data class Widget(val value: WidgetCode) : ProductCode()
    data class Gizmo(val value: GizmoCode) : ProductCode()
}

/// Constrained to be a integer between 1 and 1000
//type UnitQuantity = private UnitQuantity of int
class UnitQuantity private constructor(val value: Int) {
    companion object {
        fun validate(quantity: Int): ValidatedNel<ValidationError, UnitQuantity> =
            validateIntInRange(1, 1000, quantity).bimap(::nonEmptyListOf, ::UnitQuantity)
    }
}

/// Constrained to be a decimal between 0.05 and 100.00
//type KilogramQuantity = private KilogramQuantity of decimal
class KilogramQuantity internal constructor(val value: BigDecimal) {
    companion object {
        private val min = BigDecimal("0.05")
        private val max = BigDecimal("100.00")

        fun validate(quantity: BigDecimal): ValidatedNel<ValidationError, KilogramQuantity> {
            return validateBigDecimalInRange(min, max, quantity)
                .bimap(::nonEmptyListOf, ::KilogramQuantity)
        }
    }
}

/// A Quantity is either a Unit or a Kilogram
/*
type OrderQuantity =
| Unit of UnitQuantity
| Kilogram of KilogramQuantity
 */
sealed class OrderQuantity {
    data class Unit(val value: UnitQuantity) : OrderQuantity()
    data class Kilogram(val value: KilogramQuantity) : OrderQuantity()
}

/// Constrained to be a decimal between 0.0 and 1000.00
//type Price = private Price of decimal
class Price private constructor(val value: BigDecimal) {
    companion object {
        private val min = BigDecimal("0.00")
        private val max = BigDecimal("1000.00")

        fun validate(quantity: BigDecimal): ValidatedNel<ValidationError, Price> {
            return validateBigDecimalInRange(min, max, quantity)
                .bimap(::nonEmptyListOf, ::Price)
        }
    }
}

/// Constrained to be a decimal between 0.0 and 10000.00
//type BillingAmount = private BillingAmount of decimal
class BillingAmount internal constructor(val value: BigDecimal) {
    companion object {
        private val min = BigDecimal("0.00")
        private val max = BigDecimal("10000.00")

        fun validate(quantity: BigDecimal): ValidatedNel<ValidationError, BillingAmount> {
            return validateBigDecimalInRange(min, max, quantity)
                .bimap(::nonEmptyListOf, ::BillingAmount)
        }
    }
}

/// Represents a PDF attachment
/*
type PdfAttachment = {
    Name : string
    Bytes: byte[]
}
 */
class PdfAttachment(val name: String, val bytes: Array<Byte>)

data class ValidationError(val message: String)

fun validateRegExp(pattern: String, value: String): Validated<ValidationError, String> =
    if (Regex(pattern).matches(value)) {
        Valid(value)
    } else {
        Invalid(ValidationError("'$value' must match the pattern '$pattern'"))
    }

fun validateStringLength(min: Int, max: Int, value: String): Validated<ValidationError, String> {
    val length = value.length
    return if ((length >= min) && (length <= max)) {
        Valid(value)
    } else {
        Invalid(ValidationError("The length of <$value> should be between <$min> and <$max>"))
    }
}

fun validateIntInRange(min: Int, max: Int, value: Int): Validated<ValidationError, Int> =
    if ((value >= min) && (value <= max)) {
        Valid(value)
    } else {
        Invalid(ValidationError("The <$value> should be between <$min> and <$max>"))
    }

fun validateBigDecimalInRange(
    min: BigDecimal,
    max: BigDecimal,
    value: BigDecimal
): Validated<ValidationError, BigDecimal> =
    if ((value >= min) && (value <= max)) {
        Valid(value)
    } else {
        Invalid(ValidationError("The <$value> should be between <$min> and <$max>"))
    }
