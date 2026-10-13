# inventory-service

Microservicio de **inventario** del ecosistema [`spring-sakila-microservices`](../README.md): el
catálogo de películas (`film`, `actor`, `category`, `language`) y dónde hay copias disponibles
(`inventory`).

## Resumen

| | |
|---|---|
| **Protocolo** | **SOAP** (Spring-WS, contract-first) |
| **Puerto** | `8083` — SOAP en `/ws`, WSDL en `/ws/inventory.wsdl`, salud en `/actuator/health` |
| **Lo consume** | `api-gateway` (rutas `/api/inventory/*`) |
| **Base de datos** | PostgreSQL `sakila`, compartida con los demás servicios |
| **Tablas** | `film`, `actor`, `category`, `language`, `inventory` (lectura y escritura) |
| **Stack** | Spring Boot, Spring Web Services, JAXB, Spring Data JPA, Eureka client |

## Tablas que usa

| Tabla | Qué guarda | Referencias |
|---|---|---|
| `film` | películas | `language_id` → `language` |
| `actor` | actores | — |
| `category` | categorías de películas | — |
| `language` | idiomas | — |
| `inventory` | copias físicas de una película en una tienda | `film_id` → `film` · `store_id` → `store` *(business-service)* |

Las tablas de unión puras (`film_actor`, `film_category`) no tienen CRUD propio: se resuelven como
colecciones del lado de `Film` (`actorIds`, `categoryIds`). Las claves foráneas de la base siguen
activas, incluida la que apunta a `store` (de otro microservicio). Otros servicios también apuntan a
estas tablas (`rental` → `inventory`): por eso borrar una copia con alquileres da `STILL_REFERENCED`.

## Contrato (contract-first)

El contrato es un **XSD escrito a mano** (`src/main/resources/xsd/inventory.xsd`), con un mensaje de
petición y uno de respuesta por operación. A partir de él:

- **JAXB** (`jaxb2-maven-plugin`, fase `generate-sources`) genera las clases Java de cada mensaje —
  nunca se escriben a mano.
- El **WSDL se genera solo**, a partir del mismo XSD (`DefaultWsdl11Definition`): no hay un WSDL
  escrito aparte que se pueda desincronizar del contrato real.
- Cada operación es un método `@Endpoint` con `@PayloadRoot(namespace, localPart)`: Spring-WS despacha
  por el nombre del elemento raíz del cuerpo, sin necesidad de un header `SOAPAction`.

25 operaciones, 5 por recurso (`Film`, `Actor`, `Category`, `Language`, `Inventory`):

| Operación | Qué hace |
|---|---|
| `List{Recurso}s(page?, limit?)` | lista paginada |
| `Get{Recurso}(id)` | uno por id |
| `Create{Recurso}(...)` | alta |
| `Update{Recurso}(id, ...)` | edición parcial |
| `Delete{Recurso}(id)` | baja |

(La lista de categorías es `ListCategories` y la de inventario `ListInventories`.)

Ejemplo con `curl` (sin header `SOAPAction`: Spring-WS no lo necesita para despachar):

```bash
curl -s -X POST http://localhost:8083/ws \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://inventory.sakila.ucb.com/schema">
  <soap:Body>
    <tns:ListFilmsRequest><tns:page>1</tns:page><tns:limit>5</tns:limit></tns:ListFilmsRequest>
  </soap:Body>
</soap:Envelope>'
```

Este servicio **no pide autenticación** por sí mismo (la valida el gateway); no lo expongas
directamente fuera de la red del ecosistema.

### Cómo llegan sus rutas por el gateway

| Ruta REST | Tabla |
|---|---|
| `/api/inventory/films` | `film` |
| `/api/inventory/actors` | `actor` |
| `/api/inventory/categories` | `category` |
| `/api/inventory/languages` | `language` |
| `/api/inventory/items` | `inventory` |

Todas con `GET` (lista paginada) y `POST`, y `GET`, `PATCH`/actualización y `DELETE` en `/{id}` según
el recurso (ver el detalle en Swagger del gateway).

## Errores

Los errores salen como un **SOAP Fault**; el código de dominio va en el texto de la razón, armado por
un `AbstractSoapFaultDefinitionExceptionResolver` propio:

```xml
<SOAP-ENV:Fault>
  <faultcode>SOAP-ENV:Server</faultcode>
  <faultstring xml:lang="en">FILM_NOT_FOUND: Film not found.</faultstring>
</SOAP-ENV:Fault>
```

SOAP no transporta un status HTTP, así que el gateway lo deduce del código:

| Código | HTTP | Cuándo |
|---|---|---|
| `FILM_NOT_FOUND`, `ACTOR_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `LANGUAGE_NOT_FOUND`, `INVENTORY_NOT_FOUND` | 404 | el id no existe |
| `INVALID_LANGUAGE_ID`, `INVALID_FILM_ID` | 400 | el cuerpo apunta a un registro de este servicio que no existe (se valida antes de escribir) |
| `STILL_REFERENCED` | 409 | se borra algo que otros registros usan; el mensaje nombra la tabla |

## Configuración

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | `8083` | puerto HTTP (también el de `/ws`) |
| `DB_HOST` / `DB_PORT` | `localhost` / `5434` | dónde está PostgreSQL |
| `DB_USER` / `DB_PASSWORD` / `DB_NAME` | `postgres` / `postgres` / `sakila` | credenciales |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | registro de Eureka |

## Correrlo por separado (sin Docker Compose)

Requiere Java 25 y PostgreSQL con `sakila` cargada (ver *Preparar la base de datos* en el
[README de la raíz](../README.md)).

```bash
./mvnw spring-boot:run
```

Comprobar que quedó arriba: <http://localhost:8083/actuator/health>. El WSDL está en
<http://localhost:8083/ws/inventory.wsdl>.

**Conectarlo al gateway:** no hace falta ninguna URL fija — se encuentra vía Eureka. Si este servicio
está caído, las rutas `/api/inventory/*` responden con un error.

Con Docker Compose (todo el ecosistema junto): ver la opción B del [README de la raíz](../README.md).

## Probarlo de forma individual

### 1. Directo al servicio (sin el gateway)

- **WSDL, en el navegador:** <http://localhost:8083/ws/inventory.wsdl> (lista todas las operaciones y
  sus tipos).
- **`curl`:** el ejemplo de arriba (sección *Contrato*).
- Herramientas como SoapUI o Postman también funcionan, importando el WSDL.

Y su salud: <http://localhost:8083/actuator/health>.

### 2. A través del gateway (este servicio, más el login)

1. Levanta `discovery-server`, la base de datos, este servicio, **`business-service`** y el gateway.
   `business-service` hace falta solo para el login/registro.
2. Inicia sesión y toma el token (ver *Primer login* en el [README de la raíz](../README.md)).
3. Prueba `GET /api/inventory/films`, `/actors`, `/categories`, `/languages`, `/items` (y sus `/{id}`).

## Estructura

```
src/main/
├── resources/xsd/inventory.xsd   # el contrato: mensajes de petición/respuesta de las 25 operaciones
└── java/.../inventoryservice/
    ├── film/      # entidad, repositorio, servicio, @Endpoint
    ├── actor/
    ├── category/
    ├── language/
    ├── inventory/
    ├── ws/        # configuración de Spring-WS (servlet, WSDL autogenerado) y el resolutor de faults
    └── shared/    # PageResult, excepciones base
```
