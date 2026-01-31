package io.nexure.discount.domain.service

import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PriceCalculatorTest {

    @Test
    fun `should calculate final price with VAT and no discounts`() {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = emptyList()
        )

        val finalPrice = PriceCalculator.calculateFinalPrice(product)

        assertEquals(1250.0, finalPrice)
    }

    @Test
    fun `should calculate final price with VAT and single discount`() {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = listOf(Discount("summer-sale", 10.0))
        )

        val finalPrice = PriceCalculator.calculateFinalPrice(product)

        assertEquals(1125.0, finalPrice)
    }

    @Test
    fun `should calculate final price with VAT and multiple discounts`() {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Germany", 19.0),
            discounts = listOf(
                Discount("summer-sale", 10.0),
                Discount("loyalty", 5.0)
            )
        )

        val finalPrice = PriceCalculator.calculateFinalPrice(product)

        assertEquals(1011.5, finalPrice)
    }
}
