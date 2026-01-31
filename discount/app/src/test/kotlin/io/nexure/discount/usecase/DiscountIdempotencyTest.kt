package io.nexure.discount.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import io.nexure.discount.domain.port.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiscountIdempotencyTest {

    @Test
    fun `should apply discount only once when called multiple times sequentially`() = runTest {
        val productRepository = mockk<ProductRepository>()
        val useCase = ApplyDiscountUseCase(productRepository)

        val productWithoutDiscount = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = emptyList()
        )

        val discount = Discount("summer-sale", 10.0)
        val productWithDiscount = productWithoutDiscount.copy(discounts = listOf(discount))

        coEvery { productRepository.findById("prod-1") } returns productWithoutDiscount
        coEvery { productRepository.applyDiscount("prod-1", discount) } returns productWithDiscount

        val firstResult = useCase.execute("prod-1", discount)
        assertTrue(firstResult is ApplyDiscountResult.Success)

        coEvery { productRepository.findById("prod-1") } returns productWithDiscount

        val secondResult = useCase.execute("prod-1", discount)
        assertTrue(secondResult is ApplyDiscountResult.DiscountAlreadyApplied)

        coVerify(exactly = 1) { productRepository.applyDiscount("prod-1", discount) }
    }

    @Test
    fun `should handle concurrent discount applications idempotently`() = runTest {
        val productRepository = mockk<ProductRepository>()
        val useCase = ApplyDiscountUseCase(productRepository)

        val productWithoutDiscount = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = emptyList()
        )

        val discount = Discount("summer-sale", 10.0)
        val productWithDiscount = productWithoutDiscount.copy(discounts = listOf(discount))

        var callCount = 0
        coEvery { productRepository.findById("prod-1") } answers {
            if (callCount++ == 0) productWithoutDiscount else productWithDiscount
        }
        coEvery { productRepository.applyDiscount("prod-1", discount) } returns productWithDiscount

        val results = (1..10).map {
            async {
                useCase.execute("prod-1", discount)
            }
        }.awaitAll()

        val successCount = results.count { it is ApplyDiscountResult.Success }
        val alreadyAppliedCount = results.count { it is ApplyDiscountResult.DiscountAlreadyApplied }

        assertEquals(1, successCount, "Only one request should succeed")
        assertEquals(9, alreadyAppliedCount, "Other requests should see discount already applied")

        coVerify(exactly = 1) { productRepository.applyDiscount("prod-1", discount) }
    }

    @Test
    fun `should allow different discounts on same product`() = runTest {
        val productRepository = mockk<ProductRepository>()
        val useCase = ApplyDiscountUseCase(productRepository)

        val product = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = emptyList()
        )

        val discount1 = Discount("summer-sale", 10.0)
        val discount2 = Discount("loyalty", 5.0)

        val productWithDiscount1 = product.copy(discounts = listOf(discount1))
        val productWithBothDiscounts = product.copy(discounts = listOf(discount1, discount2))

        coEvery { productRepository.findById("prod-1") } returns product
        coEvery { productRepository.applyDiscount("prod-1", discount1) } returns productWithDiscount1

        val result1 = useCase.execute("prod-1", discount1)
        assertTrue(result1 is ApplyDiscountResult.Success)

        coEvery { productRepository.findById("prod-1") } returns productWithDiscount1
        coEvery { productRepository.applyDiscount("prod-1", discount2) } returns productWithBothDiscounts

        val result2 = useCase.execute("prod-1", discount2)
        assertTrue(result2 is ApplyDiscountResult.Success)

        coVerify { productRepository.applyDiscount("prod-1", discount1) }
        coVerify { productRepository.applyDiscount("prod-1", discount2) }
    }

    @Test
    fun `should prevent duplicate discount even with different percent values`() = runTest {
        val productRepository = mockk<ProductRepository>()
        val useCase = ApplyDiscountUseCase(productRepository)

        val discount1 = Discount("summer-sale", 10.0)
        val productWithDiscount = Product(
            id = "prod-1",
            name = "Laptop",
            basePrice = 1000.0,
            country = Country("Sweden", 25.0),
            discounts = listOf(discount1)
        )

        val discount2 = Discount("summer-sale", 15.0)

        coEvery { productRepository.findById("prod-1") } returns productWithDiscount

        val result = useCase.execute("prod-1", discount2)

        assertTrue(result is ApplyDiscountResult.DiscountAlreadyApplied)
        coVerify(exactly = 0) { productRepository.applyDiscount(any(), any()) }
    }
}
