# ============================================================
# Inicia la API de SIGELAB en http://localhost:8080
# ------------------------------------------------------------
# Usa la misma base de datos que el sistema de escritorio. La
# conexion se toma de las variables DB_URL, DB_USER y DB_PASSWORD
# (si no existen, usa los valores de application.properties).
#
# Uso, desde la carpeta api:   .\iniciar_api.ps1
# Para detenerla:              Ctrl + C
# ============================================================

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

# La contrasena se guarda como variable de Windows (setx DB_PASSWORD ...), nunca en el codigo.
# Una terminal abierta antes del setx no la conoce: se lee directo de la configuracion del usuario.
if (-not $env:DB_PASSWORD) {
    $env:DB_PASSWORD = [Environment]::GetEnvironmentVariable('DB_PASSWORD', 'User')
}

# Maven necesita un JDK 21. Si JAVA_HOME no esta definido, se busca en las carpetas donde
# lo instalan IntelliJ, Eclipse Temurin (winget) y Oracle; si no, se usa el "java" del PATH.
if (-not $env:JAVA_HOME) {
    $carpetas = @("$env:USERPROFILE\.jdks", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Java")
    $jdk = $carpetas | ForEach-Object { Get-ChildItem $_ -Directory -ErrorAction SilentlyContinue } |
        Where-Object { $_.Name -match '21' -and (Test-Path "$($_.FullName)\bin\java.exe") } |
        Select-Object -First 1
    if ($jdk) {
        $env:JAVA_HOME = $jdk.FullName
    } elseif (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'No se encontro un JDK 21. Instale uno con:  winget install EclipseAdoptium.Temurin.21.JDK'
    }
}

$puerto = if ($env:PORT) { $env:PORT } else { '8080' }
Write-Host "JDK: $(if ($env:JAVA_HOME) { $env:JAVA_HOME } else { (Get-Command java).Source })"
Write-Host "Iniciando la API de SIGELAB en http://localhost:$puerto ..."

# El Maven Wrapper (mvnw) descarga Maven la primera vez: no hace falta instalarlo.
& .\mvnw.cmd -q spring-boot:run
