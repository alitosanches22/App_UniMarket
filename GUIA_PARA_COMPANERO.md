# Guia para correr UniMarket en otra computadora

Esta guia es para abrir el proyecto en la computadora de un companero y correr la app desde Android Studio.

## 1. Programas necesarios

Instalar:

- Android Studio.
- JDK incluido con Android Studio.
- Node.js LTS: https://nodejs.org
- PostgreSQL con pgAdmin 4.
- Git.

## 2. Descargar el proyecto

Desde PowerShell o Git Bash:

```powershell
git clone URL_DEL_REPOSITORIO
cd Aplicacion_UniMarket
```

Tambien se puede descargar el ZIP desde GitHub, descomprimirlo y abrir esa carpeta.

## 3. Abrir en Android Studio

1. Abrir Android Studio.
2. Seleccionar `Open`.
3. Elegir la carpeta `Aplicacion_UniMarket`.
4. Esperar a que termine `Gradle Sync`.
5. Si Android Studio pide instalar SDK o Gradle, aceptar.
6. Si pide elegir JDK, seleccionar el JDK incluido con Android Studio o JDK 21.

No copiar `local.properties`; Android Studio lo crea solo en cada computadora.

## 4. Crear la base de datos

En pgAdmin:

1. Crear una base llamada `unimarket_db`.
2. Abrir el archivo:

```text
database/unimarket_postgresql.sql
```

3. Ejecutarlo conectado a la base `unimarket_db`.

Usuarios de prueba:

```text
Correo: alejandra.mendoza@universidad.edu
Contrasena: 123456
```

## 5. Configurar el backend

Entrar a la carpeta:

```text
backend
```

Copiar:

```text
.env.example
```

y pegarlo como:

```text
.env
```

Editar `backend/.env` y cambiar `TU_PASSWORD` por la contrasena real de PostgreSQL:

```env
PORT=3000
DATABASE_URL=postgres://postgres:TU_PASSWORD@localhost:5432/unimarket_db
```

## 6. Encender el backend

Dar doble clic a:

```text
backend/iniciar_backend.bat
```

La primera vez puede instalar dependencias con `npm install`. Dejar esa ventana abierta.

Si aparece que `node` o `npm` no existen, instalar Node.js LTS, cerrar la ventana y abrir el `.bat` otra vez.

Para probar en el navegador de la computadora:

```text
http://localhost:3000/api/salud
```

Debe responder algo como:

```json
{"mensaje":"API UniMarket funcionando."}
```

## 7. Correr la app

En Android Studio:

1. Crear o abrir un emulador.
2. Presionar `Run`.
3. Iniciar sesion con:

```text
Correo: alejandra.mendoza@universidad.edu
Contrasena: 123456
```

En emulador, la app usa:

```text
http://10.0.2.2:3000
```

Eso apunta al backend que esta corriendo en la computadora.

## 8. Si algo falla

- Si falla `npm`: reinstalar Node.js LTS y volver a abrir PowerShell/Android Studio.
- Si Android Studio no reconoce el JDK: ir a `File > Settings > Build, Execution, Deployment > Build Tools > Gradle` y elegir el JDK de Android Studio.
- Si falla el login: revisar que el backend este abierto y que el script SQL se haya ejecutado.
- Si falla PostgreSQL: revisar la contrasena en `backend/.env`.
- Si Android Studio marca errores de `databinding`: hacer `Build > Clean Project` y luego `Build > Rebuild Project`.
- Si no descarga dependencias de Android: revisar internet y aceptar la instalacion de SDK que Android Studio sugiera.
