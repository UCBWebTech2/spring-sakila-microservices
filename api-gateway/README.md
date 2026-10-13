# api-gateway

Punto de entrada único del ecosistema [`spring-sakila-microservices`](../README.md). Expone una API
**REST con Swagger** y autenticación **JWT**, y por detrás habla con cada microservicio en el
protocolo que ese microservicio usa, resolviendo su dirección en tiempo real a través de **Eureka**
(nunca con una URL fija). No tiene base de datos ni lógica de negocio propia: cada microservicio tiene
un paquete adaptador que traduce REST ↔ su protocolo.

## Resumen

| | |
|---|---|
| **Protocolo hacia afuera** | REST (JSON) + Swagger / OpenAPI |
| **Protocolos hacia los microservicios** | GraphQL, gRPC, RabbitMQ, SOAP, STOMP (WebSocket) |
| **Puerto** | `8090`, con prefijo `/api` en todas las rutas |
| **Base de datos** | ninguna |
| **Stack** | Spring Boot, Spring Security (JWT nativo, sin OAuth2 externo), Spring Cloud LoadBalancer + Eureka client, springdoc-openapi |

## Microservicios que integra

| Paquete (`.../apigateway/`) | Microservicio | Protocolo | Rutas REST |
|---|---|---|---|
| `business/` | `business-service` | GraphQL | `/api/business/staffs`, `/stores`, `/rentals`, `/payments` |
| `customer/` | `customer-service` | gRPC | `/api/customer/customers`, `/addresses` |
| `customer/` | `customer-service` | RabbitMQ | `/api/customer/cities`, `/countries` |
| `inventory/` | `inventory-service` | SOAP | `/api/inventory/films`, `/actors`, `/categories`, `/languages`, `/items` |
| `reporting/` | `reporting-service` | STOMP | `/api/reporting/...` (13 reportes de solo lectura) |
| `auth/` | `business-service` (login/registro) | GraphQL | `POST /api/auth/login`, `POST /api/auth/register` |

Las rutas de `business`, `customer` e `inventory` tienen CRUD (`GET` lista paginada con
`?page=&limit=`, `GET /{id}`, `POST`, `PATCH /{id}`, `DELETE /{id}`, salvo algunas excepciones puntuales
como `payments` sin `PATCH`); `reporting` es solo lectura. El detalle de cada ruta, con sus cuerpos y
respuestas, está en Swagger: <http://localhost:8090/api/swagger-ui/index.html>.

## Autenticación

No hay usuarios "del gateway": la cuenta es un `staff` de `business-service`, y es ese servicio quien
compara el hash de la contraseña (`staffLogin`). Si son válidas, el gateway firma su propio JWT
(HS256, con `JwtEncoder`/`JwtDecoder` nativos de Spring Security — sin pasar por un flujo OAuth2 ni un
Authorization Server aparte). Hay un solo token de acceso, sin refresh token ni roles.

`POST /api/auth/register` crea la cuenta (llama a `createStaff` en `business-service`, que guarda el
password con BCrypt) y devuelve el mismo token que un login, de una sola vez — no hace falta ninguna
cuenta precargada para empezar a usar la API.

En Swagger: `POST /api/auth/register` → *Try it out* → completa el cuerpo y *Execute* → copia el
`accessToken` → botón **Authorize** → pégalo. Desde la terminal:

```bash
curl -s -X POST http://localhost:8090/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ada","lastName":"Lovelace","addressId":1,"storeId":1,"username":"ada","password":"Admin1234"}'
# -> { "accessToken": "eyJ...", "staff": { "id": 1, "username": "ada" } }
curl -s http://localhost:8090/api/business/stores -H "Authorization: Bearer <accessToken>"
```

Todas las rutas piden el token excepto `POST /api/auth/login`, `POST /api/auth/register`, Actuator
(`/api/actuator/**`) y Swagger/OpenAPI.

## Service discovery

El gateway no tiene ninguna URL fija hacia los microservicios: se registra en `discovery-server`
(Eureka) como cliente y resuelve cada uno por su nombre de aplicación en el momento de la llamada. Para
GraphQL (`business-service`) usa un `RestClient.Builder` marcado `@LoadBalanced` (Spring Cloud resuelve
el host automáticamente); para SOAP, gRPC y STOMP —protocolos que no pasan por el cliente HTTP estándar
de Spring— resuelve el host a mano con `LoadBalancerClient` antes de abrir la conexión.

<http://localhost:8761> (el panel de `discovery-server`) muestra en vivo qué microservicios están
registrados.

## Errores

