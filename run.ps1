# Builds and runs the advisor on Windows.
# Keys are read from the user's environment variables; nothing secret is stored in the project.
#   powershell -ExecutionPolicy Bypass -File run.ps1            (port 8080)
#   powershell -ExecutionPolicy Bypass -File run.ps1 -Port 8081
param([int]$Port = 8080, [switch]$Replay, [switch]$Record)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

foreach ($name in @("GEMINI_API_KEY", "ADVISOR_REP_PASSWORD")) {
    if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
        $value = [Environment]::GetEnvironmentVariable($name, "User")
        if ($value) { [Environment]::SetEnvironmentVariable($name, $value, "Process") }
    }
}
if ($Replay) {
    $env:ADVISOR_LLM_MODE = "replay"
    Write-Host "REPLAY mode: the assistant plays back recorded turns (demo/recorded-turns.jsonl), no Gemini call."
} elseif (-not $env:GEMINI_API_KEY) {
    Write-Host "GEMINI_API_KEY is not set: the chat is off, the form still works."
}
if ($Record) { $env:ADVISOR_LLM_RECORD_FILE = "demo/recorded-turns.jsonl" }

# Runtime data lives outside the source folder (and outside Documents, which Windows may protect).
if (-not $env:SPRING_DATASOURCE_URL) {
    $env:SPRING_DATASOURCE_URL = "jdbc:h2:file:" + (Join-Path $env:LOCALAPPDATA "car-advisor\db\car-advisor")
}

if (-not $env:JAVA_HOME) {
    $jdk = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -Filter "jdk-17*" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jdk) { $env:JAVA_HOME = $jdk.FullName }
}
# Runs from compiled classes (no jar to lock while antivirus scans it).
# Windows-ROOT: trust the certificates Windows trusts. Needed behind TLS-inspecting filters (e.g. NetFree);
# certificate checks stay fully on.
& .\mvnw.cmd -q spring-boot:run `
    "-Dspring-boot.run.jvmArguments=-Djavax.net.ssl.trustStoreType=Windows-ROOT" `
    "-Dspring-boot.run.arguments=--server.port=$Port"
