package io.nexure.discount.infrastructure.adapter.persistence

import io.nexure.discount.domain.model.Discount
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductRepositoryImplTest {

    private lateinit var repository: ProductRepositoryImpl

    @BeforeTest
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(CountryTable, ProductTable, DiscountTable)
            CountryTable.insert {
                it[name] = "Sweden"
                it[vatPercent] = 25.0
            }
            ProductTable.insert {
                it[id] = "prod-1"
                it[name] = "Laptop"
                it[basePrice] = 1000.0
                it[country] = "Sweden"
            }
            ProductTable.insert {
                it[id] = "prod-2"
                it[name] = "Mouse"
                it[basePrice] = 50.0
                it[country] = "Sweden"
            }
        }
        repository = ProductRepositoryImpl()
    }

    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(DiscountTable, ProductTable, CountryTable)
        }
    }

    @Test
    fun `findById returns product when exists`() = runTest {
        val product = repository.findById("prod-1")

        assertNotNull(product)
        assertEquals("prod-1", product.id)
        assertEquals("Laptop", product.name)
        assertEquals(1000.0, product.basePrice)
        assertEquals("Sweden", product.country.name)
    }

    @Test
    fun `findById returns null when product does not exist`() = runTest {
        val product = repository.findById("prod-999")

        assertNull(product)
    }

    @Test
    fun `findByCountry returns products for country`() = runTest {
        val country = io.nexure.discount.domain.model.Country("Sweden", 25.0)
        val products = repository.findByCountry(country)

        assertEquals(2, products.size)
        assertEquals("prod-1", products[0].id)
        assertEquals("prod-2", products[1].id)
    }

    @Test
    fun `applyDiscount adds discount to product`() = runTest {
        val discount = Discount("summer-sale", 10.0)

        val product = repository.applyDiscount("prod-1", discount)

        assertNotNull(product)
        assertEquals(1, product.discounts.size)
        assertEquals("summer-sale", product.discounts[0].discountId)
        assertEquals(10.0, product.discounts[0].percent)
    }

    @Test
    fun `applyDiscount returns null when discount already exists`() = runTest {
        val discount = Discount("summer-sale", 10.0)
        val firstResult = repository.applyDiscount("prod-1", discount)
        assertNotNull(firstResult)

        val secondResult = repository.applyDiscount("prod-1", discount)

        assertNull(secondResult)
    }
}
