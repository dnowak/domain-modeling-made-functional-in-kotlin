package com.dnowak.order.taking.common

import arrow.core.Invalid
import arrow.core.Nel
import arrow.core.Valid
import arrow.core.Validated
import arrow.core.ValidatedNel
import arrow.core.getOrElse
import arrow.core.invalidNel
import arrow.core.nonEmptyListOf
import arrow.core.partially1
import arrow.core.valid
import com.dnowak.order.taking.common.ProductCode.Gizmo
import com.dnowak.order.taking.common.ProductCode.Widget
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal

/// Constrained to be 50 chars or less, not null
//type String50 = private String50 of string

class String50 internal constructor(val value: String)

/// An email address
//type EmailAddress = private EmailAddress of string
class EmailAddress private constructor(value: String): SimpleType<String>(value) {
    companion object {
        fun validate(email: String): ValidatedNel<ValidationError, EmailAddress> =
            validateRegExp(".+@.+", email).bimap(::nonEmptyListOf, ::EmailAddress)

        fun create(email: String): EmailAddress = validate(email).getOrElse { throw ValidationException(email) }
    }
}

/// A zip code
//type ZipCode = private ZipCode of string
class ZipCode private constructor(value: String): SimpleType<String>(value) {
    companion object {
        fun validate(zip: String): ValidatedNel<ValidationError, ZipCode> =
            validateRegExp("\\d{5}", zip).bimap(::nonEmptyListOf, ::ZipCode)

        fun create(zip: String): ZipCode = validate(zip).getOrElse { throw ValidationException(zip) }
    }
}

abstract class SimpleType<T : Any> protected constructor(val value: T) {
    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other?.javaClass == javaClass) {
            val otherType = other as SimpleType<*>
            return value == otherType.value
        }
        return false
    }

    override fun toString(): String {
        return (this::class.simpleName ?: "Unknown") + "(value=$value)"
    }
}

/// An Id for Orders. Constrained to be a non-empty string < 10 chars
//type OrderId = private OrderId of string
class OrderId private constructor(value: String) : SimpleType<String>(value) {
    companion object {
        fun validate(id: String): ValidatedNel<ValidationError, OrderId> =
            validateStringLength(1, 10, id).bimap(::nonEmptyListOf, ::OrderId)

        fun create(id: String): OrderId = validate(id).getOrElse { throw ValidationException(id) }
    }
}

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
//type OrderLineId = private OrderLineId of string
class OrderLineId private constructor(value: String): SimpleType<String>(value) {
    companion object {
        fun validate(id: String): ValidatedNel<ValidationError, OrderLineId> =
            validateStringLength(1, 10, id).bimap(::nonEmptyListOf, ::OrderLineId)

        fun create(id: String): OrderLineId = validate(id).getOrElse { throw ValidationException(id) }
    }
}

/// The codes for Widgets start with a "W" and then four digits
//type WidgetCode = private WidgetCode of string
class WidgetCode private constructor(value: String): SimpleType<String>(value) {
    companion object {
        fun validate(code: String): ValidatedNel<ValidationError, WidgetCode> =
            validateRegExp("W\\d{4}", code).bimap(::nonEmptyListOf, ::WidgetCode)

        fun create(code: String): WidgetCode = validate(code).getOrElse { throw ValidationException(code) }
    }
}

/// The codes for Gizmos start with a "G" and then three digits.
//type GizmoCode = private GizmoCode of string
class GizmoCode private constructor(value: String): SimpleType<String>(value) {
    companion object {
        fun validate(code: String): ValidatedNel<ValidationError, GizmoCode> =
            validateRegExp("G\\d{3}", code).bimap(::nonEmptyListOf, ::GizmoCode)

        fun create(code: String): GizmoCode = validate(code).getOrElse { throw ValidationException(code) }
    }
}

/// A ProductCode is either a Widget or a Gizmo
/*
type ProductCode =
| Widget of WidgetCode
| Gizmo of GizmoCode
 */
sealed class ProductCode {
    data class Widget(val code: WidgetCode) : ProductCode()
    data class Gizmo(val code: GizmoCode) : ProductCode()

    companion object {
        /*
        /// Return the string value inside a ProductCode
        let value productCode =
        match productCode with
        | Widget (WidgetCode wc) -> wc
        | Gizmo (GizmoCode gc) -> gc
         */

        fun value(productCode: ProductCode): String = when (productCode) {
            is Gizmo -> productCode.code.value
            is Widget -> productCode.code.value
        }
        /*
        /// Create an ProductCode from a string
        /// Return Error if input is null, empty, or not matching pattern
        let create fieldName code =
        if String.IsNullOrEmpty(code) then
        let msg = sprintf "%s: Must not be null or empty" fieldName
        Error msg
        else if code.StartsWith("W") then
        WidgetCode.create fieldName code
        |> Result.map Widget
        else if code.StartsWith("G") then
        GizmoCode.create fieldName code
        |> Result.map Gizmo
        else
        let msg = sprintf "%s: Format not recognized '%s'" fieldName code
        Error msg
         */

        fun validate(code: String): ValidatedNel<ValidationError, ProductCode> =
            if (StringUtils.startsWith(code, "W")) {
                WidgetCode.validate(code).map(ProductCode::Widget)
            } else if (StringUtils.startsWith(code, "G")) {
                GizmoCode.validate(code).map(ProductCode::Gizmo)
            } else {
                ValidationError("Invalid product code: <$code>").invalidNel()
            }
    }


}

