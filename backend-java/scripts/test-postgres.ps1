param(
    [string]$PostgresBin = "C:\Program Files\PostgreSQL\17\bin",
    [int]$Port = 55433,
    [string]$MavenCommand = "",
    [string]$Repository = ""
)
$ErrorActionPreference = "Stop"
$backendPath = Split-Path -Parent $PSScriptRoot
$testRoot = Join-Path $backendPath "target"
$clusterPath = Join-Path $testRoot ("postgres-test-" + [guid]::NewGuid().ToString("N"))
$started = $false
New-Item -ItemType Directory -Path $testRoot -Force | Out-Null
if (-not $MavenCommand) { $MavenCommand = Join-Path $backendPath "mvnw.cmd" }
Push-Location $backendPath
try {
    # Fresh disposable cluster, loopback only. Does not use the existing PostgreSQL service.
    & (Join-Path $PostgresBin "initdb.exe") -D $clusterPath -U suit_test -A trust --encoding=UTF8 --locale=C
    if ($LASTEXITCODE -ne 0) { throw "Falha ao criar cluster de teste." }
    & (Join-Path $PostgresBin "pg_ctl.exe") -D $clusterPath -l (Join-Path $clusterPath "server.log") -o "-h 127.0.0.1 -p $Port" -w start
    if ($LASTEXITCODE -ne 0) { throw "Falha ao iniciar PostgreSQL temporário (porta ocupada?)." }
    $started = $true
    $mavenArguments = @("test", "-Dspring.datasource.url=jdbc:postgresql://127.0.0.1:$Port/postgres", "-Dspring.datasource.username=suit_test", "-Dspring.datasource.password=")
    if ($Repository) { $mavenArguments += "-Dmaven.repo.local=$Repository" }
    & $MavenCommand @mavenArguments
    if ($LASTEXITCODE -ne 0) { throw "Testes PostgreSQL falharam." }
} finally {
    if ($started) {
        & (Join-Path $PostgresBin "pg_ctl.exe") -D $clusterPath -m fast -w stop
    }
    Pop-Location
    Write-Host "Logs do cluster temporário preservados em $clusterPath"
}
