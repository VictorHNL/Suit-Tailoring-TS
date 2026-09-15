param([string]$Profile = "dev")
$ErrorActionPreference = "Stop"
$backendPath = Split-Path -Parent $PSScriptRoot
Push-Location $backendPath
try {
    if (Test-Path -LiteralPath ".env") {
        foreach ($line in Get-Content -LiteralPath ".env") {
            if ($line -match '^\s*([A-Z_][A-Z0-9_]*)=(.*)$') {
                [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim(), "Process")
            }
        }
    }
    if (-not $env:DB_PASSWORD) { throw "Defina DB_PASSWORD em .env ou no ambiente." }
    $env:SPRING_PROFILES_ACTIVE = $Profile
    if (($Profile -split ",") -contains "worker") { $env:APP_JOBS_ENABLED = "true" }
    & .\mvnw.cmd spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw "Spring Boot encerrou com erro." }
} finally {
    Pop-Location
}
