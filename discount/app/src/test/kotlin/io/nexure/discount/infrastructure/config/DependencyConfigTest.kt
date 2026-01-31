package io.nexure.discount.infrastructure.config

import kotlin.test.Test
import kotlin.test.assertNotNull

class DependencyConfigTest {

    @Test
    fun `getProductsByCountryUseCase is initialized`() {
        assertNotNull(DependencyConfig.getProductsByCountryUseCase)
    }

    @Test
    fun `applyDiscountUseCase is initialized`() {
        assertNotNull(DependencyConfig.applyDiscountUseCase)
    }
}
