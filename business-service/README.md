# business-service

Microservicio de **negocio** del ecosistema [`spring-sakila-microservices`](../README.md): quién
trabaja en qué tienda (`staff`, `store`), qué se alquiló (`rental`) y qué se cobró (`payment`).
También es quien **verifica las credenciales** del login/registro del gateway.

## Resumen

| | |
|---|---|
| **Protocolo** | **GraphQL** (Spring for GraphQL, code-first) |
| **Puerto** | `8081` — GraphQL en `/graphql`, GraphiQL en `/graphiql`, salud en `/actuator/health` |
| **Lo consume** | `api-gateway` (rutas `/api/business/*` y `/api/auth/login`, `/api/auth/register`) |
| **Base de datos** | PostgreSQL `sakila`, compartida con los demás servicios |
| **Tablas** | `staff`, `store`, `payment`, `rental` (lectura y escritura) |
| **Stack** | Spring Boot, Spring for GraphQL, Spring Data JPA, Spring Security Crypto (BCrypt), Eureka client |

## Tablas que usa

| Tabla | Qué guarda | Referencias |
|---|---|---|
| `staff` | empleados (con su `username` y la contraseña en BCrypt) | `store_id` → `store` · `address_id` → `address` *(customer-service)* |
| `store` | tiendas | `manager_staff_id` → `staff` · `address_id` → `address` *(customer-service)* |
| `rental` | alquileres | `staff_id` → `staff` · `customer_id` → `customer` *(customer-service)* · `inventory_id` → `inventory` *(inventory-service)* |
| `payment` | pagos | `staff_id` → `staff` · `rental_id` → `rental` · `customer_id` → `customer` *(customer-service)* |

Las referencias a tablas de otros microservicios (`address`, `customer`, `inventory`) se declaran
como columnas simples (sin relaciones JPA). Aun así las **claves foráneas de la base siguen activas**:
ver *Errores*.

Notas de las tablas:

- **`staff.password`** se guarda con **BCrypt** (`PasswordEncoder` de Spring Security), por eso la
  columna se ensanchó a `varchar(255)` (el único cambio al schema de Sakila; ver
  `../db/business-service.sql`). No hace falta ningún paso adicional para empezar a usar el sistema:
  simplemente se registra una cuenta nueva con `POST /api/auth/register` en el gateway, que llama a
  `createStaff` por acá.
- **`payment.payment_id`** usa una secuencia explícita (no el `IDENTITY` por defecto de JPA): la tabla
  trae partición por mes en el schema de Sakila (`payment_p2007_01`…`_06`) con reglas de inserción que
  rechazan el `INSERT ... RETURNING` que `IDENTITY` necesita.
- Borrar un `rental` deja en `NULL` el `rental_id` de sus pagos (así está definido en Sakila).

## API (GraphQL)

Endpoint `http://localhost:8081/graphql`, con **GraphiQL** en `http://localhost:8081/graphiql`. El
schema está escrito a mano en `src/main/resources/graphql/*.graphqls` (code-first: cada
`@Controller` resuelve los campos con `@QueryMapping`/`@MutationMapping`/`@BatchMapping`).

| Tipo | Operación | Qué hace |
|---|---|---|
| Query | `staffs(page, limit)` · `staff(id)` | lista paginada / un empleado |
| Mutation | `createStaff(input)` · `updateStaff(id, input)` · `removeStaff(id)` | CRUD de empleados |
| Mutation | `staffLogin(input: { username, password })` | verifica credenciales y devuelve el empleado; el hash nunca sale del servicio |
| Query | `stores(page, limit)` · `store(id)` | tiendas |
| Mutation | `createStore(input)` · `updateStore(id, input)` · `removeStore(id)` | CRUD de tiendas |
| Query | `rentals(page, limit)` · `rental(id)` | alquileres |
| Mutation | `createRental(input)` · `updateRental(id, input)` · `removeRental(id)` | CRUD de alquileres (`updateRental` con `returnDate` registra la devolución) |
| Query | `payments(page, limit)` · `payment(id)` | pagos |
| Mutation | `createPayment(input)` · `removePayment(id)` | alta y baja de pagos (no tiene edición) |

`Staff.store`, `Store.manager`, `Rental.staff`, `Payment.staff` y `Payment.rental` son relaciones
anidadas; cada una se resuelve con un `@BatchMapping` (una sola consulta para todos los elementos de la
respuesta, no una por cada uno — evita el problema N+1 sin necesitar una librería aparte).

Este servicio **no pide autenticación** por sí mismo (la valida el gateway); no lo expongas
directamente fuera de la red del ecosistema.

### Cómo llegan sus rutas por el gateway

