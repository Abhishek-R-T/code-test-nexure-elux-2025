package io.nexure.discount.usecase

import io.mockk.coEvery
import io.mockk.mockk
import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Product
import io.nexure.discount.domain.port.CountryRepository
import io.nexure.discount.domain.port.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetProductsByCountryUseCaseTest {

    private val productRepository = mockk<ProductRepository>()
    private val countryRepository = mockk<CountryRepository>()
    private val useCase = GetProductsByCountryUseCase(productRepository, countryRepository)

    @Test
    fun `should return CountryNotFound when country does not exist`() = runTest {
        coEvery { countryRepository.findByName("Unknown") } returns null

        val result = useCase.execute("Unknown")

        assertTrue(result is GetProductsResult.CountryNotFound)
    }

    @Test
    fun `should return products for valid country`() = runTest {
        val country = Country("Sweden", 25.0)
        val products = listOf(
            Product("prod-1", "Laptop", 1000.0, country, emptyList()),
            Product("prod-2", "Mouse", 50.0, country, emptyList())
        )

        coEvery { countryRepository.findByName("Sweden") } returns country
        coEvery { productRepository.findByCountry(country) } returns products

        val result = useCase.execute("Sweden")

        assertTrue(result is GetProductsResult.Success)
        assertEquals(2, result.products.size)
    }

    @Test
    fun `should return empty list when no products for country`() = runTest {
        val country = Country("France", 20.0)

        coEvery { countryRepository.findByName("France") } returns country
        coEvery { productRepository.findByCountry(country) } returns emptyList()

        val result = useCase.execute("France")

        assertTrue(result is GetProductsResult.Success)
        assertEquals(0, result.products.size)
    }
}
