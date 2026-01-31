package io.nexure.discount.infrastructure.adapter.persistence

import org.jetbrains.exposed.sql.Table

object CountryTable : Table("countries") {
    val name = varchar("name", 50)
    val vatPercent = double("vat_percent")
    override val primaryKey = PrimaryKey(name)
}

object ProductTable : Table("products") {
    val id = varchar("id", 255)
    val name = varchar("name", 255)
    val basePrice = double("base_price")
    val country = varchar("country", 50).references(CountryTable.name)
    override val primaryKey = PrimaryKey(id)
}

object DiscountTable : Table("discounts") {
    val productId = varchar("product_id", 255).references(ProductTable.id)
    val discountId = varchar("discount_id", 255)
    val percent = double("percent")
    override val primaryKey = PrimaryKey(productId, discountId)
}
