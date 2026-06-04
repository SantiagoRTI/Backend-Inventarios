# Inventarios RTI Backend - Guía de Ejecución y Pruebas

## ✅ Estado del Proyecto

**El backend está completamente funcional y probado:**

- ✅ Compilación exitosa con Maven
- ✅ Tests unitarios pasando (5/5 tests del servicio de cruce)
- ✅ Conexión a MySQL en Aiven Cloud funcionando
- ✅ Migraciones Flyway ejecutadas correctamente
- ✅ Aplicación corriendo en http://localhost:8080
- ✅ Swagger UI disponible
- ✅ Colección Postman incluida

---

## 🚀 Cómo Ejecutar el Proyecto

### 1. Requisitos Previos

- **Java 17 LTS** instalado
- **Maven 3.x** instalado
- Conexión a Internet (para MySQL en Aiven Cloud)

### 2. Compilar el Proyecto

```bash
cd Backend
mvn clean compile
```

### 3. Ejecutar Tests

```bash
# Tests del servicio de cruce
mvn test -Dtest=CruceInventarioServiceTest

# Todos los tests
mvn test
```

**Resultado esperado:**
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

### 4. Iniciar la Aplicación

```bash
mvn spring-boot:run
```

La aplicación se iniciará en: `http://localhost:8080`

Verás un banner como este cuando esté lista:

```
╔═══════════════════════════════════════════════════════╗
║                                                       ║
║         Inventarios RTI Backend - INICIADO            ║
║                                                       ║
║   Swagger UI: http://localhost:8080/swagger-ui.html  ║
║   API Docs:   http://localhost:8080/api-docs         ║
║                                                       ║
╚═══════════════════════════════════════════════════════╝
```

---

## 📖 Documentación de la API

### Swagger UI

Accede a la documentación interactiva en:
**http://localhost:8080/swagger-ui.html**

Aquí puedes:
- Ver todos los endpoints disponibles
- Probar cada endpoint directamente desde el navegador
- Ver los esquemas de request/response

### OpenAPI JSON

Descarga la especificación OpenAPI en:
**http://localhost:8080/api-docs**

---

## 🧪 Pruebas con Postman

### Importar la Colección

1. Abre Postman
2. Importa el archivo `InventariosRTI.postman_collection.json`
3. La colección incluye variables para el token JWT y el base URL

### Flujo de Pruebas Recomendado

#### 1. Autenticación

**Login como Administrador:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "usuario": "admin@rti.com.co",
    "contraseña": "Admin123!"
}
```

**Respuesta:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "usuario": "admin@rti.com.co",
    "rol": "Administrador"
}
```

El token se guardará automáticamente en las variables de la colección.

#### 2. Crear Inventario

```http
POST http://localhost:8080/api/inventarios
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
    "nombre": "Inventario Bulevar Niza 2026",
    "codigo": "INV-BN-2026",
    "estado": "Ejecucion",
    "inspectorId": 2,
    "centroCostosId": 1
}
```

#### 3. Descargar Plantilla Excel

```http
GET http://localhost:8080/api/inventarios/{id}/plantilla
Authorization: Bearer {{jwt_token}}
```

Descarga un archivo Excel con las columnas requeridas:
`ID_ACTIVO | ETIQUETA | DESCRIPCION | MARCA | SERIAL | MODELO | RESPONSABLE | CIUDAD | ESTADO`

#### 4. Cargar Activos del Administrador

```http
POST http://localhost:8080/api/inventarios/{id}/cargar-excel
Authorization: Bearer {{jwt_token}}
Content-Type: multipart/form-data

archivo: [seleccionar archivo Excel]
```

**Respuesta exitosa:**
```json
{
    "mensaje": "Carga exitosa",
    "registrosProcesados": 150,
    "registrosGuardados": 148,
    "errores": []
}
```

#### 5. Registrar Activos del Inspector

**Login como Inspector:**
```http
POST http://localhost:8080/api/auth/login

{
    "usuario": "carlos.jimenez@rti.com.co",
    "contraseña": "Inspector123!"
}
```

