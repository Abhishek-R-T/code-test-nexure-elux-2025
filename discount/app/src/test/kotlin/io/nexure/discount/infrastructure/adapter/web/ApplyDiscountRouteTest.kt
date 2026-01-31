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
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.domain.model.Product
import io.nexure.discount.usecase.ApplyDiscountResult
import io.nexure.discount.usecase.ApplyDiscountUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplyDiscountRouteTest {

    @Test
    fun `PUT discount returns 200 when discount is applied successfully`() = testApplication {
        val useCase = mockk<ApplyDiscountUseCase>()
        val product = Product(
            "prod-1", "Laptop", 1000.0,
            Country("Sweden", 25.0),
            listOf(Discount("summer-sale", 10.0))
        )
        coEvery { useCase.execute("prod-1", any()) } returns ApplyDiscountResult.Success(product)

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
            routing { applyDiscountRoute(useCase) }
        }

        val response = client.put("/products/prod-1/discount") {
            contentType(ContentType.Application.Json)
            setBody("""{"discountId":"summer-sale","percent":10.0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("summer-sale"))
    }

    @Test
    fun `PUT discount returns 304 when discount already applied`() = testApplication {
        val useCase = mockk<ApplyDiscountUseCase>()
        val product = Product("prod-1", "Laptop", 1000.0, Country("Sweden", 25.0), emptyList())
        coEvery { useCase.execute("prod-1", any()) } returns ApplyDiscountResult.DiscountAlreadyApplied(product)

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
            routing { applyDiscountRoute(useCase) }
        }

        val response = client.put("/products/prod-1/discount") {
            contentType(ContentType.Application.Json)
            setBody("""{"discountId":"summer-sale","percent":10.0}""")
        }

        assertEquals(HttpStatusCode.NotModified, response.status)
    }

    @Test
    fun `PUT discount returns 404 when product not found`() = testApplication {
        val useCase = mockk<ApplyDiscountUseCase>()
        coEvery { useCase.execute("prod-999", any()) } returns ApplyDiscountResult.ProductNotFound

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
            routing { applyDiscountRoute(useCase) }
        }

        val response = client.put("/products/prod-999/discount") {
            contentType(ContentType.Application.Json)
            setBody("""{"discountId":"test","percent":10.0}""")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT discount returns 400 when total discount exceeds 100 percent`() = testApplication {
        val useCase = mockk<ApplyDiscountUseCase>()
        coEvery { useCase.execute("prod-1", any()) } returns ApplyDiscountResult.TotalDiscountExceeded

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { json() }
            routing { applyDiscountRoute(useCase) }
        }

        val response = client.put("/products/prod-1/discount") {
            contentType(ContentType.Application.Json)
            setBody("""{"discountId":"loyalty","percent":50.0}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("100%"))
    }
}
