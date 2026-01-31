package io.nexure.discount.domain.service

import io.nexure.discount.domain.model.Product

object PriceCalculator {
    fun calculateFinalPrice(product: Product): Double {
        val totalDiscountPercent = product.discounts.sumOf { it.percent }
        val priceAfterDiscount = product.basePrice * (1 - totalDiscountPercent / 100)
        return priceAfterDiscount * (1 + product.country.vatPercent / 100)
    }
}
