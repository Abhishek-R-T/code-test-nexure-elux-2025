package io.nexure.discount.usecase

import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import io.nexure.discount.domain.port.ProductRepository
import io.nexure.discount.domain.service.DiscountService

class ApplyDiscountUseCase(
    private val productRepository: ProductRepository
) {
    suspend fun execute(productId: String, discount: Discount): ApplyDiscountResult {
        DiscountService.validateDiscount(discount)

        val product = productRepository.findById(productId)
            ?: return ApplyDiscountResult.ProductNotFound

        if (DiscountService.hasDiscount(product, discount.discountId)) {
            return ApplyDiscountResult.DiscountAlreadyApplied(product)
        }

        val updatedProduct = productRepository.applyDiscount(productId, discount)
            ?: return ApplyDiscountResult.ProductNotFound

        return ApplyDiscountResult.Success(updatedProduct)
    }
}

sealed class ApplyDiscountResult {
    data class Success(val product: Product) : ApplyDiscountResult()
    data class DiscountAlreadyApplied(val product: Product) : ApplyDiscountResult()
    data object ProductNotFound : ApplyDiscountResult()
}
