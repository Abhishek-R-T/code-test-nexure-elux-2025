# Country-Based Product API

A Kotlin/Ktor service that manages products and discounts with country-specific VAT calculations and guaranteed idempotent discount application.

## Features

- Product catalog management with country-specific VAT
- Idempotent discount application with concurrency safety
- PostgreSQL database with unique constraints for race condition prevention
- Hexagonal architecture for clean separation of concerns
- Docker support for easy deployment

## Prerequisites

- JDK 22
- Docker and Docker Compose (for containerized deployment)
- Gradle 8.5+ (included via wrapper)

## Build and Run

### Using Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up --build

# The API will be available at http://localhost:8082
```

### Local Development

```bash
# Start PostgreSQL
docker-compose up postgres

# Run the application
./gradlew :app:run

# Or build and run
./gradlew :app:installDist
./app/build/install/app/bin/app
```

### Run Tests

```bash
./gradlew test
```

## API Endpoints

### Get Products by Country

```bash
curl "http://localhost:8082/products?country=Sweden"
```

Response:
```json
[
  {
    "id": "prod-1",
    "name": "Laptop",
    "basePrice": 1000.0,
    "country": "Sweden",
    "discounts": [],
    "finalPrice": 1250.0
  }
]
```

### Apply Discount to Product

```bash
curl -X PUT "http://localhost:8082/products/prod-1/discount" \
  -H "Content-Type: application/json" \
  -d '{
    "discountId": "summer-sale",
    "percent": 10.0
  }'
```

Response (first request):
```json
{
  "id": "prod-1",
  "name": "Laptop",
  "basePrice": 1000.0,
  "country": "Sweden",
  "discounts": [
    {
      "discountId": "summer-sale",
      "percent": 10.0
    }
  ],
  "finalPrice": 1125.0
}
```

Response (duplicate request):
```
HTTP 304 Not Modified
```

### Apply Multiple Discounts

```bash
# Apply second discount
curl -X PUT "http://localhost:8082/products/prod-1/discount" \
  -H "Content-Type: application/json" \
  -d '{
    "discountId": "loyalty",
    "percent": 5.0
  }'
```

Response:
```json
{
  "id": "prod-1",
  "name": "Laptop",
  "basePrice": 1000.0,
  "country": "Sweden",
  "discounts": [
    {
      "discountId": "summer-sale",
      "percent": 10.0
    },
    {
      "discountId": "loyalty",
      "percent": 5.0
    }
  ],
  "finalPrice": 1062.5
}
```

### Test Idempotency with Concurrent Requests

Bash:
```bash
for i in {1..10}; do
  curl -X PUT "http://localhost:8082/products/prod-3/discount" \
    -H "Content-Type: application/json" \
    -d '{
      "discountId": "concurrent-test",
      "percent": 15.0
    }' &
done
wait
```

PowerShell:
```powershell
1..10 | ForEach-Object -Parallel {
  curl -X PUT "http://localhost:8082/products/prod-3/discount" `
    -H "Content-Type: application/json" `
    -d '{
      "discountId": "concurrent-test",
      "percent": 15.0
    }'
}
```

## Supported Countries

Country VAT rates are stored in the database and can be configured:

| Country | VAT |
|---------|-----|
| Sweden  | 25% |
| Germany | 19% |
| France  | 20% |

To add new countries, insert into the `countries` table:
```sql
INSERT INTO countries (name, vat_percent) VALUES ('Spain', 21.0);
```

## Price Calculation

```
finalPrice = basePrice × (1 - totalDiscount%) × (1 + VAT%)
```

## Environment Variables

- `DB_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/discount_db`)
- `DB_USER`: Database user (default: `discount_user`)
- `DB_PASSWORD`: Database password (default: `discount_pass`)
- `DB_MAX_POOL_SIZE`: Connection pool size (default: `10`)
- `PORT`: Application port (default: `8082`)

## Project Structure

```
app/src/main/kotlin/io/nexure/discount/
├── domain/
│   ├── model/              # Pure data classes
│   │   ├── Product.kt
│   │   ├── Discount.kt
│   │   └── Country.kt
│   ├── service/            # Domain business logic
│   │   ├── PriceCalculator.kt
│   │   └── DiscountService.kt
│   └── port/               # Repository interfaces
│       ├── ProductRepository.kt
│       └── CountryRepository.kt
├── usecase/                # Application business logic
│   ├── GetProductsByCountryUseCase.kt
│   └── ApplyDiscountUseCase.kt
├── infrastructure/
│   ├── adapter/
│   │   ├── persistence/    # Database implementation
│   │   │   ├── Tables.kt
│   │   │   ├── ProductRepositoryImpl.kt
│   │   │   └── CountryRepositoryImpl.kt
│   │   └── web/            # HTTP layer
│   │       ├── Dtos.kt
│   │       ├── GetProductsRoute.kt
│   │       ├── ApplyDiscountRoute.kt
│   │       └── Routes.kt
│   └── config/             # Configuration
│       ├── DatabaseConfig.kt
│       └── DependencyConfig.kt
└── Application.kt          # Main entry point

app/src/main/resources/
├── application.conf        # Configuration with env overrides
├── openapi.yaml            # OpenAPI specification
└── logback.xml             # Logging configuration

app/build/generated/        # Auto-generated from OpenAPI spec
└── src/main/kotlin/io/nexure/discount/generated/
    └── models/
        ├── ProductResponse.kt
        ├── ApplyDiscountRequest.kt
        ├── Discount.kt
        └── Error.kt
```

## API Documentation

Swagger UI is available at:
- **Swagger UI**: http://localhost:8082/swagger
- **OpenAPI JSON**: http://localhost:8082/openapi

## Testing

The project includes comprehensive unit tests using MockK:

```bash
# Run all tests
./gradlew test
```

