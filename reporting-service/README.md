# reporting-service

Microservicio de **reportes** del ecosistema [`spring-sakila-microservices`](../README.md): consultas
de **solo lectura** sobre las vistas y funciones que ya trae el schema de Sakila (ventas por tienda,
catálogos combinados, stock, saldo de un cliente, etc.). No tiene tablas propias ni escribe nada.

## Resumen

| | |
|---|---|
| **Protocolo** | **STOMP sobre WebSocket** (Spring, petición-respuesta por colas) |
| **Puerto** | `8084` — WebSocket en `/ws`, salud en `/actuator/health` |
| **Lo consume** | `api-gateway` (rutas `/api/reporting/*`) |
| **Base de datos** | PostgreSQL `sakila`, compartida con los demás servicios (solo lectura) |
| **Tablas** | ninguna propia: lee **7 vistas** y llama **5 funciones** del schema; el saldo de un cliente lo calcula con una consulta equivalente a la sexta, `get_customer_balance` |
| **Stack** | Spring Boot, Spring WebSocket + STOMP (`@EnableWebSocketMessageBroker`), `JdbcTemplate`, Eureka client |

## Vistas y funciones que usa

**Vistas (7):** `actor_info`, `customer_list`, `film_list`, `nicer_but_slower_film_list`,
`sales_by_film_category`, `sales_by_store`, `staff_list`.

**Funciones que llama (5):** `film_in_stock`, `film_not_in_stock`, `inventory_held_by_customer`,
`inventory_in_stock` y `rewards_report`. La sexta, `get_customer_balance`, existe en el schema pero no
se llama, por la razón de abajo.

> **`get_customer_balance`:** la función tal como viene en Sakila no corre en PostgreSQL (usa el
> `IF(cond, a, b)` de MySQL, sintaxis que Postgres no entiende). Como el schema se deja sin modificar,
> este servicio no la llama: calcula el mismo saldo —tarifas de alquiler + 1 por cada día de retraso −
> pagos hechos— con tres consultas equivalentes (`ReportsService.getCustomerBalance`).

## API (STOMP sobre WebSocket)

Conexión: `ws://localhost:8084/ws`. 13 operaciones: el cliente manda su pedido a `/app/<operación>` y
la respuesta exitosa vuelve **solo a ese cliente**, por `/user/queue/<operación>` (STOMP convierte esto
en una suscripción con destino único por sesión; es la forma nativa de Spring de hacer
petición-respuesta sobre este transporte).

| Operación | Argumentos | Qué devuelve |
|---|---|---|
| `getActorInfo` | `{ page?, limit? }` | cada actor con sus películas, agrupadas por categoría |
| `getCustomerList` | `{ page?, limit? }` | clientes con dirección, ciudad y país |
| `getFilmList` | `{ page?, limit? }` | películas con categoría y reparto |
| `getNicerButSlowerFilmList` | `{ page?, limit? }` | igual que `getFilmList`, con los nombres de actores en formato título |
| `getSalesByFilmCategory` | `{ page?, limit? }` | ventas totales por categoría |
| `getSalesByStore` | `{ page?, limit? }` | ventas totales por tienda, con su gerente |
| `getStaffList` | `{ page?, limit? }` | empleados con dirección, ciudad y país |
| `getFilmInStock` | `{ filmId, storeId }` | ids de inventario de esa película disponibles en esa tienda |
| `getFilmNotInStock` | `{ filmId, storeId }` | ids de inventario de esa película que están alquilados |
| `getCustomerBalance` | `{ customerId, effectiveDate }` | saldo del cliente a esa fecha |
| `getInventoryHeldByCustomer` | `{ inventoryId }` | id del cliente que tiene esa copia |
| `getInventoryInStock` | `{ inventoryId }` | si está disponible |
| `getRewardsReport` | `{ minMonthlyPurchases, minDollarAmountPurchased }` | clientes que cumplen el criterio de recompensa |

Este servicio **no pide autenticación** por sí mismo (la valida el gateway); no lo expongas
directamente fuera de la red del ecosistema.

### Cómo llegan por el gateway