**Registrar activo:**
```http
POST http://localhost:8080/api/activos
Authorization: Bearer {{jwt_token}}

{
    "inventarioId": 1,
    "idActivo": "ACT-001",
    "etiqueta": "7501000111800",
    "descripcion": "Portátil Lenovo ThinkPad",
    "marca": "Lenovo",
    "serial": "ABC123456",
    "modelo": "ThinkPad X1",
    "responsable": "Juan Pérez",
    "ciudad": "Bogotá",
    "estado": "Bueno"
}
```

#### 6. Ejecutar Cruce de Información

```http
POST http://localhost:8080/api/inventarios/{id}/cruce
Authorization: Bearer {{jwt_token}} (administrador)
```

**Respuesta:**
```json
{
    "inventarioId": 1,
    "fechaCruce": "2026-06-03T18:00:00Z",
    "resumen": {
        "total": 150,
        "cruceNormal": 120,
        "editado": 15,
        "sobrante": 10,
        "faltante": 5
    },
    "activos": [
        {
            "idActivo": "ACT-001",
            "marca": "Lenovo",
            "estado": "CRUCE NORMAL",
            "camposModificados": []
        },
        {
            "idActivo": "ACT-002",
            "marca": "HP",
            "estado": "EDITADO",
            "camposModificados": ["marca", "serial"]
        }
    ]
}
```

#### 7. Descargar Excel del Cruce

```http
GET http://localhost:8080/api/inventarios/{id}/cruce/excel
Authorization: Bearer {{jwt_token}}
```

Descarga un archivo Excel con:
- Todos los activos procesados
- Estado del cruce (CRUCE NORMAL, EDITADO, SOBRANTE, FALTANTE)
- Campos modificados para activos editados
- Colores distintivos por estado

---

## 🔐 Usuarios Preconfigurados

La aplicación incluye 2 usuarios de prueba:

### Administrador
- **Usuario:** `admin@rti.com.co`
- **Contraseña:** `Admin123!`
- **Permisos:** Gestión completa del sistema

### Inspector
- **Usuario:** `carlos.jimenez@rti.com.co`
- **Contraseña:** `Inspector123!`
- **Permisos:** Registro de activos en campo

---

## 📊 Estados del Cruce

El sistema clasifica cada activo en uno de 4 estados:

| Estado | Descripción | Color en Excel |
|--------|-------------|----------------|
| **CRUCE NORMAL** | Existe en ambas fuentes y todos los campos coinciden | Verde |
| **EDITADO** | Existe en ambas fuentes pero con diferencias | Amarillo |
| **SOBRANTE** | Solo registrado por inspector (no estaba en carga inicial) | Naranja |
| **FALTANTE** | Cargado por administrador pero no encontrado por inspector | Azul |

---

## 🗄️ Base de Datos

### Conexión MySQL (Aiven Cloud)

La aplicación está configurada para conectarse a:

```
Host: mysql-4ce7272-rtisas-69cd.k.aivencloud.com
Port: 21246
Database: defaultdb
User: avnadmin
SSL: REQUIRED
```

### Tablas Creadas

- `usuarios` - Usuarios del sistema con roles
- `centros_costos` - Centros de costos
- `inventarios` - Inventarios de activos
- `activos` - Activos con origen (ADMINISTRADOR/INSPECTOR)
- `resultados_cruce` - Resumen de cruces ejecutados
- `detalle_cruce` - Detalle de cada activo en el cruce
- `flyway_schema_history` - Control de migraciones

### Datos Iniciales

Se crean automáticamente:
- 2 usuarios (administrador e inspector)
- 3 centros de costos de ejemplo

---

## 🧪 Tests Implementados

### CruceInventarioServiceTest

✅ **5 tests funcionando:**

1. `debeClasificarComoCruceNormal()` - Valida coincidencia exacta
2. `debeClasificarComoEditado()` - Detecta diferencias en campos
3. `debeClasificarComoSobrante()` - Identifica activos adicionales
4. `debeClasificarComoFaltante()` - Detecta activos no encontrados
5. `debeProcesarCruceCompleto()` - Valida múltiples estados simultáneos

**Ejecutar:**
```bash
mvn test -Dtest=CruceInventarioServiceTest
```

---

