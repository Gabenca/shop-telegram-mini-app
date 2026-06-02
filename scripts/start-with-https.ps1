#Requires -Version 5.1
<#
.SYNOPSIS
    Запускает backend и создаёт HTTPS туннель через ngrok.
.DESCRIPTION
    Автоматически запускает Spring Boot backend и ngrok для получения HTTPS URL.
    Полученный URL можно использовать в Telegram Mini App.
.NOTES
    Требуется:
    - JDK 21 (путь указывается в JAVA_HOME)
    - ngrok (с настроенным authtoken)
    - Gradle wrapper в backend/
.EXAMPLE
    .\scripts\start-with-https.ps1
.EXAMPLE
    .\scripts\start-with-https.ps1 -NgrokDomain your-domain.ngrok-free.app
#>

param(
    [string]$JavaHome = "C:\Users\$env:USERNAME\.jdks\ms-21.0.11",
    [string]$BackendPort = "8080",
    [string]$NgrokDomain = "",
    [switch]$SkipBuild = $false
)

# Цвета для вывода
$ColorInfo = "Cyan"
$ColorSuccess = "Green"
$ColorWarning = "Yellow"
$ColorError = "Red"

function Write-Header($text) {
    Write-Host "`n========================================" -ForegroundColor $ColorInfo
    Write-Host $text -ForegroundColor $ColorInfo
    Write-Host "========================================`n" -ForegroundColor $ColorInfo
}

function Write-Success($text) {
    Write-Host "✅ $text" -ForegroundColor $ColorSuccess
}

function Write-Warning($text) {
    Write-Host "⚠️  $text" -ForegroundColor $ColorWarning
}

function Write-Error($text) {
    Write-Host "❌ $text" -ForegroundColor $ColorError
}

# Проверка JDK
Write-Header "Проверка окружения"

if (-not (Test-Path $JavaHome)) {
    Write-Error "JDK не найден по пути: $JavaHome"
    Write-Host "Установите JDK 21 или укажите правильный путь через параметр -JavaHome"
    exit 1
}

Write-Success "JDK найден: $JavaHome"
$env:JAVA_HOME = $JavaHome

# Проверка ngrok
$ngrokPath = Get-Command ngrok -ErrorAction SilentlyContinue
if (-not $ngrokPath) {
    Write-Error "ngrok не найден в PATH"
    Write-Host "Установите ngrok:"
    Write-Host "  1. winget install ngrok.ngrok"
    Write-Host "  2. Или скачайте с https://ngrok.com/download"
    Write-Host "  3. Зарегистрируйтесь на https://ngrok.com и выполните: ngrok config add-authtoken <TOKEN>"
    exit 1
}

Write-Success "ngrok найден: $($ngrokPath.Source)"

# Проверка authtoken
try {
    $ngrokConfig = ngrok config check 2>&1
    Write-Success "ngrok настроен"
} catch {
    Write-Warning "Проверьте настройку ngrok authtoken"
    Write-Host "Выполните: ngrok config add-authtoken <YOUR_TOKEN>"
}

# Сборка backend (если не пропущена)
if (-not $SkipBuild) {
    Write-Header "Сборка Backend"
    Set-Location "$PSScriptRoot\..\backend"
    
    try {
        .\gradlew.bat bootJar -x test
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle build failed"
        }
        Write-Success "Backend собран"
    } catch {
        Write-Error "Ошибка сборки backend"
        exit 1
    }
} else {
    Write-Warning "Сборка пропущена (--SkipBuild)"
}

# Запуск backend
Write-Header "Запуск Backend"
Set-Location "$PSScriptRoot\..\backend"

$backendProcess = Start-Process -FilePath ".\gradlew.bat" `
    -ArgumentList "bootRun" `
    -PassThru `
    -WindowStyle Hidden

Write-Success "Backend запущен (PID: $($backendProcess.Id))"
Write-Host "Ожидание запуска Spring Boot..." -ForegroundColor $ColorInfo

# Ожидание запуска Spring Boot
$maxAttempts = 30
$attempt = 0
$backendReady = $false

while ($attempt -lt $maxAttempts -and -not $backendReady) {
    Start-Sleep -Seconds 1
    $attempt++
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$BackendPort/actuator/health" -UseBasicParsing -ErrorAction SilentlyContinue -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
        }
    } catch {
        # Ещё не готов
    }
    
    Write-Host "." -NoNewline -ForegroundColor $ColorInfo
}

Write-Host ""

if (-not $backendReady) {
    Write-Warning "Не удалось дождаться запуска backend"
    Write-Host "Продолжаем запуск ngrok..."
} else {
    Write-Success "Backend готов (http://localhost:$BackendPort)"
}

# Запуск ngrok
Write-Header "Запуск Ngrok HTTPS Tunnel"

$ngrokArgs = "http", $BackendPort
if ($NgrokDomain) {
    $ngrokArgs += "--domain", $NgrokDomain
}

Write-Host "Запуск: ngrok $($ngrokArgs -join ' ')" -ForegroundColor $ColorInfo
Write-Host "Ngrok запущен. Полученный HTTPS URL используйте в Telegram Bot.`n" -ForegroundColor $ColorWarning

# Запуск ngrok в том же окне (он будет блокировать)
try {
    ngrok @ngrokArgs
} finally {
    # При закрытии ngrok, остановить backend
    Write-Host "`nОстановка backend..." -ForegroundColor $ColorInfo
    if ($backendProcess -and -not $backendProcess.HasExited) {
        Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue
        Write-Success "Backend остановлен"
    }
}
