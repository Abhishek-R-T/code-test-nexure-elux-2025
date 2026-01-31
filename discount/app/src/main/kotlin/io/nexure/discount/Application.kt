package io.nexure.discount

import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.nexure.discount.generated.model.Error
import io.nexure.discount.infrastructure.adapter.web.configureRoutes
import io.nexure.discount.infrastructure.config.DatabaseConfig
import io.nexure.discount.infrastructure.config.DependencyConfig
import org.slf4j.LoggerFactory

const val DISCOUNT_ENDPOINT = "/discount"

fun main() {
    val logger = LoggerFactory.getLogger("Application")
    val config = ConfigFactory.load()
    
    logger.info("Starting application...")
    
    DatabaseConfig.init()
    logger.info("Database initialized")
    
    val port = config.getInt("ktor.deployment.port")
    
    embeddedServer(
        factory = Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::module,
    ).start(true)
}

fun Application.module() {
    val config = ConfigFactory.load()
    val swaggerFile = config.getString("openapi.swaggerFile")
    
    install(ContentNegotiation) {
        json()
    }
    
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, Error("Internal server error"))
        }
    }
    
    routing {
        openAPI(path = "openapi", swaggerFile = swaggerFile)
        swaggerUI(path = "swagger", swaggerFile = swaggerFile)
        
        configureRoutes(
            DependencyConfig.getProductsByCountryUseCase,
            DependencyConfig.applyDiscountUseCase
        )
    }
}
