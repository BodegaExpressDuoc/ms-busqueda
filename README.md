# ms-busqueda

Microservicio de búsqueda de artículos del sistema BodegaExpress. Expone una API
REST para buscar artículos por nombre, obteniendo los datos desde ms-articulos.

## Tecnologías

- Java 17
- Spring Boot 4.1.1 (Spring Web MVC, RestClient)
- Maven (mediante Maven Wrapper)

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
| `BUSQUEDA_PORT` | No | `8083` | Puerto HTTP del servicio |
| `ARTICULOS_BASE_URL` | No | `http://localhost:8082` | URL base de ms-articulos |

Para cambiarlas, definirlas en la terminal antes de ejecutar. Por ejemplo, en PowerShell:

```powershell
$env:ARTICULOS_BASE_URL = "http://localhost:9082"
```

### 3. Compilar, probar y empaquetar

```powershell
.\mvnw.cmd clean install
```

Ejecuta las pruebas y genera el archivo `target/ms-busqueda-0.0.1-SNAPSHOT.jar`.
Las pruebas simulan las respuestas de ms-articulos, por lo que no requieren otros
servicios en ejecución.

### 4. Ejecutar

Iniciar primero ms-articulos. Luego, con Maven:

```powershell
.\mvnw.cmd spring-boot:run
```

O con el `.jar` generado:

```powershell
java -jar target/ms-busqueda-0.0.1-SNAPSHOT.jar
```

El servicio queda disponible en `http://localhost:8083/api/busqueda`.
Para detenerlo, usar `Ctrl+C`.

## Estructura del proyecto

```text
src/main/java/cl/duoc/bodegaexpress/busqueda/
├── MsBusquedaApplication.java   Punto de entrada de la aplicación
├── controller/                  Endpoints REST
├── service/                     Lógica de búsqueda
├── config/                      Configuración del cliente HTTP hacia ms-articulos
└── dto/                         Objetos de respuesta
```

## API

URL base: `http://localhost:8083/api/busqueda`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/api/busqueda?nombre={texto}` | Busca artículos cuyo nombre contenga el texto | `200` |

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

## Pruebas manuales

1. Iniciar ms-articulos y crear al menos un artículo.
2. Iniciar este servicio (paso 4).
3. Enviar GET `http://localhost:8083/api/busqueda?nombre=caja` y verificar los resultados.
4. Enviar GET `http://localhost:8083/api/busqueda?nombre=` para obtener `400`.
5. Detener ms-articulos y repetir el paso 3 para obtener `503`.

## Servicios relacionados

| Servicio | Puerto | Ruta base |
|---|---|---|
| ms-usuarios | 8081 | `/api/usuarios` |
| ms-articulos | 8082 | `/api/articulos` |
| ms-busqueda | 8083 | `/api/busqueda` |
