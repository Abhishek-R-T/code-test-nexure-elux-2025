package io.nexure.discount.infrastructure.adapter.web

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.nexure.discount.domain.model.Discount
import io.nexure.discount.generated.model.Error
import io.nexure.discount.usecase.ApplyDiscountResult
import io.nexure.discount.usecase.ApplyDiscountUseCase
import org.slf4j.LoggerFactory

fun Route.applyDiscountRoute(applyDiscountUseCase: ApplyDiscountUseCase) {
    val logger = LoggerFactory.getLogger("ApplyDiscountRoute")

    put("/products/{id}/discount") {
        val productId = call.parameters["id"]
        if (productId == null) {
            call.respond(HttpStatusCode.BadRequest, Error("Product ID is required"))
            return@put
        }

        val request = try {
            call.receive<io.nexure.discount.generated.model.ApplyDiscountRequest>()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.BadRequest, Error("Invalid request body"))
            return@put
        }

        logger.debug("Applying discount ${request.discountId} to product $productId")
        val discount = Discount(request.discountId, request.percent)
        
        try {
            when (val result = applyDiscountUseCase.execute(productId, discount)) {
                is ApplyDiscountResult.Success -> {
                    logger.info("Discount applied successfully to product $productId")
                    call.respond(HttpStatusCode.OK, result.product.toResponse())
                }
                is ApplyDiscountResult.DiscountAlreadyApplied -> {
                    logger.debug("Discount already applied to product $productId")
                    call.respond(HttpStatusCode.NotModified)
                }
                is ApplyDiscountResult.ProductNotFound -> {
                    call.respond(HttpStatusCode.NotFound, Error("Product not found"))
                }
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, Error(e.message ?: "Invalid discount"))
        }
    }
}
