# ms-busqueda

Busqueda inicial por nombre mediante REST. Java 17 y Spring Boot 4.1.1.
Escucha en el puerto 8083 y consulta ms-articulos en http://localhost:8082.

## Ejecutar

Primero iniciar PostgreSQL y ms-articulos. Desde esta carpeta:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

En Postman o el navegador:

```text
http://localhost:8083/api/busqueda?nombre=caja
```

La busqueda admite coincidencias parciales, ignora mayusculas y espacios al
inicio y al final. No elimina acentos. Devuelve los datos del articulo y su stock.

- 200: lista de coincidencias; [] cuando no hay resultados.
- 400: nombre ausente, vacio o con mas de 150 caracteres tras quitar espacios.
- 503: ms-articulos no responde o devuelve un error.

Variables opcionales: BUSQUEDA_PORT y ARTICULOS_BASE_URL.
Limites de espera: 3 segundos de conexion y 5 segundos de lectura.

Las pruebas simulan la respuesta HTTP de ms-articulos y ejercitan controlador,
servicio y cliente REST sin requerir otros procesos ni modificar datos.

Esta version obtiene el catalogo completo y filtra en memoria. Es una base local
para la entrega; no implementa aun el modelo CQRS, una base propia, SQS,
Eureka, Gateway, JWT o Circuit Breaker del diagrama.

## Servicios locales

| Servicio | Puerto | Ruta |
|---|---|---|
| ms-usuarios | 8081 | /api/usuarios |
| ms-articulos | 8082 | /api/articulos |
| ms-busqueda | 8083 | /api/busqueda?nombre=caja |
