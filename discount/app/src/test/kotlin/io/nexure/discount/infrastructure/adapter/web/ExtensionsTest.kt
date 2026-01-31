package io.nexure.discount.infrastructure.adapter.web

import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionsTest {

    @Test
    fun `toResponse maps domain Product to API ProductResponse`() {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = listOf(
                Discount("summer-sale", 10.0),
                Discount("loyalty", 5.0)
            )
        )

        val response = product.toResponse()

        assertEquals("prod-1", response.id)
        assertEquals("Laptop", response.name)
        assertEquals(1000.0, response.basePrice)
        assertEquals("Sweden", response.country)
        assertEquals(2, response.discounts.size)
        assertEquals("summer-sale", response.discounts[0].discountId)
        assertEquals(10.0, response.discounts[0].percent)
        assertEquals("loyalty", response.discounts[1].discountId)
        assertEquals(5.0, response.discounts[1].percent)
    }

    @Test
    fun `toResponse handles product with no discounts`() {
        val product = Product(
            id = "prod-2",
            name = "Mouse",
            basePrice = 50.0,
            country = Country("Germany", 19.0),
            discounts = emptyList()
        )

        val response = product.toResponse()

        assertEquals("prod-2", response.id)
        assertEquals("Mouse", response.name)
        assertEquals(50.0, response.basePrice)
        assertEquals("Germany", response.country)
        assertEquals(0, response.discounts.size)
    }
}
