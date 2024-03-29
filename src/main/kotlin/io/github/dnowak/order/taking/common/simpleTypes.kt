package io.github.dnowak.order.taking.common

import arrow.core.Either
import arrow.core.Either.Companion.zipOrAccumulate
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.EitherNel
import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.partially1
import arrow.core.right
import arrow.core.toEitherNel
import io.github.dnowak.order.taking.common.ProductCode.Gizmo
import io.github.dnowak.order.taking.common.ProductCode.Widget
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal

private fun messages(errors: NonEmptyList<ValidationError>): String =
    errors.map(ValidationError::message).joinToString()

/// An email address
//type EmailAddress = private EmailAddress of string
@JvmInline
value class EmailAddress private constructor(val value: String) {
    companion object {
        fun validate(email: String): EitherNel<ValidationError, EmailAddress> =
            validateRegExp(
                "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}\$",
                email
            ).map(::EmailAddress).mapLeft(::nonEmptyListOf)

        fun create(email: String): EmailAddress = validate(email)
            .fold(
                { errors -> throw ValidationException("Invalid email: <$email> - errors: <${messages(errors)}>") },
                ::identity
            )
    }
}

/// A zip code
//type ZipCode = private ZipCode of string
@JvmInline
value class ZipCode private constructor(val value: String) {
    companion object {
        fun validate(zip: String): EitherNel<ValidationError, ZipCode> =
            validateRegExp("\\d{5}", zip).map(::ZipCode).mapLeft(::nonEmptyListOf)

        fun create(zip: String): ZipCode = validate(zip)
            .fold(
                { errors -> throw ValidationException("Invalid zip code: <$zip> - errors: <${messages(errors)}>") },
                ::identity
            )
    }
}

@Deprecated("Switch to value class")
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
@JvmInline
value class OrderId private constructor(val value: String) {
    companion object {
        fun validate(id: String): EitherNel<ValidationError, OrderId> =
            zipOrAccumulate(
                validateStringLength(1, 10, id),
                validateAlphaUpperNumeric(id)
            ) { _, _ -> OrderId(id) }

        fun create(id: String): OrderId = validate(id)
            .fold(
                { errors -> throw ValidationException("Invalid order id: <$id> - errors: <${messages(errors)}>") },
                ::identity
            )

    }
}

/// An Id for OrderLines. Constrained to be a non-empty string < 10 chars
//type OrderLineId = private OrderLineId of string
@JvmInline
value class OrderLineId private constructor(val value: String) {
    companion object {
        fun validate(id: String): EitherNel<ValidationError, OrderLineId> =
            zipOrAccumulate(
                validateStringLength(1, 10, id),
                validateAlphaUpperNumeric(id)
            ) { _, _ -> OrderLineId(id) }

        fun create(id: String): OrderLineId = validate(id)
            .fold(
                { errors -> throw ValidationException("Invalid order line id: <$id> - errors: <${messages(errors)}>") },
                ::identity
            )
    }
}

/// The codes for Widgets start with a "W" and then four digits
//type WidgetCode = private WidgetCode of string
@JvmInline
value class WidgetCode private constructor(val value: String) {
    companion object {
        fun validate(code: String): EitherNel<ValidationError, WidgetCode> =
            validateRegExp("W\\d{4}", code)
                .map(::WidgetCode)
                .mapLeft(::nonEmptyListOf)

        fun create(code: String): WidgetCode = validate(code)
            .fold(
                { errors -> throw ValidationException("Invalid widget code: <$code> - errors: <${messages(errors)}>") },
                ::identity
            )
    }
}

/// The codes for Gizmos start with a "G" and then three digits.
//type GizmoCode = private GizmoCode of string
@JvmInline
value class GizmoCode private constructor(val value: String) {
    companion object {
        fun validate(code: String): EitherNel<ValidationError, GizmoCode> =
            validateRegExp("G\\d{3}", code)
                .map(::GizmoCode)
                .mapLeft(::nonEmptyListOf)

        fun create(code: String): GizmoCode = validate(code)
            .fold(
                { errors -> throw ValidationException("Invalid gizmo code: <$code> - errors: <${messages(errors)}>") },
                ::identity
            )
    }
}

