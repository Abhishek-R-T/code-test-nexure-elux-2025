package io.nexure.discount.domain.service

import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product

object DiscountService {
    fun hasDiscount(product: Product, discountId: String): Boolean =
        product.discounts.any { it.discountId == discountId }

    fun addDiscount(product: Product, discount: Discount): Product =
        product.copy(discounts = product.discounts + discount)

    fun validateDiscount(discount: Discount) {
        require(discount.percent > 0 && discount.percent <= 100) {
            "Discount percent must be between 0 (exclusive) and 100"
        }
    }
}
