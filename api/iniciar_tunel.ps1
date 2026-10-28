# ============================================================
# Publica la API de SIGELAB en internet con un tunel de Cloudflare
# ------------------------------------------------------------
# Sirve para que la app movil funcione desde cualquier lugar
# (otra red Wi-Fi o datos moviles) mientras esta laptop este encendida.
#
# Requisitos: Docker con SQL Server y la API encendidos (.\iniciar_api.ps1).
# Muestra la direccion publica (https://....trycloudflare.com) y la copia
# al portapapeles para enviarla a quien use la app.
# La direccion CAMBIA cada vez que se ejecuta este script.
#
# Uso, desde la carpeta api:   .\iniciar_tunel.ps1
# Para cerrar el tunel:        Ctrl + C
# ============================================================

$ErrorActionPreference = 'Stop'

$cloudflared = 'C:\dev\cloudflared\cloudflared.exe'
if (-not (Test-Path $cloudflared)) {
    $enPath = Get-Command cloudflared -ErrorAction SilentlyContinue
    if (-not $enPath) {
        throw 'No se encontro cloudflared. Instalelo con:  winget install --id Cloudflare.cloudflared  (y abra una terminal nueva)'
    }
    $cloudflared = $enPath.Source
}

# Mismo puerto que la API (variable PORT; si no existe, 8080).
$puerto = if ($env:PORT) { $env:PORT } else { '8080' }
$api = "http://localhost:$puerto"

# El tunel solo sirve si la API ya contesta.
try {
    $estado = Invoke-RestMethod "$api/api/estado" -TimeoutSec 5
} catch {
    throw "La API no esta encendida en $api. Ejecute primero .\iniciar_api.ps1"
}
if ($estado.baseDeDatos -ne 'conectada') {
    Write-Warning 'La API responde, pero no puede conectarse a SQL Server. Revise que Docker este encendido.'
}

$registro = Join-Path $env:TEMP 'sigelab_tunel.log'
Remove-Item $registro -ErrorAction SilentlyContinue

Write-Host 'Abriendo el tunel...'
$proceso = Start-Process -FilePath $cloudflared `
    -ArgumentList 'tunnel', '--no-autoupdate', '--url', $api `
    -NoNewWindow -PassThru -RedirectStandardError $registro

try {
    # cloudflared escribe la direccion publica en su registro unos segundos despues de arrancar.
    $direccion = $null
    for ($i = 0; $i -lt 60 -and -not $direccion; $i++) {
        Start-Sleep -Seconds 1
        if ($proceso.HasExited) { break }
        $linea = Select-String -Path $registro -Pattern 'https://(?!api\.)[a-z0-9-]+\.trycloudflare\.com' -ErrorAction SilentlyContinue |
            Select-Object -First 1
        if ($linea) { $direccion = $linea.Matches[0].Value }
    }
    if (-not $direccion) {
        Get-Content $registro -Tail 20 -ErrorAction SilentlyContinue
        throw 'No se pudo abrir el tunel. Revise la conexion a internet.'
    }

    Set-Clipboard $direccion
    Write-Host ''
    Write-Host '  Direccion para la app (ya esta copiada en el portapapeles):'
    Write-Host "  $direccion" -ForegroundColor Cyan
    Write-Host ''
    Write-Host '  En el celular: login > Servidor > pegar la direccion > Guardar.'
    Write-Host '  Deje esta ventana abierta. Para cerrar el tunel: Ctrl + C'
    Write-Host ''

    Wait-Process -Id $proceso.Id
}
finally {
    if (-not $proceso.HasExited) { Stop-Process -Id $proceso.Id -Force }
}
