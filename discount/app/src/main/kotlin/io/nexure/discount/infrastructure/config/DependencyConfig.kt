package io.nexure.discount.infrastructure.config

import io.nexure.discount.domain.port.CountryRepository
import io.nexure.discount.domain.port.ProductRepository
import io.nexure.discount.infrastructure.adapter.persistence.CountryRepositoryImpl
import io.nexure.discount.infrastructure.adapter.persistence.ProductRepositoryImpl
import io.nexure.discount.usecase.ApplyDiscountUseCase
import io.nexure.discount.usecase.GetProductsByCountryUseCase

object DependencyConfig {
    private val countryRepository: CountryRepository = CountryRepositoryImpl()
    private val productRepository: ProductRepository = ProductRepositoryImpl()

    val getProductsByCountryUseCase = GetProductsByCountryUseCase(productRepository, countryRepository)
    val applyDiscountUseCase = ApplyDiscountUseCase(productRepository)
}
