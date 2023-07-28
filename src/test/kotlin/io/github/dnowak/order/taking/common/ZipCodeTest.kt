package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ZipCodeTest : FreeSpec({
    val validZipCodes = listOf("12345", "11223", "77766")
    val invalidZipCodes = listOf("", " ", "1", "12", "123", "1234", "abcde", "ABCDE")
    "ZipCode" - {
        "validate" - {
            validZipCodes.forEach { zip ->
                "accepts <$zip>" {
                    ZipCode.validate(zip).shouldBeRight().value shouldBe zip
                }
            }
            invalidZipCodes.forEach { zip ->
                "rejects <$zip>" {
                    ZipCode.validate(zip).shouldBeLeft().all.shouldExist { error -> error.message.contains(zip) }
                }
            }
        }
        "create" - {
            validZipCodes.forEach { zip ->
                "accepts <$zip>" {
                    ZipCode.create(zip).value shouldBe zip
                }
            }
            invalidZipCodes.forEach { zip ->
                "rejects <$zip>" {
                    val exception = shouldThrow<ValidationException> {
                        ZipCode.create(zip)
                    }
                    exception.message shouldContain "<$zip>"
                }
            }
        }
        "equality" - {
            validZipCodes.forEach { zip ->
                "checks <$zip>" {
                    ZipCode.create(zip) shouldBe ZipCode.create(zip)
                }
            }
        }
    }
})