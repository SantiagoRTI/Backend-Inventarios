-- Insertar usuario administrador por defecto
-- Contraseña: Admin123! (BCrypt hash)
INSERT INTO usuarios (nombre, correo, contrasena, rol, activo) 
VALUES ('Administrador RTI', 'admin@rti.com.co', '$2a$10$rMqZhL6YJYYfQX1xKW9rJ.qBJK3z7xK5H5YD8L9wQ3n7R7z1Z8z8e', 'ADMINISTRADOR', TRUE);

-- Insertar inspector de prueba
-- Contraseña: Inspector123!
INSERT INTO usuarios (nombre, correo, contrasena, rol, activo) 
VALUES ('Carlos Jiménez', 'carlos.jimenez@rti.com.co', '$2a$10$rMqZhL6YJYYfQX1xKW9rJ.qBJK3z7xK5H5YD8L9wQ3n7R7z1Z8z8e', 'INSPECTOR', TRUE);

-- Insertar centros de costos de ejemplo
INSERT INTO centros_costos (codigo, nombre, ciudad, direccion, activo) VALUES
('014', 'Bulevar Niza', 'Bogotá', 'Calle 123 #45-67', TRUE),
('015', 'Centro Comercial Gran Estación', 'Bogotá', 'Carrera 60 #25-40', TRUE),
('016', 'Oficina Principal', 'Medellín', 'Calle 50 #48-20', TRUE);
