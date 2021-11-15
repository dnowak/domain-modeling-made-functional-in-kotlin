package com.dnowak.order.common

import arrow.core.*
import java.math.BigDecimal

/// Constrained to be 50 chars or less, not null
//type String50 = private String50 of string

class String50 internal constructor(val value: String)

/// An email address
//type EmailAddress = private EmailAddress of string
class EmailAddress internal constructor(val value: String)

/// A zip code
//type ZipCode = private ZipCode of string
class ZipCode internal constructor(val value: String)

/// An Id for Orders. Constrained to be a non-empty string < 10 chars
//type OrderId = private OrderId of string
class OrderId internal constructor(val value: String)

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
//type OrderLineId = private OrderLineId of string
class OrderLineId internal constructor(val value: String)


/// The codes for Widgets start with a "W" and then four digits
//type WidgetCode = private WidgetCode of string
class WidgetCode internal constructor(val value: String)

/// The codes for Gizmos start with a "G" and then three digits.
//type GizmoCode = private GizmoCode of string
class GizmoCode internal constructor(val value: String)

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
class UnitQuantity internal constructor(val value: Int)

/// Constrained to be a decimal between 0.05 and 100.00
//type KilogramQuantity = private KilogramQuantity of decimal
class KilogramQuantity internal constructor(val value: BigDecimal)

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
class Price internal constructor(val value: BigDecimal)

/// Constrained to be a decimal between 0.0 and 10000.00
//type BillingAmount = private BillingAmount of decimal
class BillingAmount internal constructor(val value: BigDecimal)

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

fun validateEmail(email: String): ValidatedNel<ValidationError, EmailAddress> =
    validateRegExp(".+@.+", email).bimap(::nonEmptyListOf, ::EmailAddress)

fun validateZipCode(zip: String): ValidatedNel<ValidationError, ZipCode> =
    validateRegExp("\\d{5}", zip).bimap(::nonEmptyListOf, ::ZipCode)