## 📁 Estructura del Proyecto

```
Backend/
├── src/main/java/com/rti/inventarios/
│   ├── InventariosRtiApplication.java
│   ├── config/              # Configuración (Security, JWT, CORS)
│   ├── controller/          # Controllers REST
│   ├── service/             # Lógica de negocio
│   ├── repository/          # Repositorios JPA
│   ├── model/
│   │   ├── entity/          # Entidades JPA
│   │   ├── dto/             # DTOs para API
│   │   └── enums/           # Enumeraciones
│   └── exception/           # Manejo de excepciones
│
├── src/main/resources/
│   ├── application.yml      # Configuración principal
│   ├── application-dev.yml  # Configuración desarrollo
│   └── db/migration/        # Scripts Flyway
│
├── src/test/java/           # Tests unitarios
│
├── pom.xml                  # Dependencias Maven
└── InventariosRTI.postman_collection.json
```

---

## 🔧 Tecnologías Utilizadas

- **Java 17 LTS**
- **Spring Boot 3.2.5**
  - Spring Web (REST)
  - Spring Security + JWT
  - Spring Data JPA
  - Spring Validation
- **MySQL 8.x** (Aiven Cloud)
- **Apache POI 5.2.5** (Excel)
- **Flyway** (Migraciones)
- **Lombok** (Reducción de boilerplate)
- **SpringDoc OpenAPI 2.5.0** (Swagger)
- **JUnit 5 + Mockito** (Tests)

---

## 📝 Endpoints Principales

### Autenticación
- `POST /api/auth/login` - Login y obtención de JWT

### Usuarios (Administrador)
- `GET /api/usuarios` - Listar usuarios
- `POST /api/usuarios` - Crear usuario
- `PUT /api/usuarios/{id}` - Actualizar usuario
- `DELETE /api/usuarios/{id}` - Eliminar usuario

### Inventarios
- `GET /api/inventarios` - Listar inventarios
- `POST /api/inventarios` - Crear inventario
- `GET /api/inventarios/{id}` - Obtener inventario
- `GET /api/inventarios/validar/{codigo}` - Validar inventario (Inspector)

### Carga Excel (Administrador)
- `GET /api/inventarios/{id}/plantilla` - Descargar plantilla
- `POST /api/inventarios/{id}/cargar-excel` - Subir Excel
- `GET /api/inventarios/{id}/activos-administrador` - Ver activos cargados

### Activos (Inspector)
- `POST /api/activos` - Registrar activo
- `GET /api/activos/{codigo}` - Buscar por código
- `GET /api/activos/barcode/{barcode}` - Buscar por barcode
- `GET /api/inventarios/{id}/activos-inspector` - Ver activos registrados

### Cruce (Administrador)
- `POST /api/inventarios/{id}/cruce` - Ejecutar cruce
- `GET /api/inventarios/{id}/activos-cruzados` - Ver resultado
- `GET /api/inventarios/{id}/cruce/excel` - Descargar Excel del cruce

### Centros de Costos
- `GET /api/centros/{codigo}` - Buscar centro por código

---

## 🐛 Solución de Problemas

### Error de Conexión a MySQL

Si ves errores de conexión, verifica:
1. Conexión a Internet activa
2. Credenciales correctas en `application-dev.yml`
3. Firewall no bloqueando puerto 21246

### Error "Port 8080 already in use"

Si el puerto está ocupado:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Cambiar puerto en application.yml
server:
  port: 8081
```

### Tests Fallan

Si los tests fallan, asegúrate de:
1. Tener H2 en las dependencias de test
2. Archivo `application.yml` en `src/test/resources`

---

## 📧 Contacto y Soporte

Para reportar problemas o sugerencias, contacta al equipo de desarrollo de RTI.

---

## 🎉 ¡Listo para Usar!

El backend está completamente funcional y documentado. Puedes:

1. ✅ Probar todos los endpoints con Swagger o Postman
2. ✅ Crear inventarios y cargar activos
3. ✅ Registrar activos como inspector
4. ✅ Ejecutar cruces y generar reportes Excel
5. ✅ Integrar con los frontends Angular

**¡Feliz desarrollo! 🚀**
