package io.nexure.discount.infrastructure.adapter.persistence

import io.nexure.discount.domain.model.Country
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CountryRepositoryImplTest {

    private lateinit var repository: CountryRepositoryImpl

    @BeforeTest
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(CountryTable)
            CountryTable.insert {
                it[name] = "Sweden"
                it[vatPercent] = 25.0
            }
            CountryTable.insert {
                it[name] = "Germany"
                it[vatPercent] = 19.0
            }
        }
        repository = CountryRepositoryImpl()
    }

    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(CountryTable)
        }
    }

    @Test
    fun `findByName returns country when exists`() = runTest {
        val country = repository.findByName("Sweden")

        assertEquals("Sweden", country?.name)
        assertEquals(25.0, country?.vatPercent)
    }

    @Test
    fun `findByName returns null when country does not exist`() = runTest {
        val country = repository.findByName("Spain")

        assertNull(country)
    }

    @Test
    fun `findAll returns all countries`() = runTest {
        val countries = repository.findAll()

        assertEquals(2, countries.size)
        assertEquals("Sweden", countries[0].name)
        assertEquals("Germany", countries[1].name)
    }
}
