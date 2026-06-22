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

En este proyecto ya puedes iniciar el backend con doble clic:

```text
backend/iniciar_backend.bat
```

Deja esa ventana abierta mientras usas la app. Si tienes Node.js instalado normalmente, tambien puedes usar:

```powershell
npm install
npm run dev
```

Si `npm` no funciona en PowerShell, usa el archivo `iniciar_backend.bat`.

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