/// A ProductCode is either a Widget or a Gizmo
/*
type ProductCode =
| Widget of WidgetCode
| Gizmo of GizmoCode
 */
sealed interface ProductCode {
    @JvmInline
    value class Widget(val code: WidgetCode) : ProductCode

    @JvmInline
    value class Gizmo(val code: GizmoCode) : ProductCode

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

        fun validate(code: String): EitherNel<ValidationError, ProductCode> =
            if (StringUtils.startsWith(code, "W")) {
                WidgetCode.validate(code).map(ProductCode::Widget)
            } else if (StringUtils.startsWith(code, "G")) {
                GizmoCode.validate(code).map(ProductCode::Gizmo)
            } else {
                ValidationError("Unknown product code: <$code>").left().toEitherNel()
            }

        fun create(code: String): ProductCode = validate(code)
            .fold(
                { errors -> throw ValidationException("Invalid product code: <$code> - errors: <${messages(errors)}>") },
                ::identity
            )
    }


}

/// Constrained to be a integer between 1 and 1000
//type UnitQuantity = private UnitQuantity of int
@JvmInline
value class UnitQuantity private constructor(val value: Int) {
    companion object {
        fun validate(quantity: Int): EitherNel<ValidationError, UnitQuantity> =
            validateIntInRange(1, 1000, quantity).map(::UnitQuantity).mapLeft(::nonEmptyListOf)

        fun create(quantity: Int): UnitQuantity = validate(quantity)
            .fold(
                { errors -> throw ValidationException("Invalid unit quantity: <$quantity> - errors: <${messages(errors)}>") },
                ::identity
            )
    }
}

/// Constrained to be a decimal between 0.05 and 100.00
//type KilogramQuantity = private KilogramQuantity of decimal
@JvmInline
value class KilogramQuantity internal constructor(val value: BigDecimal) {
    companion object {
        private val min = BigDecimal("0.05")
        private val max = BigDecimal("100.00")

        fun validate(quantity: BigDecimal): EitherNel<ValidationError, KilogramQuantity> {
            return validateBigDecimalInRange(min, max, quantity)
                .map(::KilogramQuantity).mapLeft(::nonEmptyListOf)
        }

        fun create(quantity: BigDecimal): KilogramQuantity = validate(quantity)
            .fold(
                { errors ->
                    throw ValidationException(
                        "Invalid kilogram quantity: <$quantity> - errors: <${
                            messages(
                                errors
                            )
                        }>"
                    )
                },
                ::identity
            )

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
sealed interface OrderQuantity {
    data class Unit(val value: UnitQuantity) : OrderQuantity
    data class Kilogram(val value: KilogramQuantity) : OrderQuantity

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

        fun validate(productCode: ProductCode, value: BigDecimal): EitherNel<ValidationError, OrderQuantity> =
            when (productCode) {
                is Gizmo -> KilogramQuantity.validate(value).map(OrderQuantity::Kilogram)
                //TODO: handle not exact int value as ValidationError
                is Widget -> UnitQuantity.validate(value.intValueExact()).map(OrderQuantity::Unit)
            }
    }

    val quantity: BigDecimal
        get() = when (this) {
            is Kilogram -> value.value
            is Unit -> value.value.toBigDecimal()
        }
}

