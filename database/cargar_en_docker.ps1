# Crea la base SigelabDB (esquema + datos de prueba) dentro del SQL Server que corre en Docker.
#
# Uso desde la raíz del proyecto:
#     .\database\cargar_en_docker.ps1
#     .\database\cargar_en_docker.ps1 -Contenedor otroNombre
#
# Si todavía no existe ningún contenedor de SQL Server, se crea uno así:
#     docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=<contraseña>" -p 1433:1433 --name sqlserver -d mcr.microsoft.com/mssql/server:2022-latest
#
# La contraseña de "sa" se lee del propio contenedor; debe ser la misma que usa DatabaseConnection.java.

param(
    [string]$Contenedor = "sqlserver"
)

$sqlcmd = 'SQLCMDPASSWORD=${MSSQL_SA_PASSWORD:-$SA_PASSWORD} /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -C -b -x '

docker start $Contenedor | Out-Null
if ($LASTEXITCODE -ne 0) { throw "No se pudo iniciar el contenedor '$Contenedor'. ¿Docker Desktop está abierto?" }

# SQL Server tarda unos segundos en aceptar conexiones después de iniciar el contenedor.
$listo = $false
foreach ($intento in 1..30) {
    docker exec $Contenedor bash -c ($sqlcmd + "-Q 'SELECT 1'") *> $null
    if ($LASTEXITCODE -eq 0) { $listo = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $listo) { throw "SQL Server no respondió dentro del contenedor '$Contenedor'." }

foreach ($script in "01_esquema.sql", "02_datos_prueba.sql") {
    Write-Host "Ejecutando $script ..."
    docker cp (Join-Path $PSScriptRoot $script) "${Contenedor}:/tmp/$script"
    docker exec $Contenedor bash -c ($sqlcmd + "-i /tmp/$script")
    if ($LASTEXITCODE -ne 0) { throw "Falló $script. Si la base ya existía, bórrela con el bloque opcional de 01_esquema.sql." }
}

Write-Host "SigelabDB lista en localhost:1433."
