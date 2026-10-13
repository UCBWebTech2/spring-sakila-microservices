# customer-service

Microservicio de **clientes** del ecosistema [`spring-sakila-microservices`](../README.md): los
clientes y los datos de ubicación que los acompañan (dirección, ciudad, país). Es el único servicio
que habla **dos protocolos a la vez**: parte de sus tablas se atiende por **gRPC** y la otra parte por
**RabbitMQ**, en un mismo proceso.

## Resumen

| | |
|---|---|
| **Protocolos** | **gRPC** (`CustomersService`, `AddressesService`) y **RabbitMQ** (`city`, `country`) |
| **Puertos** | `50051` gRPC · `8082` HTTP, solo `/actuator/health` |
| **Lo consume** | `api-gateway` (rutas `/api/customer/*`) |
| **Base de datos** | PostgreSQL `sakila`, compartida con los demás servicios |
| **Tablas** | `customer`, `address`, `city`, `country` (lectura y escritura) |
| **Stack** | Spring Boot, `spring-boot-starter-grpc-server` (gRPC nativo de Spring), Spring AMQP, Spring Data JPA, Eureka client |

## Qué tabla va por qué protocolo

| Tabla | Protocolo | Contrato | Ruta en el gateway |
|---|---|---|---|
| `customer` | **gRPC** | `service CustomersService` en `src/main/proto/customer.proto` | `/api/customer/customers` |
| `address` | **gRPC** | `service AddressesService` en `src/main/proto/customer.proto` | `/api/customer/addresses` |
| `city` | **RabbitMQ** | colas `cities.find-all` · `find-one` · `create` · `update` · `remove` | `/api/customer/cities` |
| `country` | **RabbitMQ** | colas `countries.find-all` · `find-one` · `create` · `update` · `remove` | `/api/customer/countries` |

`customer` y `address` son las entidades principales y se benefician de un contrato tipado (`.proto`)
con llamadas síncronas; `city` y `country` son datos de catálogo, pequeños y estables, y sirven para
ver mensajería con un broker (una cola de petición-respuesta por operación, en vez de un único canal
compartido).

## Tablas que usa

| Tabla | Qué guarda | Referencias |
|---|---|---|
| `customer` | clientes (nombre, email, activo, fecha de alta) | `address_id` → `address` · `store_id` → `store` *(business-service)* |
| `address` | direcciones | `city_id` → `city` |
| `city` | ciudades | `country_id` → `country` |
| `country` | países | — |

Las claves foráneas de la base siguen activas, incluida la que apunta a `store` (de otro
microservicio). Otros servicios también apuntan a estas tablas (`rental` y `payment` → `customer`;
`staff` y `store` → `address`); por eso borrar un cliente o una dirección en uso da `STILL_REFERENCED`.

## Contrato gRPC (`src/main/proto/customer.proto`)

Paquete `customer`. Los campos del `.proto` van en `snake_case`; el código generado (JAXB-style, con
`protobuf-maven-plugin`) los expone en `camelCase` vía sus getters. Las fechas viajan como texto
ISO-8601.

| Servicio | Métodos |
|---|---|
| `CustomersService` | `List(PaginationRequest)` · `FindOne(ById)` · `Create` · `Update` · `Remove` |
| `AddressesService` | `List(PaginationRequest)` · `FindOne(ById)` · `Create` · `Update` · `Remove` |

Cada método del lado servidor es un `@GrpcService` que extiende el `*ImplBase` generado; los errores
de dominio se traducen a `io.grpc.Status` en una clase `@GrpcAdvice` con un `@GrpcExceptionHandler` por
tipo de excepción (el mismo rol que un `@RestControllerAdvice`, pero para gRPC).

## Colas de RabbitMQ

Una cola de petición-respuesta por operación (10 en total: `cities.*` y `countries.*`, cada una con
`find-all`, `find-one`, `create`, `update` y `remove`). El cliente publica con `replyTo` +
`correlationId` (patrón RPC estándar de AMQP) y esta cola responde con el mismo mecanismo:

| Cola | Recibe | Responde |
|---|---|---|
| `cities.find-all` / `countries.find-all` | `{ page, limit }` | `{ data: [...], meta: { page, limit, total, pages } }` |
| `cities.find-one` / `countries.find-one` | `{ id }` | la ciudad / el país |
| `cities.create` | `{ city, countryId }` | la ciudad creada |
| `countries.create` | `{ country }` | el país creado |
| `cities.update` / `countries.update` | `{ id, ...campos }` | el registro actualizado |
| `cities.remove` / `countries.remove` | `{ id }` | el registro, sin más |

## Errores

