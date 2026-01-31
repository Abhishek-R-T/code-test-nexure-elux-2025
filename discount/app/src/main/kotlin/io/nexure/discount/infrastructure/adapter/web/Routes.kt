package io.nexure.discount.infrastructure.adapter.web

import io.ktor.server.routing.*
import io.nexure.discount.usecase.ApplyDiscountUseCase
import io.nexure.discount.usecase.GetProductsByCountryUseCase

fun Route.configureRoutes(
    getProductsByCountryUseCase: GetProductsByCountryUseCase,
    applyDiscountUseCase: ApplyDiscountUseCase
) {
    getProductsRoute(getProductsByCountryUseCase)
    applyDiscountRoute(applyDiscountUseCase)
}