/// Constrained to be a decimal between 0.0 and 1000.00
//type Price = private Price of decimal
//TODO: implement as value class. Now it causes problems - https://github.com/dnowak/domain-modeling-made-functional-in-kotlin/issues/17
//@JvmInline
//value class Price private constructor(val value: BigDecimal) {
class Price private constructor(value: BigDecimal) : SimpleType<BigDecimal>(value) {
    companion object {
        private val min = BigDecimal("0.00")
        private val max = BigDecimal("1000.00")

        fun validate(amount: BigDecimal): EitherNel<ValidationError, Price> =
            validateBigDecimalInRange(min, max, amount)
                .map(::Price).mapLeft(::nonEmptyListOf)

        fun create(amount: BigDecimal): Price = validate(amount)
            .fold(
                { errors -> throw ValidationException("Invalid price: <$amount> - errors: <${messages(errors)}>") },
                ::identity
            )

        fun create(amount: String): Price = create(BigDecimal(amount))
    }
}


/// Constrained to be a decimal between 0.0 and 10000.00
//type BillingAmount = private BillingAmount of decimal
//class BillingAmount private constructor(value: BigDecimal) : SimpleType<BigDecimal>(value) {
@JvmInline
value class BillingAmount(val value: BigDecimal) {
    companion object {
        private val min = BigDecimal("0.00")
        private val max = BigDecimal("10000.00")

        fun validate(amount: BigDecimal): EitherNel<ValidationError, BillingAmount> {
            return validateBigDecimalInRange(min, max, amount)
                .map(::BillingAmount).mapLeft(::nonEmptyListOf)
        }

        fun create(amount: BigDecimal): BillingAmount = validate(amount)
            .fold(
                { errors -> throw ValidationException("Invalid billing amount: <$amount> - errors: <${messages(errors)}>") },
                ::identity
            )

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

fun validateRegExp(pattern: String, value: String): Either<ValidationError, String> =
    if (Regex(pattern).matches(value)) {
        Right(value)
    } else {
        Left(ValidationError("<$value> must match the pattern <$pattern>"))
    }

fun validateStringLength(min: Int, max: Int, value: String): Either<ValidationError, String> {
    val length = value.length
    return if ((length >= min) && (length <= max)) {
        Right(value)
    } else {
        Left(ValidationError("The length of <$value> should be between <$min> and <$max>"))
    }
}

fun validateAlphaUpperNumeric(value: String): Either<ValidationError, String> =
    validateRegExp("^[A-Z0-9]*$", value)

fun validateNullableStringLength(min: Int, max: Int, value: String?): Either<ValidationError, String?> {
    if (value == null) {
        return value.right()
    }
    val length = value.length
    return if ((length >= min) && (length <= max)) {
        Right(value)
    } else {
        Left(ValidationError("The <$value> should be <null> or its length should be between <$min> and <$max>"))
    }
}

fun validateIntInRange(min: Int, max: Int, value: Int): Either<ValidationError, Int> =
    if ((value >= min) && (value <= max)) {
        Right(value)
    } else {
        Left(ValidationError("The <$value> should be between <$min> and <$max>"))
    }

fun validateBigDecimalInRange(
    min: BigDecimal,
    max: BigDecimal,
    value: BigDecimal,
): Either<ValidationError, BigDecimal> =
    if ((value >= min) && (value <= max)) {
        Right(value)
    } else {
        Left(ValidationError("The <$value> should be between <$min> and <$max>"))
    }

val validateString50 = ::validateStringLength.partially1(1).partially1(50)

val validateNullableString50 = ::validateNullableStringLength.partially1(1).partially1(50)

fun <V> Either<ValidationError, V>.assign(property: Property): EitherNel<PropertyValidationError, V> = this
    .mapLeft { error -> PropertyValidationError(nonEmptyListOf(property), error.message) }
    .mapLeft(::nonEmptyListOf)

@JvmName("nestedValidationErrorV")
fun <V> EitherNel<ValidationError, V>.assign(property: Property): EitherNel<PropertyValidationError, V> = this
    .mapLeft { errors ->
        errors.map { error -> PropertyValidationError(nonEmptyListOf(property), error.message) }
    }

fun <V> EitherNel<PropertyValidationError, V>.prepend(property: Property): EitherNel<PropertyValidationError, V> =
    this
        .mapLeft { errors ->
            errors.map { error -> PropertyValidationError(Nel(property, error.path), error.message) }
        }
