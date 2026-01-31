package io.nexure.discount.infrastructure.adapter.web

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.nexure.discount.generated.model.Error
import io.nexure.discount.usecase.GetProductsByCountryUseCase
import io.nexure.discount.usecase.GetProductsResult
import org.slf4j.LoggerFactory

fun Route.getProductsRoute(getProductsByCountryUseCase: GetProductsByCountryUseCase) {
    val logger = LoggerFactory.getLogger("GetProductsRoute")

    get("/products") {
        val countryParam = call.request.queryParameters["country"]
        if (countryParam == null) {
            call.respond(HttpStatusCode.BadRequest, Error("Country parameter is required"))
            return@get
        }

        logger.debug("Getting products for country: $countryParam")
        
        when (val result = getProductsByCountryUseCase.execute(countryParam)) {
            is GetProductsResult.Success -> {
                call.respond(HttpStatusCode.OK, result.products.map { it.toResponse() })
            }
            is GetProductsResult.CountryNotFound -> {
                call.respond(HttpStatusCode.BadRequest, Error("Country not found: $countryParam"))
            }
        }
    }
}
