package io.nexure.discount.domain.model

data class Product(
    val id: String,
    val name: String,
    val basePrice: Double,
    val country: Country,
    val discounts: List<Discount> = emptyList()
)
