# spring-sakila-microservices

Ecosistema de microservicios en **Spring Boot** sobre la base de datos de ejemplo **Sakila** (versión
PostgreSQL), pensado para ver **distintos medios de comunicación entre servicios** funcionando juntos,
con descubrimiento de servicios vía **Eureka**. Un único punto de entrada (`api-gateway`, REST +
Swagger + JWT) recibe las peticiones y habla con cada microservicio en el protocolo que ese
microservicio usa, resolviendo su dirección en tiempo real a través de Eureka.

Hay dos formas de correrlo:

- **[Opción B — todo con Docker Compose](#opción-b--todo-con-docker-compose)** (la más corta: no
  necesitas instalar Java, Maven, PostgreSQL ni RabbitMQ, solo Docker).
- **[Opción A — cada servicio por separado](#opción-a--cada-servicio-por-separado-sin-docker-compose)**
  (sin Docker Compose: para trabajar sobre el código de un servicio, depurar, etc.).

```
                          ┌─────────────────┐
   cliente (REST/JSON) ──▶│   api-gateway   │  :8090  REST + Swagger + JWT
                          └────────┬────────┘
        ┌───────────────┬──────────┼──────────────┬─────────────────┐
        │ GraphQL       │ gRPC +   │ SOAP         │ STOMP           │
        ▼               ▼ RabbitMQ ▼              ▼  (WebSocket)
 business-service  customer-service  inventory-service  reporting-service
      :8081          :8082 / :50051       :8083              :8084
        └───────────────┴──────────┴──────────────┴─────────────────┘
                                   │
                         PostgreSQL (una sola base: sakila)

   todos los servicios (incluido api-gateway) se registran en:
                      discovery-server (Eureka)  :8761
```

| Servicio | Protocolo con el gateway | Puerto | Tablas de Sakila |
|---|---|---|---|
| [`discovery-server`](discovery-server/README.md) | — (registro Eureka) | `8761` | ninguna |
| [`api-gateway`](api-gateway/README.md) | REST + Swagger (entrada única) | `8090` | ninguna |
| [`business-service`](business-service/README.md) | GraphQL | `8081` | `staff`, `store`, `payment`, `rental` |
| [`customer-service`](customer-service/README.md) | gRPC (`customer`, `address`) + RabbitMQ (`city`, `country`) | `8082` (HTTP/health), `50051` (gRPC) | `customer`, `address`, `city`, `country` |
| [`inventory-service`](inventory-service/README.md) | SOAP | `8083` | `film`, `actor`, `category`, `language`, `inventory` |
| [`reporting-service`](reporting-service/README.md) | STOMP sobre WebSocket | `8084` | 7 vistas y funciones (solo lectura) |

Cada servicio tiene su propio README con el detalle de su contrato, sus tablas, su configuración y
cómo correrlo.

## Estructura

```
spring-sakila-microservices/
├── discovery-server/     # Eureka: registro de servicios
├── api-gateway/          # REST + Swagger + JWT; un paquete adaptador por microservicio
├── business-service/     # GraphQL
├── customer-service/     # gRPC + RabbitMQ
├── inventory-service/    # SOAP
├── reporting-service/    # STOMP (WebSocket)
├── db/                   # dump oficial de Sakila + el único ajuste al schema
└── docker-compose.yml    # levanta todo (base de datos, RabbitMQ, los 6 servicios)
```

## La base de datos

Todos los servicios usan **la misma base PostgreSQL** (`sakila`), con el **schema oficial de Sakila
sin modificar**: todas sus claves foráneas, restricciones, vistas y funciones quedan como vienen. La
única diferencia es una columna:

```sql
ALTER TABLE staff ALTER COLUMN password TYPE varchar(255);   -- db/business-service.sql
```

(`staff.password` es `varchar(40)` en Sakila, pensado para un hash SHA-1; un hash de BCrypt mide 60
caracteres.)

Como las claves foráneas siguen activas, un id que apunta a algo que no existe —incluso si ese algo
pertenece a otro microservicio— lo rechaza Postgres, y el servicio traduce el error: el cliente recibe
`400 INVALID_STORE_ID` ("Store 999 does not exist.") en vez de un error de SQL crudo. Borrar algo que
otros registros usan da `409 STILL_REFERENCED`, indicando qué tabla lo sigue usando.

| Archivo de `db/` | Qué es |
|---|---|
| `postgres-sakila-schema.sql` | schema oficial de Sakila para PostgreSQL |
| `postgres-sakila-insert-data.sql` | datos de ejemplo |
| `business-service.sql` | el ajuste del password (se corre después de los dos anteriores) |

## Requisitos

| Para… | Debe estar instalado |
|---|---|
| **Opción B (Docker Compose)** | **Docker** con Compose (`docker compose`), **en ejecución** |
| **Opción A (por separado)** | **Java 25** (JDK), **PostgreSQL** con un usuario y su contraseña, y **Docker** (solo para levantar RabbitMQ con un `docker run`) |

Cada servicio incluye el wrapper de Maven (`mvnw` / `mvnw.cmd`), así que no hace falta instalar Maven
aparte. Para comprobar lo demás: `docker --version`, `docker compose version`, `java --version` y
`psql --version`.

---

# Opción B — Todo con Docker Compose

Un único `docker-compose.yml` levanta la base de datos (PostgreSQL 18, cargada sola con `db/`),
RabbitMQ y los seis servicios (cada uno con su propio `Dockerfile`, build multietapa con Maven). Solo
necesitas **Docker en marcha**. **No hay que crear ni editar ningún archivo.**

## B.1 Levantar todo

```bash
docker compose up -d --build
docker compose ps          # esperar a que todos digan "healthy"
```

La primera vez construye las imágenes (compila cada servicio con Maven dentro del propio build) y
carga la base; tarda varios minutos. Para ver el arranque: `docker compose logs -f` (Ctrl+C para
salir; no apaga nada).

**Cómo está armado:**

- **Una red propia**, `spring-sakila-network`, que une a los 8 contenedores. Dentro de ella cada
  servicio se encuentra por el nombre de su servicio (`db`, `rabbitmq`, `discovery-server`,
  `business-service`...) y por su puerto interno.
- `discovery-server` es el registro Eureka: todos los demás servicios (incluido `api-gateway`) se
  registran en él al arrancar, y es así —no con URLs fijas— como el gateway encuentra a cada
  microservicio.
- **Cada servicio publica su puerto** para poder probarlo por separado desde tu máquina (tabla de
  abajo). Se publican solo en `127.0.0.1`: se pueden usar desde este equipo, pero no quedan abiertos a
  otros equipos de la red. Para abrirlos, quita el `127.0.0.1:` de `docker-compose.yml`.
- **La base de datos** usa el puerto **5434** del host (no el 5432) para no chocar con un PostgreSQL
  ya instalado en tu máquina. **RabbitMQ** usa **5673**/**15673** (no 5672/15672) por el mismo motivo.
  Dentro de la red siguen siendo `db:5432` y `rabbitmq:5672`.

**Valores por defecto** (para desarrollo; se pueden cambiar sin editar el compose):

| Qué | Valor por defecto |
|---|---|
| PostgreSQL | usuario `postgres`, contraseña `postgres`, base `sakila` |
| RabbitMQ | `guest` / `guest` |
| `JWT_SECRET` del gateway | una clave de desarrollo incluida en el compose |

Para cambiar alguno, define la variable al ejecutar o crea un archivo `.env` junto al compose:

```bash
DB_PASSWORD=otraclave docker compose up -d --build
```

(`DB_PASSWORD` solo se aplica cuando se crea el volumen de la base; ver *Comandos útiles* más abajo.)

## B.2 Qué queda corriendo

| Qué | Dónde (desde tu máquina) |
|---|---|
| Eureka (registro) | <http://localhost:8761> |
| Gateway (REST) | <http://localhost:8090/api> · Swagger <http://localhost:8090/api/swagger-ui/index.html> |
| `business-service` (GraphQL) | <http://localhost:8081/graphql> · GraphiQL <http://localhost:8081/graphiql> |
| `customer-service` (gRPC + RabbitMQ) | gRPC en `localhost:50051` · health <http://localhost:8082/actuator/health> |
| `inventory-service` (SOAP) | WSDL <http://localhost:8083/ws/inventory.wsdl> |
| `reporting-service` (STOMP) | `ws://localhost:8084/ws` |
| PostgreSQL | `localhost:5434` (usuario `postgres`, contraseña `postgres`, base `sakila`) |
| RabbitMQ | `localhost:5673` · consola <http://localhost:15673> (`guest` / `guest`) |

Cada microservicio se puede probar **directamente**, sin pasar por el gateway: ver el README de cada
uno.

## B.3 Primer login

No hace falta ninguna cuenta previa: el gateway expone un registro propio. Crea un empleado
(`staff`) y de una vez te devuelve un token:

```bash
curl -s -X POST http://localhost:8090/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ada","lastName":"Lovelace","addressId":1,"storeId":1,"username":"ada","password":"Admin1234"}'
# -> { "accessToken": "eyJ...", "staff": { "id": 1, "username": "ada" } }
```

Sigue en **[Iniciar sesión y probar](#iniciar-sesión-y-probar-la-api)**.

## B.4 Comandos útiles

```bash
docker compose ps                                  # estado de cada contenedor
docker compose logs -f api-gateway                 # logs de un servicio
docker compose up -d --build business-service      # reconstruir y reiniciar solo uno
docker compose stop customer-service               # apagar uno
docker compose up -d --no-deps api-gateway         # levantar el gateway sin arrancar los demás
docker compose down                                # apagar todo (los datos de la base se conservan)
docker compose down -v                             # apagar todo Y borrar la base (vuelve a cargarse limpia)
```

La base se carga **una sola vez**, la primera vez que arranca con el volumen vacío. Si cambias algo en
`db/` o quieres volver a los datos originales, usa `docker compose down -v` y vuelve a levantar.

---

# Opción A — Cada servicio por separado (sin Docker Compose)

Cada servicio corre en **su propia terminal** (abre una ventana o pestaña por servicio), en este
orden (los demás necesitan que `discovery-server` esté arriba para registrarse, aunque arrancan igual
si no lo está):

## A.1 Preparar la base de datos

Con tu PostgreSQL en marcha, carga la base **en este orden**:

```bash
psql -U postgres -h localhost -c "CREATE DATABASE sakila;"
psql -U postgres -h localhost -d sakila -f db/postgres-sakila-schema.sql
psql -U postgres -h localhost -d sakila -f db/postgres-sakila-insert-data.sql
psql -U postgres -h localhost -d sakila -f db/business-service.sql
```

Si tu PostgreSQL no usa el puerto 5432, agrega `-p <puerto>` a cada comando. Para empezar de cero:
`psql -U postgres -h localhost -c "DROP DATABASE sakila;"` y repetir.

## A.2 Levantar RabbitMQ (solo `customer-service`)

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management-alpine
```

Queda en `localhost:5672` con usuario y contraseña `guest` / `guest`, y su consola web en
<http://localhost:15672>.

## A.3 Levantar `discovery-server`

```bash
cd discovery-server
./mvnw spring-boot:run
```

Queda en <http://localhost:8761>: su panel muestra, a medida que los levantas, cada servicio
registrado.

## A.4 Levantar cada microservicio

Los cuatro microservicios (`business-service`, `customer-service`, `inventory-service`,
`reporting-service`) no necesitan ningún archivo de configuración: cada uno trae valores por defecto
que apuntan a `localhost` (base de datos en `5432`, Eureka en `8761`, RabbitMQ en `5672`). Si tu
PostgreSQL no usa esos valores, pásalos como variables de entorno. Desde cada carpeta:

```bash
cd business-service
./mvnw spring-boot:run
```

```bash
DB_PORT=5433 DB_PASSWORD=miClave ./mvnw spring-boot:run     # si tu Postgres usa otro puerto o clave
```

| Servicio | Puerto | Health | Otros |
|---|---|---|---|
| `business-service` | 8081 | <http://localhost:8081/actuator/health> | GraphQL: <http://localhost:8081/graphql> · GraphiQL: <http://localhost:8081/graphiql> |
| `customer-service` | 8082 y 50051 | <http://localhost:8082/actuator/health> | gRPC en `localhost:50051` |
| `inventory-service` | 8083 | <http://localhost:8083/actuator/health> | WSDL: <http://localhost:8083/ws/inventory.wsdl> |
| `reporting-service` | 8084 | <http://localhost:8084/actuator/health> | STOMP: `ws://localhost:8084/ws` |

Variables de entorno que se pueden ajustar en cualquiera de los cuatro (todas opcionales, con el
valor por defecto entre paréntesis): `PORT`, `DB_HOST` (`localhost`), `DB_PORT` (`5434`), `DB_USER`
(`postgres`), `DB_PASSWORD` (`postgres`), `DB_NAME` (`sakila`), `EUREKA_URL`
(`http://localhost:8761/eureka/`). `customer-service` además: `GRPC_PORT` (`50051`),
`RABBITMQ_HOST`/`RABBITMQ_PORT`/`RABBITMQ_USER`/`RABBITMQ_PASSWORD` (`localhost`/`5672`/`guest`/`guest`).

> `DB_PORT` por defecto es `5434` (el puerto donde el `docker-compose.yml` publica su propia base,
> para no chocar con un PostgreSQL ya instalado). Si usas tu propio PostgreSQL con la opción A, pásale
> `DB_PORT=5432` (o el que corresponda).

## A.5 Levantar el gateway

```bash
cd api-gateway
./mvnw spring-boot:run
```

El gateway encuentra cada microservicio a través de Eureka (`discovery-server`), así que no hace
falta ninguna variable para "apuntarlo" a un puerto fijo: basta con que el microservicio esté
registrado. Variables opcionales: `PORT` (`8090`), `EUREKA_URL`, `JWT_SECRET`,
`JWT_EXPIRATION_MINUTES` (`15`), y las de RabbitMQ (mismas por defecto que arriba, deben apuntar al
mismo broker que `customer-service`).

Con el gateway arriba:

- Swagger: <http://localhost:8090/api/swagger-ui/index.html>
- Actuator: <http://localhost:8090/api/actuator/health>

## A.6 Primer login

Igual que con Docker: registra un empleado nuevo.

```bash
curl -s -X POST http://localhost:8090/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ada","lastName":"Lovelace","addressId":1,"storeId":1,"username":"ada","password":"Admin1234"}'
```

Luego sigue en **[Iniciar sesión y probar](#iniciar-sesión-y-probar-la-api)**.

---

# Iniciar sesión y probar la API

(Vale para las dos opciones.) Todas las rutas del gateway piden un token JWT, excepto `/auth/login`,
`/auth/register` y las rutas de Actuator/Swagger. El token dura 15 minutos por defecto
(`JWT_EXPIRATION_MINUTES`); cuando expire, vuelve a iniciar sesión.

## Lo más simple: Swagger

1. Abre <http://localhost:8090/api/swagger-ui/index.html>.
2. En `POST /api/auth/register` (o `/auth/login` si ya registraste antes) → **Try it out**, completa
   el cuerpo y **Execute**. Copia el `accessToken` de la respuesta.
3. Arriba a la derecha, **Authorize**, pega el token, **Authorize** y cierra.
4. Ya puedes probar cualquier ruta con **Try it out**.

## Desde la terminal

```bash
# 1) registrar (o iniciar sesión si ya existe la cuenta) -> responde { "accessToken": "...", "staff": {...} }
curl -s -X POST http://localhost:8090/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ada","lastName":"Lovelace","addressId":1,"storeId":1,"username":"ada","password":"Admin1234"}'

# 2) copiar el token y probar un protocolo distinto en cada línea
T=<accessToken>
curl -s http://localhost:8090/api/business/stores         -H "Authorization: Bearer $T"   # GraphQL
curl -s http://localhost:8090/api/customer/customers/1    -H "Authorization: Bearer $T"   # gRPC
curl -s http://localhost:8090/api/customer/countries/20   -H "Authorization: Bearer $T"   # RabbitMQ
curl -s http://localhost:8090/api/inventory/films/1       -H "Authorization: Bearer $T"   # SOAP
curl -s http://localhost:8090/api/reporting/sales/by-store -H "Authorization: Bearer $T"  # STOMP
```

## Service discovery

<http://localhost:8761> (el panel de `discovery-server`) muestra, en vivo, qué servicios están
registrados y cuántas instancias de cada uno. Es la forma más directa de ver si un microservicio llegó
a registrarse (si no aparece ahí, el gateway tampoco lo va a encontrar).

## Formato de los errores

Todos los errores salen del gateway con la misma forma, sin importar el protocolo del microservicio
detrás:

```json
{ "statusCode": 400, "error": "INVALID_STORE_ID", "message": "Store 999 does not exist.", "path": "/api/customer/customers", "timestamp": "..." }
```

| `error` | HTTP | Cuándo |
|---|---|---|
| `<RECURSO>_NOT_FOUND` | 404 | el id pedido no existe |
| `INVALID_<RECURSO>_ID` | 400 | el cuerpo apunta a un registro que no existe (`INVALID_STORE_ID`, `INVALID_ADDRESS_ID`...) |
| `STILL_REFERENCED` | 409 | se intenta borrar algo que otros registros todavía usan |
| `*_ALREADY_EXISTS` | 409 | valor único repetido (p. ej. un `username` ya tomado) |
| `VALIDATION_ERROR` | 400 | el cuerpo de la petición no pasa las validaciones |
| `INVALID_TOKEN` | 401 | falta el JWT, es inválido o expiró |
| `<SERVICIO>_SERVICE_UNAVAILABLE` / `_TIMEOUT` / `_UNREACHABLE` | 503 / 504 / 500 | el microservicio no responde |

## Problemas frecuentes

- **`503`/`504` en una ruta del gateway**: ese microservicio no está registrado en Eureka (revisa
  <http://localhost:8761>) o tardó más de lo esperado en responder.
- **`401 INVALID_CREDENTIALS` al iniciar sesión**: usuario o contraseña incorrectos, o la cuenta no
  existe — regístrala primero con `/auth/register`.
- **`401 INVALID_TOKEN` en cualquier otra ruta**: falta el header `Authorization: Bearer <token>`, o el
  token expiró (dura 15 minutos): vuelve a iniciar sesión.
- **Un microservicio no conecta a la base** (`Connection refused`, `password authentication failed`):
  revisa `DB_HOST`/`DB_PORT`/`DB_PASSWORD` si lo corres por separado. Con la base de Docker, desde tu
  máquina es `localhost:5434`, usuario `postgres`, contraseña `postgres` (salvo que hayas definido
  otra).
- **Puerto ocupado** (`Address already in use`, `port is already allocated`): cada servicio usa un
  puerto fijo (8081–8084, 8090, 8761, 50051, 5434, 5673, 15673). Dos copias a la vez chocan, por
  ejemplo un servicio por separado y el mismo dentro de Docker.
- **Quiero empezar de cero con Docker**: `docker compose down -v` y luego `docker compose up -d --build`.
- **Chip Apple Silicon / ARM**: las imágenes que usa el compose (`eclipse-temurin`, `postgres`,
  `rabbitmq`) tienen versión para ARM; no hace falta ningún ajuste.
