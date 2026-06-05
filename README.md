# Inventarios RTI — Back end

API REST para la plataforma de inventarios RTI. Expone servicios de autenticación, gestión de usuarios e inventarios, carga masiva por Excel, inspección de activos y **cruce de información cargada** (administrador vs inspector) con exportación a Excel.

Conecta con frontends **Angular** (portal administrador e inspector web) y consumo desde aplicación móvil según el diagrama de flujo del proyecto.

---

## Objetivo del proyecto

Construir un backend que permita:

- Autenticar usuarios con roles **Administrador** e **Inspector** (JWT).
- Gestionar usuarios e inventarios (CRUD).
- Cargar activos iniciales del administrador mediante **plantilla y archivo Excel**.
- Registrar activos inspeccionados en campo (API consumida por web/móvil inspector).
- **Comparar** la información cargada del administrador contra la del inspector.
- Clasificar cada activo en **CRUCE NORMAL**, **EDITADO** o **SOBRANTE**.
- Generar y descargar **Excel** con el resultado del cruce y reportes de auditoría.

Referencias de negocio y diseño:

- [Figma — Inventarios (UI)](https://www.figma.com/design/Xt3pkCL3TF1RDvvcAGXUiK/Inventarios)
- [FigJam — Diagramas de flujo](https://www.figma.com/board/PC30yyoGu4enuvqnXguZPk/Inventarios-RTI)
- `InventariosMockup.pdf` — mockups visuales
- Jira proyecto **IRT** — historias `Back-end`, `Web-Admin`, `Web-Inspector`

---

## Entorno de ejecución

Verificado en el entorno de desarrollo:

```txt
C:\Users\Luis Carlos Triana>java -version
java version "17.0.10" 2024-01-16 LTS
Java(TM) SE Runtime Environment (build 17.0.10+11-LTS-240)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.10+11-LTS-240, mixed mode, sharing)
```

| Componente        | Versión / detalle                          |
|-------------------|--------------------------------------------|
| Java              | **17 LTS** (17.0.10)                       |
| Spring Boot       | 3.x (compatible con Java 17)               |
| Base de datos     | **MySQL** 8.x                              |
| Build             | Maven o Gradle                             |
| Autenticación     | JWT (Bearer token)                         |
| Documentación API | SpringDoc OpenAPI / Swagger (recomendado) |
| Excel             | Apache POI o equivalente                   |

---

## Tecnologías utilizadas

### Core

- Java 17 LTS
- Spring Boot 3
- Spring Web (REST)
- Spring Security + JWT
- Spring Data JPA
- Spring Validation
- MySQL Connector/J
- Lombok (opcional)

### Funcionalidades transversales

- Apache POI — lectura/escritura Excel (plantilla, carga admin, cruce, reportes)
- MapStruct o mappers manuales — DTO ↔ entidades
- Flyway o Liquibase — migraciones de esquema MySQL

### Integración frontends

| Cliente              | Stack              | Etiqueta Jira    |
|----------------------|--------------------|------------------|
| Portal administrador | Angular + PrimeNG  | `Web-Admin`      |
| Portal inspector     | Angular + PrimeNG  | `Web-Inspector`  |
| App móvil inspector  | (según proyecto)   | `Movil`          |

---

## Arquitectura esperada

```txt
inventarios-rti-api/
  src/main/java/com/rti/inventarios/
    InventariosRtiApplication.java

    config/
      SecurityConfig.java
      JwtConfig.java
      CorsConfig.java

    controller/
      AuthController.java
      UsuarioController.java
      InventarioController.java
      ActivoController.java
      CentroCostosController.java
      ReporteController.java

    service/
      AuthService.java
      UsuarioService.java
      InventarioService.java
      ActivoService.java
      ExcelCargaService.java
      CruceInventarioService.java
      ReporteAuditoriaService.java

    repository/
      UsuarioRepository.java
      InventarioRepository.java
      ActivoAdministradorRepository.java
      ActivoInspectorRepository.java
      ResultadoCruceRepository.java

    model/
      entity/
      dto/
      enums/
        RolUsuario.java          // ADMINISTRADOR, INSPECTOR
        OrigenActivo.java        // ADMINISTRADOR, INSPECTOR
        EstadoCruce.java         // CRUCE_NORMAL, EDITADO, SOBRANTE

    exception/
      GlobalExceptionHandler.java

  src/main/resources/
    application.yml
    application-dev.yml
    db/migration/               // scripts Flyway

  pom.xml / build.gradle
```
## Cada clase y funcion debe tener un comentario correspondiente y no debe haber codigo quemado
---

## Conexión MySQL

Ejemplo `application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/inventarios_rti?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true
    username: ${DB_USER:inventarios_user}
    password: ${DB_PASSWORD:changeme}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
```

mysql-4ce7272-rtisas-69cd.k.aivencloud.com

SSL mode REQUIRED

Variables de entorno recomendadas:

```txt
DB_HOST=localhost
DB_PORT=3306

JWT_EXPIRATION_MS=86400000
```

---

## Modelo de datos (resumen)

### Usuario

| Campo        | Tipo        | Notas                          |
|-------------|-------------|--------------------------------|
| id          | BIGINT PK   |                                |
| nombre      | VARCHAR     |                                |
| correo      | VARCHAR UK  | Login                          |
| contrasena  | VARCHAR     | Hash BCrypt                    |
| rol         | ENUM        | ADMINISTRADOR, INSPECTOR       |
| fecha_creacion | DATETIME |                                |

### Inventario

| Campo         | Tipo      | Notas                    |
|--------------|-----------|--------------------------|
| id           | BIGINT PK |                          |
| nombre       | VARCHAR   | Objetivo / nombre        |
| codigo       | VARCHAR UK| Código de inventario     |
| estado       | VARCHAR   | Ejecución, Finalizado…   |
| inspector_id | FK Usuario| Rol INSPECTOR            |

### Activo (por origen)

Registros separados o discriminados por `origen`:

- **ADMINISTRADOR** — carga Excel inicial
- **INSPECTOR** — registro en inspección

| Campo       | Tipo    | Comparación en cruce |
|------------|---------|----------------------|
| id_activo  | VARCHAR | **Clave de cruce**   |
| etiqueta   | VARCHAR | Sí                   |
| descripcion| VARCHAR | Sí                   |
| marca      | VARCHAR | Sí                   |
| serial     | VARCHAR | Sí                   |
| modelo     | VARCHAR | Sí                   |
| responsable| VARCHAR | Sí                   |
| ciudad     | VARCHAR | Sí                   |
| inventario_id | FK   |                      |

### Resultado cruce (opcional persistido)

Snapshot del último cruce por inventario para `GET /activos-cruzados` y generación de Excel.

---

## Información cargada y cruce

Flujo de negocio que debe implementar el backend:

```txt
1. Administrador crea inventario y asigna inspector
2. Administrador descarga plantilla Excel
3. Administrador sube Excel → activos origen ADMINISTRADOR
4. Inspector registra activos en campo → origen INSPECTOR
5. Administrador consulta información cargada (ambas fuentes)
6. Administrador ejecuta CRUCE
7. Sistema clasifica cada activo y permite descargar Excel final
```

### Consulta sin cruce

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/inventarios/{id}/activos-administrador` | Solo carga admin |
| GET | `/api/inventarios/{id}/activos-inspector` | Solo carga inspector |
| GET | `/api/inventarios/{id}/informacion-cargada` | Ambas fuentes en un JSON |

### Ejecución del cruce

| Método | Endpoint | Descripción | Permisos |
|--------|----------|-------------|----------|
| POST | `/api/inventarios/{id}/cruce` | Ejecuta comparación | Administrador |
| GET | `/api/inventarios/{id}/activos-cruzados` | Último resultado JSON | Admin/Inspector |
| GET | `/api/inventarios/{id}/cruce/excel` | **Descarga Excel** | Admin/Inspector |
| GET | `/api/inventarios/{id}/activos-administrador/excel` | Excel solo admin | Administrador |

---

## Reglas de cruce (comparación por `ID_ACTIVO`)

El servicio `CruceInventarioService` debe:

1. Cargar mapa de activos **administrador** indexado por `idActivo`.
2. Cargar mapa de activos **inspector** indexado por `idActivo`.
3. Para cada `idActivo` presente en **inspector**:
   - Si **no existe** en administrador → **SOBRANTE**
   - Si existe en ambos y **todos** los campos comparables son iguales → **CRUCE NORMAL**
   - Si existe en ambos y **algún** campo difiere → **EDITADO** (registrar `camposModificados`)
4. Activos solo en administrador sin contraparte inspector → definir regla de negocio (ej. `FALTANTE`);.

### Campos comparables

```txt
etiqueta, descripcion, marca, serial, modelo, responsable, ciudad
```

Comparación **case-insensitive** y con **trim** recomendado para strings.

### Estados

| Estado         | Condición |
|----------------|-----------|
| CRUCE NORMAL   | Existe en admin e inspector; todos los campos comparables coinciden |
| EDITADO        | Existe en ambos; al menos un campo comparable difiere |
| SOBRANTE       | Existe solo en carga del inspector |

### Ejemplo

| idActivo | Admin marca | Inspector marca | Estado        |
|----------|-------------|-----------------|---------------|
| 1001     | Lenovo      | Lenovo          | CRUCE NORMAL  |
| 1002     | Lenovo      | HP              | EDITADO       |
| 5000     | —           | Dell            | SOBRANTE      |

### Respuesta JSON (`POST /api/inventarios/{id}/cruce`)

```json
{
  "inventarioId": 1,
  "fechaCruce": "2026-06-03T18:00:00Z",
  "resumen": {
    "total": 3,
    "cruceNormal": 1,
    "editado": 1,
    "sobrante": 1
  },
  "activos": [
    {
      "idActivo": "1001",
      "etiqueta": "7501000111800",
      "descripcion": "Portátil Lenovo",
      "marca": "Lenovo",
      "serial": "ABC123",
      "modelo": "Yoga",
      "responsable": "Juan",
      "ciudad": "Bogotá",
      "estado": "CRUCE NORMAL",
      "camposModificados": []
    },
    {
      "idActivo": "1002",
      "etiqueta": "7501000111801",
      "descripcion": "Portátil Lenovo",
      "marca": "HP",
      "serial": "ABC124",
      "modelo": "Yoga",
      "responsable": "Juan",
      "ciudad": "Bogotá",
      "estado": "EDITADO",
      "camposModificados": ["marca"]
    },
    {
      "idActivo": "5000",
      "etiqueta": "7501000999999",
      "descripcion": "Monitor Dell",
      "marca": "Dell",
      "serial": "XYZ111",
      "modelo": "P2419",
      "responsable": "Pedro",
      "ciudad": "Bogotá",
      "estado": "SOBRANTE",
      "camposModificados": []
    }
  ]
}
```

### Excel de salida (`GET /cruce/excel`)

Columnas:

```txt
ID_ACTIVO | ETIQUETA | DESCRIPCION | MARCA | SERIAL | MODELO | RESPONSABLE | CIUDAD | ESTADO
```

`Content-Type`: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

---

## APIs REST — catálogo

Todas las rutas bajo prefijo `/api`. Header en rutas protegidas:

```txt
Authorization: Bearer {jwt_token}
```

### Autenticación

```http
POST /api/auth/login
```

**Request:**

```json
{
  "usuario": "admin@rti.com.co",
  "contraseña": "********"
}
```

**Response 200:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "usuario": "admin@rti.com.co",
  "rol": "Administrador"
}
```

**Response 401:**

```json
{ "error": "Credenciales inválidas" }
```

---

### Usuarios (rol Administrador)

```http
GET    /api/usuarios
POST   /api/usuarios
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}
```

**POST body:**

```json
{
  "nombre": "Carlos Jiménez",
  "correo": "carlos.jimenez@rti.com.co",
  "contraseña": "********",
  "rol": "Inspector"
}
```

`rol` permitido: `Administrador` | `Inspector`

---

### Inventarios

```http
GET    /api/inventarios
POST   /api/inventarios
GET    /api/inventarios/{id}
PUT    /api/inventarios/{id}
DELETE /api/inventarios/{id}
GET    /api/inventarios/validar/{codigo}    # Inspector — inventario asignado
```

**POST body:**

```json
{
  "nombre": "Inventario Bulevar Niza",
  "codigo": "INV-014",
  "estado": "Ejecución",
  "inspectorId": 3
}
```

**PUT body:** (mismo formato que POST)

Los servicios PUT y DELETE están implementados para permitir la edición y eliminación de inventarios.

---

### Carga Excel (administrador)

```http
GET  /api/inventarios/{id}/plantilla
POST /api/inventarios/{id}/cargar-excel     # multipart: archivo
```

**Comportamiento de carga:**
- Si un activo ya existe en el inventario (mismo `idActivo`), se actualizan sus datos
- Si es un activo nuevo, se crea un nuevo registro
- Permite subir múltiples tandas de activos al mismo inventario sin errores

**POST Response 200:**

```json
{
  "mensaje": "Carga exitosa",
  "registrosProcesados": 150,
  "registrosGuardados": 148,
  "errores": []
}
```

**POST Response 400:**

```json
{
  "error": "Estructura de Excel inválida",
  "detalle": ["Columna MARCA faltante", "Fila 12: ID_ACTIVO vacío"]
}
```

Columnas obligatorias plantilla:

```txt
ID_ACTIVO, ETIQUETA, DESCRIPCION, MARCA, SERIAL, MODELO, RESPONSABLE, CIUDAD, ESTADO
```

---

### Activos (inspector)

```http
GET  /api/activos/{codigo}              # Busca activos del administrador por código
GET  /api/activos/barcode/{barcode}     # Busca activos del administrador por barcode
POST /api/activos
PUT  /api/activos/{id}
```

**GET /api/activos/{codigo}:**

Busca y retorna un activo cargado por el administrador desde el Excel usando su código de activo.
Útil para que el inspector pueda consultar la información del activo registrada previamente.

**GET /api/activos/barcode/{barcode}:**

Busca y retorna un activo cargado por el administrador desde el Excel usando su código de barras (etiqueta).
Útil para escanear códigos de barras durante la inspección.

**POST body:**

```json
{
  "inventarioId": 1,
  "idActivo": "5000",
  "etiqueta": "7501000999999",
  "descripcion": "Monitor Dell",
  "marca": "Dell",
  "serial": "XYZ111",
  "modelo": "P2419",
  "responsable": "Pedro",
  "ciudad": "Bogotá"
}
```

**Comportamiento de POST activos:**
- Si el activo ya existe en el inventario (mismo `idActivo`), actualiza sus datos
- Si es nuevo, crea un registro con origen `INSPECTOR`
- Permite re-registrar activos sin generar errores

---

### Reportes

```http
GET /api/reportes/{centro}?inventarioId=1
```

**Response 200:** archivo Excel (auditoría)

**Response 404:**

```json
{
  "error": "Sin datos",
  "mensaje": "No hay registros de auditoría para el centro indicado"
}
```

---

## Seguridad y roles

| Rol           | Permisos principales |
|---------------|----------------------|
| Administrador | CRUD usuarios, CRUD inventarios, carga Excel, cruce, descargas Excel |
| Inspector     | Validar inventario asignado, consultar/registrar activos, descargar Excel de cruce, reporte auditoría |

Implementar con `@PreAuthorize` o reglas en `SecurityFilterChain`.

Rutas públicas: solo `POST /api/auth/login`.

---

## Códigos HTTP estándar

| Código | Uso |
|--------|-----|
| 200    | OK, consultas, actualizaciones |
| 201    | Recurso creado (activo, usuario) |
| 400    | Validación Excel, datos inválidos, cruce sin cargas |
| 401    | Token inválido o credenciales incorrectas |
| 403    | Rol sin permiso |
| 404    | Inventario, activo, centro o cruce no encontrado |
| 500    | Error interno (log + mensaje genérico) |

---

## Buenas prácticas Spring Boot

- DTOs en capa `controller`; no exponer entidades JPA directamente.
- Transacciones en servicios (`@Transactional`) para carga Excel y cruce.
- Validación con `@Valid` y `jakarta.validation`.
- Manejo centralizado de excepciones (`@ControllerAdvice`).
- Logs estructurados (SLF4J) en carga masiva y cruce.
- Tests unitarios para `CruceInventarioService` (casos NORMAL, EDITADO, SOBRANTE y FALTANTE).
- Tests de integración con Testcontainers MySQL (opcional).

---

## Flujo de implementación sugerido

1. Proyecto Spring Boot 3 + MySQL + JWT
2. Entidades y migraciones Flyway
3. Auth + usuarios + inventarios
4. Carga Excel administrador
5. APIs activos inspector
6. Consulta información cargada
7. **CruceInventarioService** + persistencia resultado
8. Exportación Excel cruce y reportes
9. OpenAPI + colección Postman `InventariosRTI.postman_collection.json`

---

## Referencias Jira (historias Back-end)

| Issue  | Tema |
|--------|------|
| IRT-29 | Auth JWT |
| IRT-30 | CRUD usuarios |
| IRT-31 | CRUD inventarios |
| IRT-32 | Validar inventario inspector |
| IRT-33 | Consultar activo |
| IRT-34 | Registrar/actualizar activos inspector |
| IRT-35 | Información cargada (consulta) |
| IRT-36 | Plantilla y subida Excel |
| IRT-37 | **Cruce + Excel (reglas NORMAL/EDITADO/SOBRANTE)** |
| IRT-38 | Reporte auditoría |
| IRT-39 | Centro de costos |

---

## Resultado esperado

Backend listo para:

- Compilar y ejecutar con **Java 17** y **Spring Boot**
- Realizar conexion con la base de datos
- Persistir en **MySQL**
- Exponer REST documentados alineados con front Angular
- Comparar información cargada admin vs inspector
- Entregar **Excel** con estados de cruce
- Integrarse con el ecosistema Inventarios RTI (web admin, web inspector)
- Entregar las peticiones en formato POSTMAN para realizar pruebas 
- Dejar documentacion en cada servicio explicando su funcionamiento
