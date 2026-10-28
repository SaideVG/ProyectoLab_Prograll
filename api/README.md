# SIGELAB API

API REST que conecta la app móvil (`../movil`) con la misma base de datos `SigelabDB`
que usa el sistema de escritorio.

```
Escritorio (Swing) ──JDBC──────────────────────────┐
                                                    ├──► SQL Server (SigelabDB)
App móvil (Flutter) ──HTTP──► API (Spring Boot) ───┘
```

- Java 21, Spring Boot 4.1, `mssql-jdbc` y `jbcrypt` en las mismas versiones del escritorio.
- Capas iguales al escritorio: `controller` → `service` → `repository` → SQL Server.
- Es **de consulta**: la app no registra préstamos, devoluciones ni mantenimientos.
  Lo único que escribe es la Auditoría de cada inicio de sesión (RN-18).

## Ejecutar

La conexión se toma de las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`, igual que el escritorio.

- **Desde IntelliJ**: clic derecho en `api/pom.xml` → *Add as Maven Project*. Luego,
  arriba a la derecha, elegir **SIGELAB API** y presionar ▶.
- **Desde una terminal** (en la carpeta `api`): `.\iniciar_api.ps1`

Para comprobar que funciona: abrir <http://localhost:8080/api/estado> en el navegador.

## Usar la app desde cualquier lugar (túnel)

Con la API encendida, en otra terminal (carpeta `api`): `.\iniciar_tunel.ps1`

- Usa `cloudflared` (en esta PC: `C:\dev\cloudflared\cloudflared.exe`) para dar una dirección pública
  `https://….trycloudflare.com` que llega a la API de esta laptop. No hay que abrir puertos.
- El script muestra la dirección y la copia al portapapeles. En el celular: login → **Servidor** → pegar → Guardar.
- La dirección **cambia** cada vez que se abre el túnel, y deja de funcionar si la laptop se apaga o se suspende.
- Solo se publica la API (HTTP), nunca SQL Server.

## Rutas

Todas, menos el login y el estado, piden el encabezado `Authorization: Bearer <token>`.

| Método | Ruta | Devuelve |
|---|---|---|
| GET  | `/api/estado` | Si la API y la base de datos responden |
| POST | `/api/auth/login` | `{"usuario", "password"}` → token y datos del usuario |
| GET  | `/api/auth/yo` | Usuario dueño del token |
| POST | `/api/auth/logout` | Cierra la sesión |
| GET  | `/api/dashboard` | Cantidades del menú principal |
| GET  | `/api/equipos?estado=&buscar=` | Equipos (filtros opcionales) |
| GET  | `/api/equipos/{codigo}` | Ficha del equipo con préstamos y mantenimientos (lo que se ve al escanear su QR) |
| GET  | `/api/prestamos?filtro=ACTIVOS\|VENCIDOS\|FINALIZADOS` | Préstamos |
| GET  | `/api/prestamos/{id}` | Préstamo con sus equipos |
| GET  | `/api/mantenimientos?soloEnProceso=true` | Mantenimientos |

Los errores responden `{"mensaje": "..."}` con el código HTTP correspondiente
(400 regla de negocio, 401 sin sesión, 403 sin permiso, 404 no existe).

## Seguridad

- La contraseña se valida con BCrypt contra la tabla `Usuario`; se aplica RN-01 (usuario ACTIVO).
- La validación facial (RN-02) se queda en el escritorio: la app entra con usuario y contraseña.
- El usuario de prueba `admin/admin` del escritorio **no** funciona en la API.
- Después de 5 contraseñas incorrectas seguidas, ese usuario queda bloqueado 15 minutos en la app
  (`sigelab.login.max-intentos` y `sigelab.login.minutos-bloqueo`); responde HTTP 429.
  El escritorio no se ve afectado.
- Los tokens viven 12 horas (`sigelab.sesion.horas`) y se guardan en memoria: si la API se reinicia,
  hay que volver a iniciar sesión.
- Para publicarla en internet hace falta HTTPS y un usuario de SQL Server propio de la API
  (no `sa`) con permisos de solo lectura más `INSERT` en `Auditoria`.
