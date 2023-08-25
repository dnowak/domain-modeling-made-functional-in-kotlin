package io.github.dnowak.order.taking.common

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.arbs.geo.zipcodes
import io.kotest.property.assume
import io.kotest.property.checkAll

class ZipCodeTest : FreeSpec({
    "ZipCode" - {
        "validate" - {
            "accepts" {
                Arb.zipcodes().checkAll { zipcode ->
                    ZipCode.validate(zipcode).shouldBeRight().value shouldBe zipcode
                }
            }
            "rejects" {
                Arb.string().checkAll { zipcode ->
                    assume(invalidZipCodeValue(zipcode))
                    ZipCode.validate(zipcode)
                        .shouldBeLeft().all.shouldExist { error -> error.message.contains(zipcode) }
                }
            }
        }
        "create" - {
            "accepts" {
                Arb.zipcodes().checkAll { zipcode ->
                    ZipCode.create(zipcode).value shouldBe zipcode
                }
            }
            "rejects" {
                Arb.string().checkAll { zipcode ->
                    assume(invalidZipCodeValue(zipcode))
                    val exception = shouldThrow<ValidationException> {
                        ZipCode.create(zipcode)
                    }
                    exception.message shouldContain zipcode
                }
            }
        }
        //TODO: is it necessary?
        "equality" - {
            Arb.zipcodes().checkAll { zipcode ->
                ZipCode.create(zipcode) shouldBe ZipCode.create(zipcode)
            }
        }
    }
})