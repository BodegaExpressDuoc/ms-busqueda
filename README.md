# ms-busqueda

Microservicio de búsqueda de artículos del sistema BodegaExpress. Expone una API
REST para buscar artículos por nombre, obteniendo los datos desde ms-articulos.

## Tecnologías

- Java 17
- Spring Boot 4.1.1 (Spring Web MVC, RestClient)
- Maven (mediante Maven Wrapper)
- Docker y GitHub Actions (despliegue en AWS)

## Requisitos previos

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | 17 o superior | La variable `JAVA_HOME` debe apuntar al JDK |
| Git | Cualquiera reciente | Para clonar el repositorio |
| ms-articulos | — | En ejecución, para que las búsquedas devuelvan resultados |
| Postman | Opcional | Para probar la API manualmente |

No es necesario instalar Maven: el repositorio incluye Maven Wrapper (`mvnw` / `mvnw.cmd`).
Este servicio no usa base de datos propia.

## Instalación y ejecución local

Los comandos se muestran para Windows (PowerShell). En Linux o macOS, reemplazar
`.\mvnw.cmd` por `./mvnw`.

### 1. Clonar el repositorio

```powershell
git clone https://github.com/BodegaExpressDuoc/ms-busqueda.git
cd ms-busqueda
```

### 2. Configurar las variables de entorno (opcional)

Todas las variables tienen un valor por defecto adecuado para ejecución local:

| Variable | Obligatoria | Valor por defecto | Descripción |
|---|---|---|---|
| `BUSQUEDA_ADDRESS` | No | `127.0.0.1` | Dirección de red en la que escucha el servicio |
| `BUSQUEDA_PORT` | No | `8083` | Puerto HTTP del servicio |
| `ARTICULOS_BASE_URL` | No | `http://localhost:8082` | URL base de ms-articulos |
| `GATEWAY_SECRET` | No | vacío | Secreto que el AWS API Gateway envía en el header `X-Gateway-Secret`. Si está definido, se rechazan con 403 las peticiones que no lo traen, y se envía el mismo header en las llamadas a ms-articulos. Vacío = no se exige |

Para cambiarlas, definirlas en la terminal antes de ejecutar. Por ejemplo, en PowerShell:

```powershell
$env:ARTICULOS_BASE_URL = "http://localhost:9082"
```

### 3. Compilar, probar y empaquetar

```powershell
.\mvnw.cmd clean install
```

Ejecuta las pruebas y genera el archivo `target/ms-busqueda.jar`.
Las pruebas simulan las respuestas de ms-articulos, por lo que no requieren otros
servicios en ejecución.

### 4. Ejecutar

Iniciar primero ms-articulos. Luego, con Maven:

```powershell
.\mvnw.cmd spring-boot:run
```

O con el `.jar` generado:

```powershell
java -jar target/ms-busqueda.jar
```

El servicio queda disponible en `http://localhost:8083/api/v1/busqueda`.
Para detenerlo, usar `Ctrl+C`.

## Estructura del proyecto

```text
src/main/java/cl/duoc/bodegaexpress/busqueda/
├── MsBusquedaApplication.java   Punto de entrada de la aplicación
├── controller/                  Endpoints REST
├── service/                     Lógica de búsqueda
├── config/                      Cliente HTTP hacia ms-articulos (envía X-Gateway-Secret)
├── dto/                         Objetos de respuesta
└── seguridad/                   FiltroGateway (solo se entra por el API Gateway)
```

## API

