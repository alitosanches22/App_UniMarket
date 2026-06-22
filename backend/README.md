# UniMarket API REST

Backend para conectar la aplicacion Android UniMarket con PostgreSQL.

## 1. Crear la base de datos

En PostgreSQL crea una base llamada `unimarket_db` y ejecuta:

```powershell
psql -U postgres -d unimarket_db -f "..\database\unimarket_postgresql.sql"
```

Tambien puedes abrir `database/unimarket_postgresql.sql` desde pgAdmin y ejecutarlo en la base `unimarket_db`.

## 2. Configurar variables

Copia `.env.example` como `.env`:

```powershell
copy .env.example .env
```

Edita `.env` con tu usuario y contrasena real de PostgreSQL:

```env
PORT=3000
DATABASE_URL=postgres://postgres:tu_password@localhost:5432/unimarket_db
```

## 3. Instalar y correr

Primero instala Node.js LTS desde:

```text
https://nodejs.org
```

Despues cierra y vuelve a abrir PowerShell o Android Studio para que `node` y `npm` queden disponibles.

En este proyecto ya puedes iniciar el backend con doble clic:

```text
backend/iniciar_backend.bat
```

El archivo `.bat` instala dependencias si falta `node_modules` y luego levanta la API. Deja esa ventana abierta mientras usas la app.

Si prefieres hacerlo manual:

```powershell
npm install
npm run dev
```

Si `npm` no funciona, reinstala Node.js LTS y vuelve a abrir la terminal.

La API queda en:

```text
http://localhost:3000
```

Para probar:

```text
GET http://localhost:3000/api/salud
GET http://localhost:3000/api/categorias
GET http://localhost:3000/api/productos
```

## 4. URL desde Android

Si corres la app en el emulador de Android Studio, `localhost` del computador se usa como:

```text
http://10.0.2.2:3000
```

Si usas un celular fisico conectado por USB, puedes ejecutar este comando desde Android Studio Terminal o PowerShell:

```powershell
adb reverse tcp:3000 tcp:3000
```

La app tambien tiene configurada la ruta `http://127.0.0.1:3000` para ese caso.
