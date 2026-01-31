package io.nexure.discount.infrastructure.adapter.web

import io.nexure.discount.domain.model.Product
import io.nexure.discount.domain.service.PriceCalculator
import io.nexure.discount.generated.model.ProductResponse
import io.nexure.discount.generated.model.Discount as DiscountDto

fun Product.toResponse() = ProductResponse(
    id = id,
    name = name,
    basePrice = basePrice,
    country = country.name,
    discounts = discounts.map { DiscountDto(it.discountId, it.percent) },
    finalPrice = PriceCalculator.calculateFinalPrice(this)
)
