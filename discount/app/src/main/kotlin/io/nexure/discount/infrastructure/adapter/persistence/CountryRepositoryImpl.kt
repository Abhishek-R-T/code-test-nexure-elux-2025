package io.nexure.discount.infrastructure.adapter.persistence

import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.port.CountryRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CountryRepositoryImpl : CountryRepository {
    override suspend fun findByName(name: String): Country? = newSuspendedTransaction {
        CountryTable.selectAll().where { CountryTable.name eq name }
            .singleOrNull()
            ?.let { Country(it[CountryTable.name], it[CountryTable.vatPercent]) }
    }

    override suspend fun findAll(): List<Country> = newSuspendedTransaction {
        CountryTable.selectAll().map {
            Country(it[CountryTable.name], it[CountryTable.vatPercent])
        }
    }
}