/// Constrained to be a integer between 1 and 1000
//type UnitQuantity = private UnitQuantity of int
class UnitQuantity private constructor(value: Int): SimpleType<Int>(value) {
    companion object {
        fun validate(quantity: Int): ValidatedNel<ValidationError, UnitQuantity> =
            validateIntInRange(1, 1000, quantity).bimap(::nonEmptyListOf, ::UnitQuantity)

        fun create(quantity: Int): UnitQuantity =
            validate(quantity).getOrElse { throw ValidationException(quantity.toString()) }
    }
}

/// Constrained to be a decimal between 0.05 and 100.00
//type KilogramQuantity = private KilogramQuantity of decimal
class KilogramQuantity internal constructor(value: BigDecimal): SimpleType<BigDecimal>(value) {
    companion object {
        private val min = BigDecimal("0.05")
        private val max = BigDecimal("100.00")

        fun validate(quantity: BigDecimal): ValidatedNel<ValidationError, KilogramQuantity> {
            return validateBigDecimalInRange(min, max, quantity)
                .bimap(::nonEmptyListOf, ::KilogramQuantity)
        }

        fun create(quantity: BigDecimal): KilogramQuantity =
            validate(quantity).getOrElse { throw ValidationException(quantity.toString()) }

        fun create(quantity: String): KilogramQuantity =
            create((BigDecimal(quantity)))
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

    companion object {
        /*
        /// Return the value inside a OrderQuantity
        let value qty =
        match qty with
        | Unit uq ->
        uq |> UnitQuantity.value |> decimal
        | Kilogram kq ->
        kq |> KilogramQuantity.value
         */

        fun value(quantity: OrderQuantity): BigDecimal = when (quantity) {
            is Kilogram -> quantity.value.value
            is Unit -> quantity.value.value.toBigDecimal()
        }

        /*
        /// Create a OrderQuantity from a productCode and quantity
        let create fieldName productCode quantity  =
        match productCode with
        | Widget _ ->
        UnitQuantity.create fieldName (int quantity) // convert float to int
        |> Result.map OrderQuantity.Unit             // lift to OrderQuantity type
        | Gizmo _ ->
        KilogramQuantity.create fieldName quantity
        |> Result.map OrderQuantity.Kilogram         // lift to OrderQuantity type
         */

        fun validate(productCode: ProductCode, value: BigDecimal): ValidatedNel<ValidationError, OrderQuantity> =
            when (productCode) {
                is Gizmo -> KilogramQuantity.validate(value).map(OrderQuantity::Kilogram)
                is Widget -> UnitQuantity.validate(value.intValueExact()).map(OrderQuantity::Unit)
            }
    }

    val quantity: BigDecimal get() = when(this) {
        is Kilogram -> value.value
        is Unit -> value.value.toBigDecimal()
    }
}

/// Constrained to be a decimal between 0.0 and 1000.00
//type Price = private Price of decimal
class Price private constructor(value: BigDecimal): SimpleType<BigDecimal>(value) {
    companion object {
        private val min = BigDecimal("0.00")
        private val max = BigDecimal("1000.00")

        fun validate(amount: BigDecimal): ValidatedNel<ValidationError, Price> {
            return validateBigDecimalInRange(min, max, amount)
                .bimap(::nonEmptyListOf, ::Price)
        }

        fun create(amount: BigDecimal): Price =
            validate(amount).getOrElse { throw ValidationException(amount.toString()) }

        fun create(amount: String): Price =
            create(BigDecimal(amount))
    }
}

/// Constrained to be a decimal between 0.0 and 10000.00
//type BillingAmount = private BillingAmount of decimal
class BillingAmount private constructor(value: BigDecimal): SimpleType<BigDecimal>(value) {
    companion object {
        private val min = BigDecimal("0.00")
        private val max = BigDecimal("10000.00")

        fun validate(amount: BigDecimal): ValidatedNel<ValidationError, BillingAmount> {
            return validateBigDecimalInRange(min, max, amount)
                .bimap(::nonEmptyListOf, ::BillingAmount)
        }

        fun create(amount: BigDecimal): BillingAmount =
            validate(amount).getOrElse { throw ValidationException(amount.toString()) }

        fun create(amount: String): BillingAmount =
            create(BigDecimal(amount))
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

class ValidationException(message: String) : RuntimeException(message)

data class Property(val value: String)

data class PropertyValidationError(
    val path: Nel<Property>,
    val message: String,
)

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

fun validateNullableStringLength(min: Int, max: Int, value: String?): Validated<ValidationError, String?> {
    if (value == null) {
        return value.valid()
    }
    val length = value.length
    return if ((length >= min) && (length <= max)) {
        Valid(value)
    } else {
        Invalid(ValidationError("The <$value> should be <null> or its length should be between <$min> and <$max>"))
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

val validateString50 = ::validateStringLength.partially1(1).partially1(50)

val validateNullableString50 = ::validateNullableStringLength.partially1(1).partially1(50)

fun <V> Validated<ValidationError, V>.assign(property: Property): ValidatedNel<PropertyValidationError, V> = this
    .mapLeft { error -> PropertyValidationError(nonEmptyListOf(property), error.message) }
    .mapLeft(::nonEmptyListOf)

@JvmName("nestedValidationErrorV")
fun <V> ValidatedNel<ValidationError, V>.assign(property: Property): ValidatedNel<PropertyValidationError, V> = this
    .mapLeft { errors ->
        errors.map { error -> PropertyValidationError(nonEmptyListOf(property), error.message) }
    }

fun <V> ValidatedNel<PropertyValidationError, V>.prepend(property: Property): ValidatedNel<PropertyValidationError, V> =
    this
        .mapLeft { errors ->
            errors.map { error -> PropertyValidationError(Nel(property, error.path), error.message) }
        }
