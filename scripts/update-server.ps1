# =============================================================================
# update-server.ps1 - Esther Server 모드 업데이트 스크립트 (Windows)
#
# 사용법:
#   .\update-server.ps1 -ModsDir "C:\minecraft\server\mods"
#
# GitHub API를 통해 최신 릴리스의 JAR을 다운로드하여 서버 mods 폴더에 배치합니다.
# 기존 estherserver JAR은 자동으로 제거됩니다.
# =============================================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$ModsDir
)

$ErrorActionPreference = "Stop"
$Repo = "team-for-choco/esther-server"

if (-not (Test-Path $ModsDir)) {
    Write-Host "오류: '$ModsDir' 폴더가 존재하지 않습니다." -ForegroundColor Red
    exit 1
}

Write-Host "최신 릴리스 확인 중..."

$headers = @{}
if ($env:GITHUB_TOKEN) {
    $headers["Authorization"] = "token $env:GITHUB_TOKEN"
}

$release = Invoke-RestMethod -Uri "https://api.github.com/repos/$Repo/releases/latest" -Headers $headers

$tag = $release.tag_name
if (-not $tag) {
    Write-Host "오류: 릴리스를 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}

$asset = $release.assets | Where-Object { $_.name -like "*.jar" } | Select-Object -First 1
$downloadUrl = $asset.browser_download_url
$jarName = $asset.name

Write-Host "최신 버전: $tag"
Write-Host "다운로드:  $jarName"

# 기존 estherserver JAR 제거
$existing = Get-ChildItem -Path $ModsDir -Filter "estherserver-*.jar" -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "기존 JAR 제거:"
    foreach ($f in $existing) {
        Write-Host "  - $($f.Name)"
        Remove-Item $f.FullName -Force
    }
}

# 새 JAR 다운로드
Write-Host "다운로드 중..."
$destPath = Join-Path $ModsDir $jarName
Invoke-WebRequest -Uri $downloadUrl -OutFile $destPath -Headers $headers

Write-Host "완료: $destPath" -ForegroundColor Green
