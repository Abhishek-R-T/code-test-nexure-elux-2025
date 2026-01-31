package io.nexure.discount.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import io.nexure.discount.domain.port.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplyDiscountUseCaseTest {

    private val productRepository = mockk<ProductRepository>()
    private val useCase = ApplyDiscountUseCase(productRepository)

    @Test
    fun `should return ProductNotFound when product does not exist`() = runTest {
        val discount = Discount("summer-sale", 10.0)
        coEvery { productRepository.findById("prod-1") } returns null

        val result = useCase.execute("prod-1", discount)

        assertTrue(result is ApplyDiscountResult.ProductNotFound)
    }

    @Test
    fun `should return DiscountAlreadyApplied when discount exists`() = runTest {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = listOf(Discount("summer-sale", 10.0))
        )
        val discount = Discount("summer-sale", 10.0)

        coEvery { productRepository.findById("prod-1") } returns product

        val result = useCase.execute("prod-1", discount)

        assertTrue(result is ApplyDiscountResult.DiscountAlreadyApplied)
        coVerify(exactly = 0) { productRepository.applyDiscount(any(), any()) }
    }

    @Test
    fun `should apply discount successfully`() = runTest {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = emptyList()
        )
        val discount = Discount("summer-sale", 10.0)
        val updatedProduct = product.copy(discounts = listOf(discount))

        coEvery { productRepository.findById("prod-1") } returns product
        coEvery { productRepository.applyDiscount("prod-1", discount) } returns updatedProduct

        val result = useCase.execute("prod-1", discount)

        assertTrue(result is ApplyDiscountResult.Success)
        assertEquals(updatedProduct, result.product)
        coVerify { productRepository.applyDiscount("prod-1", discount) }
    }

    @Test
    fun `should return TotalDiscountExceeded when total discount exceeds 100 percent`() = runTest {
        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = listOf(Discount("summer-sale", 60.0))
        )
        val discount = Discount("loyalty", 50.0)

        coEvery { productRepository.findById("prod-1") } returns product

        val result = useCase.execute("prod-1", discount)

        assertTrue(result is ApplyDiscountResult.TotalDiscountExceeded)
        coVerify(exactly = 0) { productRepository.applyDiscount(any(), any()) }
    }
}
