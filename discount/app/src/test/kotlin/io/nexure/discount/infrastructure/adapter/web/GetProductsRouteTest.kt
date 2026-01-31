package io.nexure.discount.infrastructure.adapter.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import io.nexure.discount.domain.model.Country
import io.nexure.discount.domain.model.Product
import io.nexure.discount.usecase.GetProductsByCountryUseCase
import io.nexure.discount.usecase.GetProductsResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetProductsRouteTest {

    @Test
    fun `GET products returns 200 with products when country is valid`() = testApplication {
        val useCase = mockk<GetProductsByCountryUseCase>()
        val products = listOf(
            Product("prod-1", "Laptop", 1000.0, Country("Sweden", 25.0), emptyList())
        )
        coEvery { useCase.execute("Sweden") } returns GetProductsResult.Success(products)

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
            routing { getProductsRoute(useCase) }
        }

        val response = client.get("/products?country=Sweden")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("prod-1"))
        assertTrue(response.bodyAsText().contains("Laptop"))
    }

    @Test
    fun `GET products returns 400 when country parameter is missing`() = testApplication {
        val useCase = mockk<GetProductsByCountryUseCase>()

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
            routing { getProductsRoute(useCase) }
        }

        val response = client.get("/products")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET products returns 400 when country is invalid`() = testApplication {
        val useCase = mockk<GetProductsByCountryUseCase>()
        coEvery { useCase.execute("InvalidCountry") } returns GetProductsResult.CountryNotFound

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
            routing { getProductsRoute(useCase) }
        }

        val response = client.get("/products?country=InvalidCountry")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