Cada protocolo transporta el error a su manera y el gateway lo convierte siempre en la misma forma:
`{ statusCode, error, message, path, timestamp }`.

| Protocolo | Cómo llega el error del microservicio |
|---|---|
| GraphQL | `errors[0].extensions` (`code`, `statusCode`) |
| SOAP | `Fault` con el texto `CODIGO: mensaje` |
| gRPC | status gRPC + el mensaje `CODIGO: texto` |
| RabbitMQ | el objeto `{ statusCode, error, message }`, como la respuesta misma |
| STOMP | mensaje en `/user/queue/errors`, con `code`, `message` y la operación que falló |

Vocabulario común: `<RECURSO>_NOT_FOUND` → 404, `INVALID_<RECURSO>_ID` → 400 (el cuerpo apunta a un
registro que no existe), `STILL_REFERENCED` → 409 (se intenta borrar algo que otros registros usan),
`INVALID_TOKEN` → 401, `VALIDATION_ERROR` → 400. Si un microservicio no responde o no está registrado
en Eureka → un error `503`/`504` con un código propio (p. ej. `CUSTOMER_SERVICE_UNREACHABLE`,
`REPORTING_SERVICE_TIMEOUT`).

## Configuración

No hace falta ningún archivo: todo tiene un valor por defecto pensado para correr en `localhost`,
sobreescribible con variables de entorno.

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | `8090` | puerto del gateway |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | registro de Eureka |
| `JWT_SECRET` | *(clave de desarrollo incluida)* | clave con la que se firman los tokens — cambiarla antes de exponer esto fuera de tu máquina |
| `JWT_EXPIRATION_MINUTES` | `15` | duración del token |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | mismo broker que usa `customer-service` |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `guest` / `guest` | credenciales del broker |

## Correrlo por separado (sin Docker Compose)

Requiere Java 25. Los microservicios que quieras usar tienen que estar registrados en Eureka (el
gateway arranca igual aunque no lo estén; sus rutas solo responden con error hasta que aparezcan).

```bash
./mvnw spring-boot:run
```

Queda en <http://localhost:8090/api>: Swagger en `/api/swagger-ui/index.html`, Actuator en
`/api/actuator/health`.

Guía completa (base de datos, RabbitMQ, cada servicio, primer registro):
[README de la raíz](../README.md). Para levantar todo con Docker, ver la opción B de ese README.

## Probarlo de forma individual

### 1. El gateway solo, sin ningún microservicio

No necesita a los microservicios para arrancar (sí necesita a `discovery-server`, para registrarse).
Con solo el gateway levantado:

- Swagger abre normal: <http://localhost:8090/api/swagger-ui/index.html>.
- `GET /api/actuator/health` responde `200`.
- Cualquier ruta que dependa de un microservicio (incluido el login/registro) responde con un error,
  porque no hay ninguna instancia de ese servicio en Eureka para resolver.

### 2. El gateway con un solo microservicio

Levanta `discovery-server`, el gateway, **`business-service`** (emite el token: todas las rutas lo
piden) y **un** microservicio más, con la base de datos (`customer-service` además necesita
RabbitMQ). Las rutas de ese microservicio funcionan; las de los demás responden con error.

| Levantas (además de `business-service`) | Rutas que funcionan |
|---|---|
| `customer-service` (+ RabbitMQ) | `/api/customer/customers`, `/addresses` (gRPC) y `/cities`, `/countries` (RabbitMQ) |
| `inventory-service` | `/api/inventory/*` |
| `reporting-service` | `/api/reporting/*` |

Con solo `business-service` funcionan `/api/business/*` y el login/registro. Cada microservicio tiene
en su README cómo probarlo directo, sin gateway, y las rutas del gateway que le corresponden.

## Estructura

```
src/main/java/.../apigateway/
├── auth/        # login + registro JWT, configuración de Spring Security — sin tabla propia
├── business/    # adaptador GraphQL → REST (staffs, stores, rentals, payments)
├── customer/    # adaptador gRPC + RabbitMQ → REST (incluye proto/customer.proto, propio)
├── inventory/   # adaptador SOAP → REST (incluye xsd/inventory.xsd, propio)
├── reporting/   # adaptador STOMP → REST
└── shared/      # excepción y manejador de errores global, cliente GraphQL genérico, configuración de RestClient
```

`customer/` e `inventory/` generan su propio cliente (clases JAXB / gRPC) a partir del mismo contrato
que publica el microservicio correspondiente, pero sin depender de su código: el gateway solo conoce
el `.proto` / `.xsd`, nunca una clase Java del otro servicio.
