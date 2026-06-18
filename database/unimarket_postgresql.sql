-- UniMarket - PostgreSQL schema
-- Ejecutar primero conectado a la base de datos donde quieras crear las tablas.
-- Recomendado: crear una base llamada unimarket_db y luego correr este archivo.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    institutional_email VARCHAR(160) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    career VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    profile_photo_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id SMALLSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

INSERT INTO categories (name)
VALUES
    ('Libros'),
    ('Electronicos'),
    ('Laboratorio'),
    ('Tutorias'),
    ('Otros')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id SMALLINT NOT NULL REFERENCES categories(id),
    title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    condition VARCHAR(60) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'SOLD', 'HIDDEN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('IMAGE', 'VIDEO')),
    media_url TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS favorites (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seller_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_product_buyer_seller UNIQUE (product_id, buyer_id, seller_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (institutional_email);

CREATE INDEX IF NOT EXISTS idx_products_category
    ON products (category_id);

CREATE INDEX IF NOT EXISTS idx_products_seller
    ON products (seller_id);

CREATE INDEX IF NOT EXISTS idx_products_status
    ON products (status);

CREATE INDEX IF NOT EXISTS idx_products_title_search
    ON products USING gin (to_tsvector('spanish', title || ' ' || description));

CREATE INDEX IF NOT EXISTS idx_product_media_product
    ON product_media (product_id);

CREATE INDEX IF NOT EXISTS idx_favorites_user
    ON favorites (user_id);

CREATE INDEX IF NOT EXISTS idx_conversations_buyer
    ON conversations (buyer_id);

CREATE INDEX IF NOT EXISTS idx_conversations_seller
    ON conversations (seller_id);

CREATE INDEX IF NOT EXISTS idx_messages_conversation
    ON messages (conversation_id, created_at);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_products_updated_at ON products;
CREATE TRIGGER trg_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_conversations_updated_at ON conversations;
CREATE TRIGGER trg_conversations_updated_at
BEFORE UPDATE ON conversations
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Datos de prueba opcionales.
-- Puedes borrar esta seccion cuando conectemos el backend real.

INSERT INTO users (
    first_name,
    last_name,
    institutional_email,
    password_hash,
    career,
    phone
)
VALUES
    (
        'Alejandra',
        'Mendoza',
        'alejandra.mendoza@universidad.edu',
        crypt('123456', gen_salt('bf')),
        'Ingenieria en Sistemas',
        '0995552301'
    ),
    (
        'Daniel',
        'Ruiz',
        'daniel.ruiz@universidad.edu',
        crypt('123456', gen_salt('bf')),
        'Ingenieria Civil',
        '0981112233'
    ),
    (
        'Mariana',
        'Lopez',
        'mariana.lopez@universidad.edu',
        crypt('123456', gen_salt('bf')),
        'Administracion',
        '0972223344'
    )
ON CONFLICT (institutional_email) DO NOTHING;

INSERT INTO products (
    seller_id,
    category_id,
    title,
    description,
    price,
    condition
)
SELECT
    u.id,
    c.id,
    'Calculo de Stewart 8va edicion',
    'Libro en buen estado, con ejercicios marcados y resumenes utiles.',
    32.00,
    'Usado - bueno'
FROM users u
JOIN categories c ON c.name = 'Libros'
WHERE u.institutional_email = 'daniel.ruiz@universidad.edu'
AND NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.title = 'Calculo de Stewart 8va edicion'
    AND p.seller_id = u.id
);

INSERT INTO products (
    seller_id,
    category_id,
    title,
    description,
    price,
    condition
)
SELECT
    u.id,
    c.id,
    'Calculadora cientifica Casio fx-991',
    'Funciona perfecto para estadistica, fisica y algebra. Incluye estuche.',
    18.50,
    'Usado - excelente'
FROM users u
JOIN categories c ON c.name = 'Electronicos'
WHERE u.institutional_email = 'mariana.lopez@universidad.edu'
AND NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.title = 'Calculadora cientifica Casio fx-991'
    AND p.seller_id = u.id
);

INSERT INTO products (
    seller_id,
    category_id,
    title,
    description,
    price,
    condition
)
SELECT
    u.id,
    c.id,
    'Kit de laboratorio basico',
    'Gafas, bata talla M y guantes reutilizables para practicas de quimica.',
    24.99,
    'Nuevo'
FROM users u
JOIN categories c ON c.name = 'Laboratorio'
WHERE u.institutional_email = 'alejandra.mendoza@universidad.edu'
AND NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.title = 'Kit de laboratorio basico'
    AND p.seller_id = u.id
);
