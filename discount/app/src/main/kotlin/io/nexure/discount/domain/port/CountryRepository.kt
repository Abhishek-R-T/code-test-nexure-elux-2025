package io.nexure.discount.domain.port

import io.nexure.discount.domain.model.Country

interface CountryRepository {
    suspend fun findByName(name: String): Country?
    suspend fun findAll(): List<Country>
}
