#Requires -Version 5.1
<#
.SYNOPSIS
    Запускает frontend dev server и создаёт HTTPS туннель через ngrok.
.DESCRIPTION
    Полезно для разработки frontend с HTTPS, когда backend уже запущен.
.NOTES
    Требуется:
    - Node.js и npm
    - ngrok (с настроенным authtoken)
.EXAMPLE
    .\scripts\start-frontend-https.ps1
#>

param(
    [string]$FrontendPort = "4200",
    [string]$BackendUrl = "https://your-ngrok-url.ngrok-free.app",
    [switch]$SkipBuild = $false
)

$ColorInfo = "Cyan"
$ColorSuccess = "Green"
$ColorWarning = "Yellow"
$ColorError = "Red"

function Write-Header($text) {
    Write-Host "`n========================================" -ForegroundColor $ColorInfo
    Write-Host $text -ForegroundColor $ColorInfo
    Write-Host "========================================`n" -ForegroundColor $ColorInfo
}

# Проверка ngrok
$ngrokPath = Get-Command ngrok -ErrorAction SilentlyContinue
if (-not $ngrokPath) {
    Write-Error "ngrok не найден в PATH"
    Write-Host "Установите ngrok:" -ForegroundColor $ColorWarning
    Write-Host "  winget install ngrok.ngrok"
    exit 1
}

Write-Header "Запуск Frontend с HTTPS"

# Обновление proxy.conf.json для ngrok
$proxyConfig = @{
    "/api" = @{
        target = $BackendUrl
        secure = $true
        changeOrigin = $true
    }
}

$proxyPath = "$PSScriptRoot\..\frontend\proxy.conf.json"
$proxyConfig | ConvertTo-Json -Depth 3 | Set-Content $proxyPath

Write-Success "Proxy настроен на: $BackendUrl"

# Запуск frontend dev server
Write-Header "Запуск Frontend Dev Server"
Set-Location "$PSScriptRoot\..\frontend"

$frontendProcess = Start-Process -FilePath "npm" `
    -ArgumentList "start" `
    -PassThru `
    -WindowStyle Hidden

Write-Success "Frontend dev server запущен"
Write-Host "Ожидание запуска..." -ForegroundColor $ColorInfo
Start-Sleep -Seconds 10

# Запуск ngrok для frontend
Write-Header "Запуск Ngrok для Frontend"
Write-Host "Ngrok запущен. HTTPS URL для Telegram Web App:`n" -ForegroundColor $ColorWarning

ngrok http $FrontendPort
