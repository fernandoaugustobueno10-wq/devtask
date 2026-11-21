
$ErrorActionPreference = "Stop"

$RaizProjeto = Split-Path -Parent $PSScriptRoot
$PomBackend = Join-Path $RaizProjeto "backend\pom.xml"

if (-not (Test-Path -LiteralPath $PomBackend -PathType Leaf)) {
    Write-Error "Arquivo backend\pom.xml nao encontrado."
    exit 1
}

if (-not (Get-Command "java.exe" -ErrorAction SilentlyContinue)) {
    Write-Error "Java nao encontrado. Instale o JDK 17."
    exit 1
}

if (-not (Get-Command "mvn.cmd" -ErrorAction SilentlyContinue)) {
    Write-Error "Maven nao encontrado no PATH."
    exit 1
}

Write-Host ""
Write-Host "===================================="
Write-Host "         DevTask - Backend"
Write-Host "===================================="
Write-Host ""
Write-Host "Banco de dados local:"
Write-Host "Host: 127.0.0.1"
Write-Host "Porta: 5433"
Write-Host "Banco: devtask_db"
Write-Host "Usuario: devtask_app"
Write-Host ""

$env:DB_URL = "jdbc:postgresql://127.0.0.1:5433/devtask_db"
$env:DB_USERNAME = "devtask_app"
$env:FLYWAY_ENABLED = "false"
$env:PORT = "8080"
$env:SESSION_COOKIE_SECURE = "false"

$SenhaSegura = Read-Host "Digite a senha local do PostgreSQL" -AsSecureString

try {
    $env:DB_PASSWORD = [System.Net.NetworkCredential]::new(
        "",
        $SenhaSegura
    ).Password
}
finally {
    $SenhaSegura.Dispose()
    Remove-Variable SenhaSegura -ErrorAction SilentlyContinue
}

try {
    Write-Host ""
    Write-Host "Iniciando Spring Boot..."
    Write-Host ""

    & mvn.cmd -f $PomBackend spring-boot:run

    if ($LASTEXITCODE -ne 0) {
        throw "O backend terminou com erro."
    }
}
finally {
    Remove-Item Env:\DB_PASSWORD -ErrorAction SilentlyContinue
}
