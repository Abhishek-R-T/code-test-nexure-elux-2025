package io.nexure.discount.usecase

import io.nexure.discount.domain.model.Product
import io.nexure.discount.domain.port.CountryRepository
import io.nexure.discount.domain.port.ProductRepository

class GetProductsByCountryUseCase(
    private val productRepository: ProductRepository,
    private val countryRepository: CountryRepository
) {
    suspend fun execute(countryName: String): GetProductsResult {
        val country = countryRepository.findByName(countryName)
            ?: return GetProductsResult.CountryNotFound
        
        val products = productRepository.findByCountry(country)
        return GetProductsResult.Success(products)
    }
}

sealed class GetProductsResult {
    data class Success(val products: List<Product>) : GetProductsResult()
    data object CountryNotFound : GetProductsResult()
}
