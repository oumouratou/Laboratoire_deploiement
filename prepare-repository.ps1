$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourceRoot = Split-Path -Parent $root

$springSource = Join-Path $sourceRoot 'Backend_deployement\backend_labo2'
$vueSource = Join-Path $sourceRoot 'Frontend2 -security\Stage_frontend'
$flaskSource = Join-Path $vueSource 'chatbot-api'

$springTarget = Join-Path $root 'spring-boot'
$vueTarget = Join-Path $root 'vue-js'
$flaskTarget = Join-Path $root 'flask'

New-Item -ItemType Directory -Force -Path $springTarget, $vueTarget, $flaskTarget | Out-Null

robocopy $springSource $springTarget /MIR /XD target .git .github .vscode /XF *.log | Out-Null
robocopy $vueSource $vueTarget /MIR /XD node_modules dist .git .github .vscode backend chatbot-api /XF *.log | Out-Null
robocopy $flaskSource $flaskTarget /MIR /XD venv .venv __pycache__ /XF *.log | Out-Null

Set-Location $root
if (-not (Test-Path (Join-Path $root '.git'))) {
    git init
}

git add .
Write-Host 'Repository prepared. Create the GitHub repository Laboratoire_deploiement, add the remote, then run git push -u origin main.'
