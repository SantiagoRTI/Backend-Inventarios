-- Tabla de usuarios con roles (Administrador e Inspector)
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'INSPECTOR') NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    INDEX idx_correo (correo),
    INDEX idx_rol (rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de centros de costos
CREATE TABLE centros_costos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100),
    direccion VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    INDEX idx_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de inventarios
CREATE TABLE inventarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    estado VARCHAR(50) NOT NULL DEFAULT 'Ejecucion',
    inspector_id BIGINT,
    centro_costos_id BIGINT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    fecha_finalizacion DATETIME,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (inspector_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (centro_costos_id) REFERENCES centros_costos(id) ON DELETE SET NULL,
    INDEX idx_codigo (codigo),
    INDEX idx_inspector (inspector_id),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de activos con origen (ADMINISTRADOR o INSPECTOR)
CREATE TABLE activos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventario_id BIGINT NOT NULL,
    id_activo VARCHAR(100) NOT NULL,
    etiqueta VARCHAR(255),
    descripcion VARCHAR(500),
    marca VARCHAR(100),
    serial VARCHAR(100),
    modelo VARCHAR(100),
    responsable VARCHAR(255),
    ciudad VARCHAR(100),
    estado VARCHAR(50),
    origen ENUM('ADMINISTRADOR', 'INSPECTOR') NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (inventario_id) REFERENCES inventarios(id) ON DELETE CASCADE,
    INDEX idx_inventario_origen (inventario_id, origen),
    INDEX idx_id_activo (id_activo),
    INDEX idx_inventario_id_activo (inventario_id, id_activo),
    UNIQUE KEY uk_inventario_id_activo_origen (inventario_id, id_activo, origen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de resultados de cruce
CREATE TABLE resultados_cruce (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventario_id BIGINT NOT NULL,
    fecha_cruce DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_activos INT DEFAULT 0,
    cruce_normal INT DEFAULT 0,
    editados INT DEFAULT 0,
    sobrantes INT DEFAULT 0,
    faltantes INT DEFAULT 0,
    FOREIGN KEY (inventario_id) REFERENCES inventarios(id) ON DELETE CASCADE,
    INDEX idx_inventario (inventario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla detalle de cruces
CREATE TABLE detalle_cruce (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resultado_cruce_id BIGINT NOT NULL,
    id_activo VARCHAR(100) NOT NULL,
    etiqueta VARCHAR(255),
    descripcion VARCHAR(500),
    marca VARCHAR(100),
    serial VARCHAR(100),
    modelo VARCHAR(100),
    responsable VARCHAR(255),
    ciudad VARCHAR(100),
    estado_cruce ENUM('CRUCE_NORMAL', 'EDITADO', 'SOBRANTE', 'FALTANTE') NOT NULL,
    campos_modificados TEXT,
    FOREIGN KEY (resultado_cruce_id) REFERENCES resultados_cruce(id) ON DELETE CASCADE,
    INDEX idx_resultado_cruce (resultado_cruce_id),
    INDEX idx_id_activo (id_activo),
    INDEX idx_estado_cruce (estado_cruce)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
