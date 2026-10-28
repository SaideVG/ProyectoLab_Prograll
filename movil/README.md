# SIGELAB móvil

App de Flutter que complementa al sistema de escritorio: consulta el estado del laboratorio
desde el celular y lee el código QR que el escritorio genera para cada equipo.

| Pantalla | Qué muestra |
|---|---|
| Inicio | Equipos por estado y préstamos activos y vencidos (como el menú del escritorio) |
| Equipos | Inventario con búsqueda y filtro por estado |
| Escanear | Lee el QR del equipo (o se escribe el código) y abre su ficha |
| Ficha del equipo | Datos, estado, historial de préstamos y de mantenimientos |
| Préstamos | Activos, vencidos (RN-17) y finalizados, con el detalle de cada equipo |
| Mantenimiento | Equipos en mantenimiento e historial |

La app **no** se conecta directo a SQL Server: habla con la API de `../api`, que es la que
usa la base de datos. Los préstamos, devoluciones y mantenimientos se registran en el escritorio.

## Estructura (`lib/`)

```
config/tema.dart        Colores del tema oscuro, los mismos de Tema.java del escritorio
models/                 Usuario, Dashboard, Equipo, Prestamo, Mantenimiento
services/api_service    Único lugar que llama a la API (como los Repository del escritorio)
services/sesion.dart    Usuario conectado, token y dirección del servidor
views/                  Una pantalla por archivo (login, inicio, equipos, escáner, ...)
widgets/componentes     Tarjetas, etiquetas de estado y carga de datos reutilizables
```

## Ejecutar

Requisitos: Flutter (en esta PC está en `C:\dev\flutter`) y la API encendida (`..\api\iniciar_api.ps1`).

```
cd movil
flutter pub get
flutter run            # elige el emulador, un celular conectado o Chrome
```

Dirección de la API (botón **Servidor** en el login):

- Emulador de Android (debug/profile): `http://10.0.2.2:8080` (ya viene así).
- Navegador de la computadora: `http://localhost:8080` (ya viene así).
- Celular real, desde cualquier lugar: la dirección del túnel (`..\api\iniciar_tunel.ps1`),
  por ejemplo `https://abc-def.trycloudflare.com`.
- Celular real en la misma red Wi-Fi: la IP de la computadora, por ejemplo `192.168.0.8:8080`.
  Windows debe tener esa red como *Privada* para permitir la conexión.

Si se escribe sin `http://` o `https://`, la app lo completa sola.

## Generar la APK para instalar en teléfonos

```
flutter build apk --release --split-per-abi
```

Se usa `build/app/outputs/flutter-apk/app-arm64-v8a-release.apk` (casi todos los teléfonos actuales).
La APK final no trae ninguna dirección: al abrirla, el login pide configurar el **Servidor**.
Para que ya venga con una dirección fija: `flutter build apk --release --dart-define=SERVIDOR=https://...`

En el teléfono: abrir la APK → permitir *Instalar apps desconocidas* → si Play Protect avisa,
*Instalar de todas formas* (la APK está firmada con la clave de prueba, no viene de Play Store).

## Nota para esta PC (OneDrive)

El proyecto está dentro de OneDrive, que bloquea los miles de archivos temporales de la compilación
de Android. Por eso `movil/build` es un acceso directo (junction) a `C:\dev\build\sigelab_movil`.
Si se borra, se vuelve a crear con PowerShell:

```
New-Item -ItemType Junction -Path build -Target C:\dev\build\sigelab_movil
```
