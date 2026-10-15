# zara-price-service

Servicio REST de Spring Boot que, dada una marca, un producto y una fecha de aplicación, devuelve la
tarifa de precios que corresponde. Prueba técnica de comercio electrónico.

## Qué resuelve

La tabla `PRICES` guarda, por marca y producto, intervalos de fechas con una tarifa, una prioridad y un
precio final. Varios intervalos pueden solaparse. El servicio devuelve **un único resultado**: entre las
tarifas cuyo intervalo contiene la fecha, la de mayor `PRIORITY`.

## Requisitos

- Java 21
- Maven (probado con 3.9.11)

## Cómo ejecutarlo

```bash
mvn spring-boot:run          # arranca en http://localhost:8080
mvn test                     # ejecuta todos los tests
```

La base de datos es H2 en memoria. Se crea y se carga con los datos del ejemplo (`data.sql`) en cada
arranque, sin ninguna configuración.

## API

`GET /prices`

| Parámetro | Tipo | Descripción |
|---|---|---|
| `brandId` | entero positivo | Identificador de la cadena (1 = ZARA) |
| `productId` | entero positivo | Identificador del producto |
| `applicationDate` | fecha-hora ISO-8601, sin zona | Por ejemplo `2020-06-14T16:00:00` |

```bash
curl "http://localhost:8080/prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00"
```

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

### Errores

Todos los errores usan `application/problem+json` (RFC 9457) con el mismo formato:

| Código | Cuándo | Ejemplo de `detail` |
|---|---|---|
| 400 | Parámetro ausente, mal formado o con fecha con zona | `Invalid request content.` |
| 400 | `brandId` o `productId` no positivos, fecha ausente | `brandId must be positive` |
| 404 | Ninguna tarifa aplica a esa marca, producto y fecha | `No price applies to brand 1 and product 999 at 2020-06-14T10:00` |
| 405 | Método distinto de `GET` | |

## Datos de ejemplo

Marca 1 (ZARA), producto 35455:

| Tarifa | Desde | Hasta | Prioridad | Precio |
|---|---|---|---|---|
| 1 | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 0 | 35.50 EUR |
| 2 | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 1 | 25.45 EUR |
| 3 | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 1 | 30.50 EUR |
| 4 | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 1 | 38.95 EUR |

## Arquitectura

Arquitectura hexagonal con la estructura mínima. Las dependencias apuntan siempre hacia el dominio.

```
src/main/java/com/zara/price/
├── PriceApplication.java
├── domain/                       // núcleo, sin Spring ni JPA
│   ├── Price.java                // la tarifa: protege sus invariantes
│   ├── PriceQuery.java           // value object: criterios válidos para buscar una tarifa
│   ├── PriceRepository.java      // puerto de salida
│   ├── InvalidPriceQueryException.java
│   └── PriceNotFoundException.java
├── application/
│   ├── FindApplicablePriceUseCase.java  // puerto de entrada
│   └── PriceService.java                // caso de uso: delega la búsqueda en el repositorio
└── adapter/
    ├── in/web/                   // entrada REST
    │   ├── PriceController.java
    │   ├── PriceRequest.java
    │   ├── PriceResponse.java    // contrato de salida
    │   ├── PriceResponseMapper.java  // MapStruct: Price -> PriceResponse
    │   └── ApiExceptionHandler.java  // excepciones de dominio -> ProblemDetail
    └── out/persistence/          // salida JPA
        ├── PriceEntity.java
        ├── SpringDataPriceRepository.java  // consulta JPQL con ORDER BY y LIMIT
        ├── JpaPriceRepository.java  // implementa PriceRepository
        └── PriceEntityMapper.java   // MapStruct: PriceEntity -> Price
```

## Decisiones de diseño

- **`Price` modela la tarifa; `PriceQuery` encapsula los criterios válidos para buscarla.** Ambos son
  `record` inmutables que validan en su constructor. `Price` exige que `startDate` no sea posterior a
  `endDate`, que el precio no sea negativo y que la moneda no esté vacía. `PriceQuery` exige ids
  positivos y fecha obligatoria.
- **La selección ocurre en la base de datos.** La consulta filtra por marca, producto y fecha, ordena por
  `priority DESC, start_date DESC, id DESC` y devuelve solo la primera fila (`Limit.of(1)`). La
  elección no materializa todas las candidatas en memoria.
- **Regla de selección.** Gana la mayor prioridad. Si empatan, la de `startDate` más reciente, y si
  siguen empatadas, la de `id` más alto. El enunciado no define el empate; así el resultado es siempre
  determinista. La regla vive en el dominio (`Price`) y en el `ORDER BY` de la consulta.
- **Esquema explícito.** El DDL está en `schema.sql` con `spring.jpa.hibernate.ddl-auto: validate`.
  Hibernate comprueba el esquema al arrancar y no lo crea solo. La tabla tiene el índice
  `idx_prices_lookup (brand_id, product_id, start_date, end_date)`.
