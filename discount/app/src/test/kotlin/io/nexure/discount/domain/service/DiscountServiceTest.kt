package io.nexure.discount.domain.service

import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscountServiceTest {

    @Test
    fun `should detect existing discount`() {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = listOf(Discount("summer-sale", 10.0))
        )

        assertTrue(DiscountService.hasDiscount(product, "summer-sale"))
    }

    @Test
    fun `should not detect non-existing discount`() {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = emptyList()
        )

        assertFalse(DiscountService.hasDiscount(product, "summer-sale"))
    }

    @Test
    fun `should validate discount percent is greater than zero`() {
        val discount = Discount("invalid", 0.0)

        assertThrows<IllegalArgumentException> {
            DiscountService.validateDiscount(discount)
        }
    }

    @Test
    fun `should validate discount percent is not greater than 100`() {
        val discount = Discount("invalid", 101.0)

        assertThrows<IllegalArgumentException> {
            DiscountService.validateDiscount(discount)
        }
    }

    @Test
    fun `should accept valid discount`() {
        val discount = Discount("valid", 50.0)
        DiscountService.validateDiscount(discount)
    }
}