URL base: `http://localhost:8083/api/v1/busqueda`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/v1/busqueda?nombre={texto}` | Busca artículos cuyo nombre contenga el texto | `200` |

- La búsqueda es parcial y no distingue mayúsculas de minúsculas.
- Se ignoran los espacios al inicio y al final del texto.
- Si no hay coincidencias, devuelve una lista vacía (`[]`).

Respuesta de ejemplo:

```json
[
  {
    "id": 1,
    "nombre": "Caja",
    "descripcion": "Caja mediana",
    "precio": 2500.50,
    "stock": 10
  }
]
```

| Código | Causa |
|---|---|
| `400` | Parámetro `nombre` ausente, vacío o con más de 150 caracteres |
| `503` | ms-articulos no responde o devuelve un error |

El tiempo máximo de espera hacia ms-articulos es de 3 segundos para conectar y
5 segundos para leer la respuesta.

### Formato de errores

Los errores se devuelven en formato
[ProblemDetail (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457), con el
código HTTP y un mensaje descriptivo:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "El nombre debe contener entre 1 y 150 caracteres",
  "instance": "/api/v1/busqueda"
}
```

## Pruebas manuales

1. Iniciar ms-articulos y crear al menos un artículo.
2. Iniciar este servicio (paso 4).
3. Enviar GET `http://localhost:8083/api/v1/busqueda?nombre=caja` y verificar los resultados.
4. Enviar GET `http://localhost:8083/api/v1/busqueda?nombre=` para obtener `400`.
5. Detener ms-articulos y repetir el paso 3 para obtener `503`.

## Pruebas con Postman

El repositorio incluye la colección
[`postman/ms-busqueda.postman_collection.json`](postman/ms-busqueda.postman_collection.json).

1. En Postman, seleccionar **File → Import** y elegir el archivo de la colección.
2. Iniciar el servicio, junto con ms-articulos (paso 4).
3. Abrir la colección, seleccionar **Run** y ejecutar las peticiones en orden.

Cada petición incluye pruebas automáticas que verifican el código HTTP y el
contenido de la respuesta. La variable `baseUrl` de la colección define la URL
del servicio. Para comprobar el error `503`, detener ms-articulos y repetir la
petición 1.

## Docker

El `Dockerfile` es multi-stage:

1. **Compilación:** `maven:3.9.9-eclipse-temurin-17` descarga las dependencias
   (en una capa que queda en caché) y genera el jar.
2. **Ejecución:** `eclipse-temurin:17-jre-alpine`, solo con el JRE y el jar, y
   con un usuario sin privilegios (`app`). Expone el puerto 8083.

```bash
docker build -t ms-busqueda:local .
```

Para levantarlo junto con ms-articulos y los demás servicios se usa el
`docker-compose.yml` de
[bodegaexpress-infra](https://github.com/BodegaExpressDuoc/bodegaexpress-infra).

## CI/CD y versiones

El workflow [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) (GitHub Actions):

| Evento | Jobs |
|---|---|
| Push a `develop` o pull request hacia `main` | `build-test`: compila y ejecuta las pruebas (`mvnw verify`) |
| Push a `main` (merge del pull request) | `build-test` y luego `deploy` |

El job `deploy`:

1. crea el repositorio `ms-busqueda` en Amazon ECR si no existe;
2. construye la imagen y la sube con tres tags: la versión del `pom.xml`
   (por ejemplo `0.2.0`), el sha corto del commit y `latest`;
3. ejecuta en la EC2, mediante AWS Systems Manager (SSM), el script `cicd.sh` de
   bodegaexpress-infra, que descarga la imagen nueva y reinicia solo este servicio.

La rama `main` está protegida: solo cambia mediante pull request desde `develop`.
Cada entrega se marca con un tag anotado en `main`:

| Versión | Tag | Entrega | Cambios principales |
|---|---|---|---|
| 0.1.0 | `v0.1.0` | Evaluación Parcial 2 | Búsqueda de artículos por nombre consultando ms-articulos |
| 0.2.0 | `v0.2.0` | Evaluación Parcial 3 | Rutas `/api/v1`, Dockerfile, pipeline de despliegue en AWS y acceso solo a través del API Gateway (`FiltroGateway`) |

## Servicios relacionados

| Servicio | Puerto | Ruta base |
|---|---|---|
| ms-usuarios | 8081 | `/api/v1/usuarios` |
| ms-articulos | 8082 | `/api/v1/articulos` |
| ms-busqueda | 8083 | `/api/v1/busqueda` |
