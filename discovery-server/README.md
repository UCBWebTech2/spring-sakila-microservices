# discovery-server

Registro de servicios del ecosistema [`spring-sakila-microservices`](../README.md), con **Eureka**.
No tiene lógica de negocio ni base de datos: cada microservicio (incluido `api-gateway`) se registra
en él al arrancar, y así es como el gateway encuentra, en tiempo real, la dirección de cada uno —en
vez de depender de URLs fijas por configuración.

## Resumen

| | |
|---|---|
| **Protocolo** | HTTP (panel + API de registro de Eureka) |
| **Puerto** | `8761` |
| **Lo consumen** | todos los demás servicios de este ecosistema |
| **Base de datos** | ninguna |
| **Stack** | Spring Boot, Spring Cloud Netflix Eureka Server |

## Modo standalone

Corre en **modo de un solo nodo**: no se registra a sí mismo (`register-with-eureka: false`) ni
consulta a otro registro (`fetch-registry: false`). Para este ecosistema —un puñado de servicios en
una sola máquina— es la configuración más simple; en un despliegue con varios nodos de Eureka, cada
uno normalmente se registra en los demás para replicar el registro.

La auto-preservación de Eureka (`enable-self-preservation`) está desactivada: en un solo nodo, solo
agrega advertencias en el log sin aportar nada (está pensada para que un registro no expulse en masa a
sus clientes cuando *el propio registro* pierde conectividad con una porción grande de ellos al mismo
tiempo, algo que no puede pasar con un solo nodo).

## Panel

<http://localhost:8761> muestra, en vivo, cada aplicación registrada (por su `spring.application.name`
en mayúsculas) y cuántas instancias tiene. Es la forma más directa de confirmar si un microservicio
llegó a registrarse: si no aparece ahí, el gateway tampoco lo va a encontrar.

## Cómo se registra un cliente

Cualquier servicio con `spring-cloud-starter-netflix-eureka-client` en su `pom.xml` y

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

se registra solo al arrancar. `prefer-ip-address: true` hace que se registre por IP en vez de por
nombre de host, necesario para que el `api-gateway` pueda resolver la dirección real del contenedor
dentro de la red de Docker.

## Configuración

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | `8761` | puerto HTTP (también el del panel) |

No necesita conexión a base de datos ni a ningún otro servicio: siempre puede ser el primero en
arrancar.

## Correrlo por separado (sin Docker Compose)

No requiere nada más que Java 25:

```bash
./mvnw spring-boot:run
```

Comprobar que quedó arriba: abre <http://localhost:8761> en el navegador.

Con Docker Compose (todo el ecosistema junto): ver la opción B del [README de la raíz](../README.md).

## Estructura

```
src/main/java/.../discoveryserver/
└── DiscoveryServerApplication.java   # @EnableEurekaServer
```

Toda la configuración está en `src/main/resources/application.yml`; no hay código propio más allá del
punto de entrada.
