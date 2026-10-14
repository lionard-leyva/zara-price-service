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
│   ├── Price.java                // la tarifa: protege sus invariantes y decide la precedencia
│   ├── PriceQuery.java           // value object: criterios válidos para buscar una tarifa
│   ├── PriceRepository.java      // puerto de salida
│   ├── InvalidPriceQueryException.java
│   └── PriceNotFoundException.java
├── application/
│   └── PriceService.java         // caso de uso: busca candidatas y elige la ganadora
└── adapter/
    ├── PriceController.java      // entrada REST
    ├── PriceResponse.java        // contrato de salida
    ├── PriceResponseMapper.java  // MapStruct: Price -> PriceResponse
    ├── ApiExceptionHandler.java  // excepciones de dominio -> ProblemDetail
    ├── PriceEntity.java          // salida JPA
    ├── SpringDataPriceRepository.java
    ├── JpaPriceRepository.java   // implementa PriceRepository
    └── PriceEntityMapper.java    // MapStruct: PriceEntity -> Price
```

## Decisiones de diseño

- **`Price` modela la tarifa; `PriceQuery` encapsula los criterios válidos para buscarla.** Ambos son
  `record` inmutables que validan en su constructor. `Price` exige que `startDate` no sea posterior a
  `endDate`, que el precio no sea negativo y que la moneda no esté vacía. `PriceQuery` exige ids
  positivos y fecha obligatoria.
- **Eficiencia de la consulta.** La base de datos solo filtra los intervalos que contienen la fecha
  (`:fecha BETWEEN start_date AND end_date`, por marca y producto). No se recorre la tabla entera. La
  elección entre las candidatas, que son pocas, la hace el dominio.
- **Regla de selección.** Gana la mayor prioridad. Si empatan, la de `startDate` más reciente, y si
  siguen empatadas, la de `id` más alto. El enunciado no define el empate; así el resultado es siempre
  determinista.
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

## Tests

61 tests, todos contra la aplicación completa y la base H2 real (sin mocks de repositorio).

| Clase | Qué comprueba |
|---|---|
| `PriceControllerTest` | Los 5 casos del enunciado, límites de intervalo, parámetros inválidos, sin resultado, métodos no permitidos y aislamiento entre marcas y productos |
| `PriceTest` | Regla de precedencia e invariantes de `Price` |
| `PriceQueryTest` | Invariantes de `PriceQuery` |
| `PriceApplicationTests` | Arranque del contexto |

Los 5 casos del enunciado, marca 1 y producto 35455:

| Test | Petición | Tarifa | Precio |
|---|---|---|---|
| 1 | día 14 a las 10:00 | 1 | 35.50 |
| 2 | día 14 a las 16:00 | 2 | 25.45 |
| 3 | día 14 a las 21:00 | 1 | 35.50 |
| 4 | día 15 a las 10:00 | 3 | 30.50 |
| 5 | día 16 a las 21:00 | 4 | 38.95 |

## Comportamientos conocidos

- Un parámetro repetido (`?brandId=1&brandId=2`) no se rechaza: Spring toma el primer valor.
- La ruta con barra final (`/prices/`) devuelve 404.
