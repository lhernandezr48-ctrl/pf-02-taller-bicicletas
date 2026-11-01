# PF-02 — Órdenes de reparación de bicicletas

**Estudiante:** Lilian Gabriela Hernández Rodríguez
**Carné:** [9941-25-27807]
**Proyecto asignado:** PF-02 — Órdenes de reparación de bicicletas

## Descripción del problema

Un taller de bicicletas necesita registrar sus servicios y controlar las órdenes de reparación que recibe. El personal debe poder consultar qué reparaciones están pendientes, en proceso o terminadas, y cuánto deberá pagar cada cliente.

El sistema administra dos entidades relacionadas: **Servicio de taller** y **Orden de reparación**. Cada orden selecciona un servicio, y su costo estimado se obtiene del costo base de ese servicio más un recargo autorizado.

## Tecnologías utilizadas

- Java 21
- Maven (con Maven Wrapper)
- Spring Boot 4.1.2: Spring Web, Spring Data JPA, Validation
- PostgreSQL 14 y su driver JDBC
- Postman para las pruebas de la API

## Estructura general del sistema

```
src/main/java/com/taller/bicicletas
├── controller   Endpoints REST
├── service      Lógica y reglas de negocio
├── repository   Acceso a datos (Spring Data JPA)
├── entity       Entidades JPA y enum EstadoOrden
├── dto          Objetos de entrada y salida de la API
└── exception    Excepciones y manejador centralizado
sql/             Scripts de estructura y datos de prueba
postman/         Colección de Postman con las pruebas
evidencias/      Documento de seguimiento y evidencias (Word)
src/main/resources/static/   Frontend (HTML, CSS y JavaScript)
```

## Requisitos para ejecutar el proyecto

- JDK 21 instalado
- PostgreSQL 14 en ejecución
- Git (para clonar el repositorio)
- Postman (opcional, para probar la API)

## Configuración de PostgreSQL

1. Crear la base de datos desde pgAdmin o psql:

```sql
CREATE DATABASE taller_bicicletas;
```

2. Ejecutar los scripts en la base `taller_bicicletas`, en este orden:
    - `sql/01_estructura.sql` (tablas)
    - `sql/02_datos.sql` (datos de prueba)

3. Revisar la conexión en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/taller_bicicletas
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

- El puerto del ejemplo es **5433**. Si su PostgreSQL usa el puerto por defecto, cámbielo a **5432**.
- La contraseña **no está en el repositorio**: se lee de la variable de entorno `DB_PASSWORD`.

## Instrucciones para iniciar el backend

Desde la carpeta del proyecto, en PowerShell:

```powershell
$env:DB_PASSWORD='su_contraseña_de_postgres'
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Para detenerla se usa `Ctrl + C`.
## Instrucciones para abrir el frontend

Con el backend en ejecución, abra en el navegador:

http://localhost:8080

Los archivos del frontend (HTML, CSS y JavaScript) están en `src/main/resources/static/`:

- `index.html`
- `css/estilos.css`
- `js/app.js`

Desde la interfaz se pueden registrar, consultar, modificar y eliminar órdenes y servicios, cambiar el estado de una orden, filtrar por estado o por cliente, y ver el total a pagar de un cliente y el indicador de órdenes pendientes.
## Endpoints

### Servicios — `/api/servicios`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/servicios` | Registrar un servicio |
| GET | `/api/servicios` | Listar servicios |
| GET | `/api/servicios/{id}` | Consultar por identificador |
| PUT | `/api/servicios/{id}` | Modificar un servicio |
| PATCH | `/api/servicios/{id}/desactivar` | Desactivar un servicio |

### Órdenes de reparación — `/api/ordenes`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/ordenes` | Registrar una orden (nace en estado PENDIENTE) |
| GET | `/api/ordenes` | Listar órdenes |
| GET | `/api/ordenes/{id}` | Consultar por identificador |
| PUT | `/api/ordenes/{id}` | Modificar una orden |
| PATCH | `/api/ordenes/{id}/estado` | Cambiar el estado de la orden |
| DELETE | `/api/ordenes/{id}` | Eliminar una orden pendiente |
| GET | `/api/ordenes/estado/{estado}` | Órdenes por estado (PENDIENTE, EN_PROCESO, TERMINADA) |
| GET | `/api/ordenes/cliente/{cliente}` | Órdenes por cliente |
| GET | `/api/ordenes/cliente/{cliente}/total` | Total estimado a pagar por cliente |
| GET | `/api/ordenes/pendientes/cantidad` | Indicador: cantidad de órdenes pendientes |

Ejemplo de cuerpo para registrar una orden:

```json
{
  "cliente": "Carlos Méndez",
  "bicicleta": "Trek Marlin 5",
  "fecha": "2026-10-31",
  "observaciones": "Frenos hacen ruido",
  "recargo": 10.00,
  "servicioId": 2
}
```

Ejemplo de cuerpo para cambiar el estado:

```json
{ "estado": "EN_PROCESO" }
```

## Reglas principales del negocio

1. Toda orden registra cliente, bicicleta, fecha, estado y observaciones.
2. El costo estimado es el costo base del servicio seleccionado más el recargo autorizado (si no hay recargo, cuenta como 0).
3. Una orden terminada no puede regresar al estado pendiente, ni a ningún otro estado, y tampoco puede modificarse.
4. Solo se pueden usar servicios activos al registrar o modificar una orden.
5. Solo se pueden eliminar órdenes que estén en estado PENDIENTE.
6. No se permiten servicios con nombre duplicado.

## Códigos de respuesta HTTP

| Código | Cuándo se devuelve |
|---|---|
| 200 / 201 / 204 | Operación exitosa |
| 400 | Datos inválidos, JSON mal formado o estado no permitido |
| 404 | Orden, servicio o ruta inexistente |
| 409 | Duplicado o regla de negocio incumplida |
| 500 | Error inesperado del sistema |

## Pruebas y evidencias

- La colección de Postman con las pruebas está en la carpeta `postman/`.
- El documento de seguimiento y evidencias está en la carpeta `evidencias/`.
