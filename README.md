# reporteservice

API REST para la gestión de reportes de basura (Limpieza Pública), construida con Spring Boot 4, Spring Data JPA y MySQL.

## Requisitos

- Java 21
- MySQL 8 (o PostgreSQL cambiando la URL y credenciales)

## Configuración

La conexión se configura en `src/main/resources/application.properties` y puede sobreescribirse con variables de entorno:

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/reportesdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | *(vacío)* |

Al iniciar, Hibernate crea/actualiza automáticamente la tabla `reportes` (`spring.jpa.hibernate.ddl-auto=update`).
También puedes crearla manualmente con el script `docs/database.sql`.

Para levantar la aplicación:

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

| Método | Endpoint | Descripción |
| --- | --- | --- |
| POST | `/reportes/` | Registra un reporte. El estado inicial siempre es `1` (pendiente). |
| GET | `/reportes/?offset=0&limit=5` | Lista los reportes de forma paginada. |
| GET | `/reportes/{id}` | Obtiene el reporte con el código indicado. |
| GET | `/reportes/ciudadano/{dni}` | Obtiene todos los reportes de un ciudadano. |
| PUT | `/reportes/{id}` | Marca el reporte como atendido (estado de `1` a `0`). |

### Registrar un reporte

```http
POST /reportes/
Content-Type: application/json

{
  "dni": "12345678",
  "fecha": "2026-10-29T09:30:00",
  "tipo": "DOMICILIARIA",
  "direccion": "Av. Los Álamos 456",
  "referencia": "Cerca al mercado"
}
```

- `fecha` es opcional: si no se envía se usa la fecha y hora actual.
- `referencia` es opcional.
- El campo `estado` no se recibe; se asigna `1` automáticamente.

Respuesta `201 Created`:

```json
{
  "id": 1,
  "dni": "12345678",
  "fecha": "2026-10-29T09:30:00",
  "tipo": "DOMICILIARIA",
  "direccion": "Av. Los Álamos 456",
  "referencia": "Cerca al mercado",
  "estado": 1
}
```

### Listar reportes

```http
GET /reportes/?offset=0&limit=5
```

Respuesta `200 OK`:

```json
{
  "content": [ { "id": 1, "...": "..." } ],
  "offset": 0,
  "limit": 5,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

`offset` es la cantidad de registros a saltar y `limit` el tamaño de página (máximo 100).

### Atender un reporte

```http
PUT /reportes/1
```

Devuelve el reporte con `"estado": 0`. Si el reporte ya estaba atendido responde `409 Conflict`;
si no existe, `404 Not Found`.

## Manejo de errores

Los errores se devuelven con este formato:

```json
{
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Datos inválidos en la solicitud",
  "detalles": [ "dni: El DNI debe tener exactamente 8 dígitos" ],
  "path": "/reportes/",
  "timestamp": "2026-10-29T10:00:00Z"
}
```

## Despliegue en Render

Render no tiene runtime nativo de Java, así que el proyecto se despliega con **Docker** (ya incluido: `Dockerfile`). Render tampoco ofrece MySQL administrado, por eso el despliegue usa **PostgreSQL** (el driver ya está en `build.gradle`).

### Opción A: Blueprint (recomendada)

1. Sube el proyecto a un repositorio de GitHub.
2. En el dashboard de Render: **New + → Blueprint**.
3. Conecta el repositorio. Render detecta `render.yaml` y muestra la base de datos PostgreSQL y el servicio web.
4. Pulsa **Apply**. Render crea la base de datos, construye la imagen Docker y despliega.
5. La API queda en `https://<nombre-del-servicio>.onrender.com`.

`render.yaml` configura automáticamente:

- La base de datos PostgreSQL `reportesdb`.
- El perfil `render` (`src/main/resources/application-render.properties`).
- Las variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` y `DB_PASSWORD` tomadas de la base de datos.
- El `PORT` lo inyecta Render y la aplicación ya lo lee con `server.port=${PORT:8080}`.

### Opción B: configuración manual

1. **New + → Postgres**: crea la base de datos y copia su *Internal Database URL*.
2. Convierte la URL al formato JDBC. Por ejemplo:

   ```text
   postgresql://reportes:clave@dpg-xxxx-a:5432/reportesdb
   ```

   se convierte en:

   ```text
   jdbc:postgresql://dpg-xxxx-a:5432/reportesdb
   ```

3. **New + → Web Service → Docker**, conecta el repositorio y usa `./Dockerfile` como Dockerfile.
4. En **Environment** agrega:

   | Variable | Valor |
   | --- | --- |
   | `DB_URL` | `jdbc:postgresql://<host>:5432/<base>` |
   | `DB_USERNAME` | usuario de la base de datos |
   | `DB_PASSWORD` | contraseña de la base de datos |

   No hace falta configurar `PORT`. Si usas la URL *externa* de PostgreSQL, agrega `?sslmode=require` al final de `DB_URL`.
5. Health check: `/reportes/`.

La tabla `reportes` se crea sola al arrancar (`spring.jpa.hibernate.ddl-auto=update`).

### Notas sobre Render

- El plan gratuito de servicios web suspende la instancia tras un periodo de inactividad: la primera petición puede tardar cerca de un minuto.
- El plan gratuito de PostgreSQL de Render tiene fecha de expiración (actualmente 30 días). Si expira, cambia el plan o migra la base de datos.
- Si prefieres MySQL, Render no lo ofrece como servicio administrado; puedes usar uno externo (Aiven, Railway, Clever Cloud, etc.) y configurar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con la URL JDBC de MySQL.
- Para probar la imagen localmente (requiere Docker Desktop):

  ```bash
  docker build -t reporteservice .
  docker run --rm -p 8080:8080 \
    -e DB_URL="jdbc:postgresql://host:5432/reportesdb" \
    -e DB_USERNAME="reportes" \
    -e DB_PASSWORD="clave" \
    reporteservice
  ```

## Pruebas

```bash
./gradlew test
```

Las pruebas usan H2 en memoria, por lo que no requieren una base de datos MySQL en ejecución.
