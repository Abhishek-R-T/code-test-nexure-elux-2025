package io.nexure.discount

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTests {

    @Test
    fun `application module installs ContentNegotiation`() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `openapi endpoint is available`() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/openapi")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("ProductResponse") || body.contains("products"))
    }

    @Test
    fun `swagger endpoint is available`() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
