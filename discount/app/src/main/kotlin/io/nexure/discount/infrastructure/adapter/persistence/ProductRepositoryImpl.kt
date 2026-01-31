package io.nexure.discount.infrastructure.adapter.persistence

import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import io.nexure.discount.domain.port.ProductRepository
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

class ProductRepositoryImpl : ProductRepository {
    private val logger = LoggerFactory.getLogger(ProductRepositoryImpl::class.java)

    override suspend fun findById(id: String): Product? = newSuspendedTransaction {
        fetchProduct(id)
    }

    override suspend fun findByCountry(country: Country): List<Product> = newSuspendedTransaction {
        ProductTable.selectAll().where { ProductTable.country eq country.name }
            .mapNotNull { row -> fetchProduct(row[ProductTable.id]) }
    }

    override suspend fun applyDiscount(productId: String, discount: Discount): Product? = newSuspendedTransaction {
        try {
            DiscountTable.insert {
                it[DiscountTable.productId] = productId
                it[discountId] = discount.discountId
                it[percent] = discount.percent
            }
        } catch (e: ExposedSQLException) {
            val message = e.message?.lowercase() ?: ""
            if (message.contains("duplicate key") || message.contains("unique") || message.contains("constraint")) {
                logger.debug("Discount ${discount.discountId} already applied to product $productId")
                return@newSuspendedTransaction null
            } else {
                throw e
            }
        }
        fetchProduct(productId)
    }

    private fun fetchProduct(id: String): Product? {
        val productRow = ProductTable.selectAll().where { ProductTable.id eq id }.singleOrNull() ?: return null

        val countryName = productRow[ProductTable.country]
        val countryRow = CountryTable.selectAll().where { CountryTable.name eq countryName }.single()
        val country = Country(countryRow[CountryTable.name], countryRow[CountryTable.vatPercent])

        val discounts = DiscountTable.selectAll().where { DiscountTable.productId eq id }
            .map { Discount(it[DiscountTable.discountId], it[DiscountTable.percent]) }

        return Product(
            id = productRow[ProductTable.id],
            name = productRow[ProductTable.name],
            basePrice = productRow[ProductTable.basePrice],
            country = country,
            discounts = discounts
        )
    }
}