- **Rendimiento medido, no supuesto.** Con los datos de ejemplo el índice basta. Un índice que incluya
  `priority` no mejora el plan; el índice ordenado solo compensa con muchos solapes por producto. Ver
  la sección [Rendimiento](#rendimiento).
- **Actuator para observar el pool.** Se expone `/actuator/metrics` (solo `health` y `metrics`) para
  leer `hikaricp.connections.*` durante las pruebas de carga.
- **Calidad automática en `mvn verify`.** Tests unitarios (Surefire) y de integración (Failsafe),
  cobertura de líneas mínima del 80 % con JaCoCo (excluye el código generado por MapStruct y la clase
  `main`), y análisis estático con PMD y SpotBugs sin hallazgos.
- **Intervalos inclusivos.** El inicio y el fin son inclusivos: `2020-06-14T18:30:00` sigue en la
  tarifa 2 y `18:30:01` ya no.
- **Fechas sin zona horaria.** Se asume que las fechas de la tabla y las de la petición están en la
  misma zona. Una fecha con `Z` o con desfase se rechaza con 400.
- **Errores de dominio traducidos en el adaptador.** El dominio lanza excepciones propias y
  `ApiExceptionHandler` las convierte en `ProblemDetail`. Los errores estándar de Spring salen con el
  mismo formato.
- **MapStruct** para los dos mapeos (entidad a dominio, dominio a respuesta), de modo que el
  controlador y el repositorio quedan sin código de conversión.
- **`PriceResponse` aparte del dominio.** No expone `id` ni `priority`, que son internos.
- **Paquetes `adapter/in` y `adapter/out`.** Separan la entrada REST de la salida JPA. El caso de uso
  depende del puerto `PriceRepository`, no de Spring Data.

## Tests

Tests unitarios (`mvn test`) y de integración (`mvn verify`), contra la base H2 real (sin mocks de
repositorio).

| Clase | Tipo | Qué comprueba |
|---|---|---|
| `PriceApiIT` | Integración | Los 5 casos del enunciado, límites de intervalo, parámetros inválidos, sin resultado, métodos no permitidos y aislamiento entre marcas y productos |
| `JpaPriceRepositoryTest` | Integración | Desempate por prioridad, fecha de inicio e id |
| `PriceTest` | Unitario | Invariantes de `Price`: fechas, precio, moneda y campos obligatorios |
| `PriceQueryTest` | Unitario | Invariantes de `PriceQuery` |

Los 5 casos del enunciado, marca 1 y producto 35455:

| Test | Petición | Tarifa | Precio |
|---|---|---|---|
| 1 | día 14 a las 10:00 | 1 | 35.50 |
| 2 | día 14 a las 16:00 | 2 | 25.45 |
| 3 | día 14 a las 21:00 | 1 | 35.50 |
| 4 | día 15 a las 10:00 | 3 | 30.50 |
| 5 | día 16 a las 21:00 | 4 | 38.95 |

## Rendimiento

Línea base con k6 sobre H2 (script en [`performance/load-test.js`](performance/load-test.js), 20 s por nivel, 0 % de errores):

| VUs | RPS | p95 | p99 | Hikari `pending` (máx) | Hikari `active` (máx de 10) |
|---|---|---|---|---|---|
| 1 | 1 698 | 1,1 ms | – | 0 | 1 |
| 10 | 13 437 | 1,5 ms | – | 0 | 4 |
| 50 | 20 820 | 4,3 ms | – | 0 | 4 |
| 100 | 17 354 | 11,3 ms | 26,5 ms | 0 | 4 |

Con H2 el pool nunca se satura. `hikaricp.connections.usage` y `acquire` son acumulados desde el arranque. Esta prueba repite la misma petición, así que no refleja un escenario con caché fría.

Reproducir:

```powershell
mvn spring-boot:run
k6 run -e VUS=50 -e DURATION=20s performance/load-test.js
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending
```

Plan de consulta con PostgreSQL 17 (datos en [`performance/seed-prices.sql`](performance/seed-prices.sql), ≈2 M filas, marca 1, producto 50000):

| Caso | Índice | Tiempo | Plan |
|---|---|---|---|
| Pocos solapes | `idx_prices_lookup` | 0,081 ms | Index scan |
| Pocos solapes | + `priority` | 0,090 ms | Igual |
| 50 000 solapes | `idx_prices_lookup` | 19,7 ms | Recorre todas las filas y ordena |
| 50 000 solapes | + `priority` | 20,3 ms | Igual |
| 50 000 solapes | `(brand_id, product_id, priority DESC, start_date DESC, id DESC)` | 0,078 ms | Limit 1, para en la primera fila |

Conclusión: el coste depende de las filas que casan por marca y producto. Con los datos de ejemplo el índice actual basta; el índice ordenado solo compensa con muchos solapes. No se ha probado la aplicación completa contra PostgreSQL.

## Comportamientos conocidos

- Un parámetro repetido (`?brandId=1&brandId=2`) no se rechaza: Spring toma el primer valor.
- La ruta con barra final (`/prices/`) devuelve 404.
