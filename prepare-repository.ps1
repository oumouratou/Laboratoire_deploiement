$ErrorActionPreference = 'Stop'

function Invoke-Robocopy {
    param(
        [Parameter(Mandatory = $true)][string]$Source,
        [Parameter(Mandatory = $true)][string]$Target,
        [Parameter(Mandatory = $true)][string[]]$Options
    )

    if (-not (Test-Path $Source)) {
        throw "Source introuvable: $Source"
    }

    & robocopy $Source $Target @Options | Out-Null
    $code = $LASTEXITCODE
    if ($code -ge 8) {
        throw "Robocopy a echoue (code $code) pour $Source -> $Target"
    }
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourceRoot = Split-Path -Parent $root

$springSource = Join-Path $sourceRoot 'Backend_deployement\backend_labo2'
$vueSource = Join-Path $sourceRoot 'Frontend2 -security\Stage_frontend'
$flaskSource = Join-Path $vueSource 'chatbot-api'

$springTarget = Join-Path $root 'spring-boot'
$vueTarget = Join-Path $root 'vue-js'
$flaskTarget = Join-Path $root 'flask'

New-Item -ItemType Directory -Force -Path $springTarget, $vueTarget, $flaskTarget | Out-Null

Invoke-Robocopy -Source $springSource -Target $springTarget -Options @('/MIR','/XD','target','.git','.github','.vscode','/XF','*.log')
Invoke-Robocopy -Source $vueSource -Target $vueTarget -Options @('/MIR','/XD','node_modules','dist','.git','.github','.vscode','backend','chatbot-api','/XF','*.log')
Invoke-Robocopy -Source $flaskSource -Target $flaskTarget -Options @('/MIR','/XD','venv','.venv','__pycache__','/XF','*.log')

Set-Location $root
if (-not (Test-Path (Join-Path $root '.git'))) {
    git init
}

git add .
Write-Host 'Copie terminee. Verifie spring-boot, vue-js et flask puis commit/push.'