`GET /api/reporting/` + `actors/info`, `customers`, `films`, `films/nicer-but-slower`,
`sales/by-category`, `sales/by-store`, `staff`, `films/{filmId}/in-stock?storeId=`,
`films/{filmId}/not-in-stock?storeId=`, `customers/{customerId}/balance?effectiveDate=`,
`customers/rewards?minMonthlyPurchases=&minDollarAmountPurchased=`, `inventory/{inventoryId}/held-by`
y `inventory/{inventoryId}/in-stock`.

## Errores

`/user/queue/errors` es un canal **compartido por todas las operaciones** de una misma sesión —con
varios pedidos en vuelo a la vez, un cliente necesita saber a cuál pertenece un error sin que el
mensaje lo diga por sí solo. Por eso cada error incluye un campo `operation` (sacado del header
`simpDestination` del mensaje original que falló, con un `@MessageExceptionHandler`):

```json
{ "code": "INVALID_PARAMS", "message": "filmId must be a positive integer.", "operation": "getFilmInStock" }
```

El gateway ya hace este filtrado por ti (correlaciona cada llamada con su propia suscripción de
error) y lo traduce a un error REST:

| Código | HTTP | Cuándo |
|---|---|---|
| `INVALID_PARAMS` | 400 | un parámetro falta o no es un entero positivo |
| `FILM_NOT_FOUND`, `CUSTOMER_NOT_FOUND`, `INVENTORY_NOT_FOUND` | 404 | el id por el que se pregunta no existe |

## Configuración

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | `8084` | puerto HTTP (también el del WebSocket) |
| `DB_HOST` / `DB_PORT` | `localhost` / `5434` | dónde está PostgreSQL |
| `DB_USER` / `DB_PASSWORD` / `DB_NAME` | `postgres` / `postgres` / `sakila` | credenciales |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | registro de Eureka |

## Correrlo por separado (sin Docker Compose)

Requiere Java 25 y PostgreSQL con `sakila` cargada (ver *Preparar la base de datos* en el
[README de la raíz](../README.md)).

```bash
./mvnw spring-boot:run
```

Comprobar que quedó arriba: <http://localhost:8084/actuator/health>. Para probar un reporte sin el
gateway hace falta un cliente STOMP: ver *Probarlo de forma individual* más abajo.

**Conectarlo al gateway:** no hace falta ninguna URL fija — se encuentra vía Eureka. Si este servicio
está caído, las rutas `/api/reporting/*` responden con un error.

Con Docker Compose (todo el ecosistema junto): ver la opción B del [README de la raíz](../README.md).

## Probarlo de forma individual

### 1. Directo al servicio (sin el gateway)

Un WebSocket STOMP no se puede probar con el navegador ni con `curl`. Cualquier cliente STOMP sirve;
por ejemplo, en Node.js con el paquete [`ws`](https://www.npmjs.com/package/ws) armando a mano los
frames `CONNECT`/`SUBSCRIBE`/`SEND` del protocolo STOMP, o con una librería STOMP dedicada
(`@stomp/stompjs`, `stomp.py`, etc.) apuntando a `ws://localhost:8084/ws`.

Y su salud: <http://localhost:8084/actuator/health>.

### 2. A través del gateway (este servicio, más el login)

1. Levanta `discovery-server`, la base de datos, este servicio, **`business-service`** y el gateway.
   `business-service` hace falta solo para el login/registro.
2. Inicia sesión y toma el token (ver *Primer login* en el [README de la raíz](../README.md)).
3. Prueba, por ejemplo, `GET /api/reporting/sales/by-store`, `/films`, `/customers`,
   `/films/1/in-stock?storeId=1` y `/customers/1/balance?effectiveDate=2006-01-01`.

## Estructura

```
src/main/java/.../reportingservice/reports/
├── WebSocketConfig.java    # @EnableWebSocketMessageBroker: endpoint /ws, broker /queue, prefijo /app
├── ReportsService.java     # las 13 consultas, vía JdbcTemplate (sin entidades JPA)
└── ReportsController.java  # un @MessageMapping + @SendToUser por operación, más el manejo de errores
```

Al no tener tablas propias, todo el servicio vive en un solo paquete en vez de dividirse por recurso.