| Código | gRPC status | RabbitMQ (`statusCode`) | HTTP en el gateway | Cuándo |
|---|---|---|---|---|
| `CUSTOMER_NOT_FOUND`, `ADDRESS_NOT_FOUND`, `CITY_NOT_FOUND`, `COUNTRY_NOT_FOUND` | `NOT_FOUND` | 404 | 404 | el id no existe |
| `INVALID_COUNTRY_ID`, `INVALID_CITY_ID`, `INVALID_ADDRESS_ID` | `INVALID_ARGUMENT` | 400 | 400 | el cuerpo apunta a un padre que no existe (se valida antes de escribir) |
| `INVALID_STORE_ID` (y otros `INVALID_<TABLA>_ID`) | `INVALID_ARGUMENT` | 400 | 400 | referencia a otro microservicio que no existe: lo detecta Postgres por la clave foránea |
| `STILL_REFERENCED` | `FAILED_PRECONDITION` | 409 | 409 | se borra algo que otros registros usan; el mensaje nombra la tabla |
| `VALIDATION_ERROR` | `INVALID_ARGUMENT` | 400 | 400 | cuerpo mal formado |

En gRPC el código de dominio viaja dentro del mensaje de status, como `"CODIGO: texto"` (un error gRPC
solo tiene un status y un mensaje, sin campo propio para un código); en RabbitMQ viaja el objeto
`{ statusCode, error, message }` como la respuesta misma (AMQP no tiene un canal de error separado del
canal de datos). El gateway traduce ambos a la misma respuesta REST.

## Service discovery

Este servicio escucha gRPC en un puerto (`50051`) **distinto** del que registra en Eureka (`8082`,
el HTTP del health). Ese puerto gRPC se publica como **metadata de la instancia**
(`eureka.instance.metadata-map`), y es así —no por convención de puertos— como el gateway sabe a
cuál conectar el canal una vez que resuelve el host por `LoadBalancerClient`.

## Configuración

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | `8082` | puerto HTTP (solo health) |
| `GRPC_PORT` | `50051` | puerto donde escucha gRPC |
| `DB_HOST` / `DB_PORT` | `localhost` / `5434` | dónde está PostgreSQL |
| `DB_USER` / `DB_PASSWORD` / `DB_NAME` | `postgres` / `postgres` / `sakila` | credenciales |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | broker de RabbitMQ |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `guest` / `guest` | credenciales del broker |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | registro de Eureka |

## Correrlo por separado (sin Docker Compose)

Requiere Java 25, PostgreSQL con `sakila` cargada y **RabbitMQ** corriendo (ver *Preparar la base de
datos* y *Levantar RabbitMQ* en el [README de la raíz](../README.md)).

```bash
./mvnw spring-boot:run
```

Comprobar que quedó arriba: <http://localhost:8082/actuator/health>. Con RabbitMQ en marcha, su
consola (<http://localhost:15672>) muestra las 10 colas, cada una con un consumidor cuando este
servicio está conectado.

**Conectarlo al gateway:** no hace falta ninguna URL fija — se encuentra vía Eureka. Solo hace falta
que `RABBITMQ_HOST`/`RABBITMQ_PORT` (de este servicio y del gateway) apunten al mismo broker. Si este
servicio está caído, las rutas `/api/customer/*` responden con un error.

Con Docker Compose (todo el ecosistema junto): ver la opción B del [README de la raíz](../README.md).

## Probarlo de forma individual

### 1. Directo al servicio (sin el gateway)

Ni gRPC ni RabbitMQ se pueden probar desde el navegador ni con `curl` sin herramientas adicionales.

**gRPC**, con [`grpcurl`](https://github.com/fullstorydev/grpcurl) (el servidor tiene reflexión
habilitada, así que no hace falta pasarle el `.proto`):

```bash
grpcurl -plaintext localhost:50051 list
grpcurl -plaintext -d '{"page":1,"limit":2}' localhost:50051 customer.CustomersService/List
grpcurl -plaintext -d '{"id":1}' localhost:50051 customer.AddressesService/FindOne
```

**RabbitMQ**, con cualquier cliente que soporte el patrón RPC de AMQP (`replyTo` + `correlationId`),
por ejemplo [`pika`](https://pika.readthedocs.io/) en Python, publicando en la cola que corresponda
(ver la tabla de *Colas de RabbitMQ*).

Y su salud: <http://localhost:8082/actuator/health>.

### 2. A través del gateway (este servicio, más el login)

1. Levanta `discovery-server`, la base de datos, RabbitMQ, este servicio, **`business-service`** y el
   gateway. `business-service` hace falta solo para el login/registro.
2. Inicia sesión y toma el token (ver *Primer login* en el [README de la raíz](../README.md)).
3. Prueba `GET /api/customer/customers`, `/addresses` (por gRPC) y `/cities`, `/countries` (por
   RabbitMQ), y sus `/{id}`.

## Estructura

```
src/main/
├── proto/customer.proto    # contrato gRPC (customer, address)
└── java/.../customerservice/
    ├── customer/   # entidad, repositorio, servicio, @GrpcService
    ├── address/
    ├── city/       # entidad, repositorio, servicio, @RabbitListener
    ├── country/
    └── shared/     # excepciones, PgErrors, @GrpcAdvice, configuración de RabbitMQ
```