`GET/POST /api/business/staffs`, `/stores`, `/rentals`, `/payments` (más `/{id}` con `GET`, `PATCH` y
`DELETE`, salvo `payments` que no tiene `PATCH`), y `POST /api/auth/login` / `POST /api/auth/register`.

## Errores

Salen como un error de GraphQL; el código y el status HTTP viajan en `errors[0].extensions` (`code`,
`statusCode`), que un `DataFetcherExceptionResolverAdapter` arma a partir de cada excepción de
dominio, y que el gateway convierte en la respuesta REST.

| Código | HTTP | Cuándo |
|---|---|---|
| `STAFF_NOT_FOUND`, `STORE_NOT_FOUND`, `RENTAL_NOT_FOUND`, `PAYMENT_NOT_FOUND` | 404 | el id no existe |
| `INVALID_ADDRESS_ID`, `INVALID_CUSTOMER_ID`, `INVALID_INVENTORY_ID` | 400 | el cuerpo apunta a un registro de **otro** microservicio que no existe: lo detecta Postgres por la clave foránea |
| `STILL_REFERENCED` | 409 | se borra algo que otros registros usan; el mensaje nombra la tabla |
| `STAFF_ALREADY_EXISTS` | 409 | `username` repetido |
| `INVALID_CREDENTIALS` | 401 | usuario o contraseña incorrectos (login) |

Las claves foráneas del schema original siguen vigentes, así que un id inexistente de otro
microservicio lo rechaza Postgres; un componente propio (`PgErrors`) lee el detalle del error JDBC y
arma un código como `INVALID_CUSTOMER_ID` ("Customer 999 does not exist.") en vez de dejar pasar un
error de SQL crudo. Esta traducción corre en el `create`/`update`/`remove` de cada entidad.

## Configuración

No hace falta ningún archivo: todo tiene un valor por defecto pensado para correr en `localhost`,
sobreescribible con variables de entorno.

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | `8081` | puerto HTTP |
| `DB_HOST` / `DB_PORT` | `localhost` / `5434` | dónde está PostgreSQL (`5434` es la del `docker-compose.yml`; en una instalación local suele ser `5432`) |
| `DB_USER` / `DB_PASSWORD` / `DB_NAME` | `postgres` / `postgres` / `sakila` | credenciales |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | registro de Eureka |

## Correrlo por separado (sin Docker Compose)

Requiere Java 25 y PostgreSQL con `sakila` cargada (schema, datos y `../db/business-service.sql`; ver
*Preparar la base de datos* en el [README de la raíz](../README.md)).

```bash
./mvnw spring-boot:run
```

Comprobar que quedó arriba: <http://localhost:8081/actuator/health>. GraphiQL está en
<http://localhost:8081/graphiql>.

**Conectarlo al gateway:** no hace falta ninguna URL fija — el gateway lo encuentra a través de
`discovery-server` (Eureka) por su nombre de aplicación (`business-service`). Si este servicio está
caído, el login/registro y las rutas `/api/business/*` responden con un error `503`/`500`.

Con Docker Compose (todo el ecosistema junto): ver la opción B del [README de la raíz](../README.md).

## Probarlo de forma individual

### 1. Directo al servicio (sin el gateway)

Con el servicio corriendo (por separado o en Docker), **GraphiQL** es lo más simple: abre
<http://localhost:8081/graphiql> y ejecuta, por ejemplo:

```graphql
{ stores(page: 1, limit: 2) { data { id addressId } meta { total } } }
```

O con `curl`:

```bash
curl -s -X POST http://localhost:8081/graphql -H 'Content-Type: application/json' \
  -d '{"query":"{ stores(page:1,limit:2){ data { id } meta { total } } }"}'
```

Y su salud: <http://localhost:8081/actuator/health>.

### 2. A través del gateway, con solo este servicio arriba

1. Levanta `discovery-server`, la base de datos, este servicio y el gateway. Nada más.
2. Como este servicio **es** el del login/registro, puedes crear una cuenta normalmente (ver *Primer
   login* en el [README de la raíz](../README.md)) y usar el token.
3. Prueba `GET /api/business/stores`, `/staffs`, `/rentals`, `/payments` (y sus `/{id}`).

## Estructura

```
src/main/java/.../businessservice/
├── staff/      # entidad, repositorio, servicio, controller GraphQL, excepciones
├── store/
├── rental/
├── payment/
└── shared/     # excepción base, PgErrors, PageInfo/PageResult, el resolutor de errores GraphQL
```

Paquete por funcionalidad (una carpeta = un recurso), no por capa técnica. El schema GraphQL vive en
`src/main/resources/graphql/` (un archivo por recurso, que `extend`-ea `Query`/`Mutation` en vez de
redeclararlos).
