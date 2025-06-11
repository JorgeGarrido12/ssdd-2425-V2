-- Crear esquema y usarlo
CREATE SCHEMA IF NOT EXISTS ssdd;
USE ssdd;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(50) UNIQUE,
    password_hash TEXT,
    name TEXT,
    token TEXT,
    visits INT
);

-- Índice para búsquedas por email
CREATE INDEX user_email_idx ON users (email);

-- Usuario de prueba: PASSWORD = "admin"
INSERT INTO users VALUES (
    "dsevilla",
    "dsevilla@um.es",
    "21232f297a57a5a743894a0e4a801fc3",
    "diego",
    "TOKEN",
    0
);

-- Tabla de conversaciones
CREATE TABLE IF NOT EXISTS conversations (
    dialogue_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(50),
    status VARCHAR(20),
    next_url TEXT,
    end_url TEXT,
    created_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índice para acelerar consultas por usuario
CREATE INDEX idx_conversations_user_id ON conversations(user_id);

-- Tabla de prompts dentro de una conversación
CREATE TABLE IF NOT EXISTS prompts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dialogue_id VARCHAR(100),
    prompt TEXT,
    answer TEXT,
    timestamp BIGINT,
    FOREIGN KEY (dialogue_id) REFERENCES conversations(dialogue_id) ON DELETE CASCADE
);

-- Índice para acelerar búsquedas por conversación
CREATE INDEX idx_prompts_dialogue_id ON prompts(dialogue_id);

-- Tabla opcional de estadísticas de uso (recomendado)
CREATE TABLE IF NOT EXISTS usage_stats (
    user_id VARCHAR(50) PRIMARY KEY,
    total_prompts INT DEFAULT 0,
    total_conversations INT DEFAULT 0,
    last_access_timestamp BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);