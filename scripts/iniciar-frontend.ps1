
$ErrorActionPreference = "Stop"

$RaizProjeto = Split-Path -Parent $PSScriptRoot
$PastaFrontend = Join-Path $RaizProjeto "frontend"

$PackageJson = Join-Path $PastaFrontend "package.json"
$ProxyConfig = Join-Path $PastaFrontend "proxy.conf.json"
$NodeModules = Join-Path $PastaFrontend "node_modules"

if (-not (Test-Path -LiteralPath $PackageJson -PathType Leaf)) {
    Write-Error "Arquivo frontend\package.json nao encontrado."
    exit 1
}

if (-not (Test-Path -LiteralPath $ProxyConfig -PathType Leaf)) {
    Write-Error "Arquivo frontend\proxy.conf.json nao encontrado."
    exit 1
}

if (-not (Get-Command "node.exe" -ErrorAction SilentlyContinue)) {
    Write-Error "Node.js nao encontrado."
    exit 1
}

if (-not (Get-Command "npm.cmd" -ErrorAction SilentlyContinue)) {
    Write-Error "npm nao encontrado."
    exit 1
}

Write-Host ""
Write-Host "===================================="
Write-Host "         DevTask - Frontend"
Write-Host "===================================="
Write-Host ""

Push-Location $PastaFrontend

try {
    if (-not (Test-Path -LiteralPath $NodeModules)) {
        Write-Host "Instalando dependencias..."
        & npm.cmd ci

        if ($LASTEXITCODE -ne 0) {
            throw "Falha na instalacao das dependencias."
        }
    }

    Write-Host ""
    Write-Host "Iniciando Angular..."
    Write-Host "Acesse: http://localhost:4200"
    Write-Host ""

    & npm.cmd start -- --proxy-config proxy.conf.json

    if ($LASTEXITCODE -ne 0) {
        throw "O frontend terminou com erro."
    }
}
finally {
    Pop-Location
}
