package io.nexure.discount.domain.port

import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product

interface ProductRepository {
    suspend fun findById(id: String): Product?
    suspend fun findByCountry(country: Country): List<Product>
    suspend fun applyDiscount(productId: String, discount: Discount): Product?
}
