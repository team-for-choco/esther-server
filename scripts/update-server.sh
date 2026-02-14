#!/usr/bin/env bash
# =============================================================================
# update-server.sh - Esther Server 모드 업데이트 스크립트
#
# 사용법:
#   ./update-server.sh /path/to/server/mods
#
# GitHub API를 통해 최신 릴리스의 JAR을 다운로드하여 서버 mods 폴더에 배치합니다.
# 기존 estherserver JAR은 자동으로 제거됩니다.
# =============================================================================

set -euo pipefail

REPO="team-for-choco/esther-server"

if [[ $# -lt 1 ]]; then
  echo "사용법: $0 <mods 폴더 경로>"
  echo "예시:  $0 /home/minecraft/server/mods"
  exit 1
fi

MODS_DIR="$1"

if [[ ! -d "$MODS_DIR" ]]; then
  echo "오류: '$MODS_DIR' 폴더가 존재하지 않습니다."
  exit 1
fi

echo "최신 릴리스 확인 중..."

RELEASE_JSON=$(curl -sL "https://api.github.com/repos/$REPO/releases/latest")

TAG=$(echo "$RELEASE_JSON" | grep -m1 '"tag_name"' | cut -d'"' -f4)
if [[ -z "$TAG" ]]; then
  echo "오류: 릴리스를 찾을 수 없습니다. 레포가 private이면 GITHUB_TOKEN 환경변수를 설정하세요."
  exit 1
fi

DOWNLOAD_URL=$(echo "$RELEASE_JSON" | grep '"browser_download_url"' | grep '\.jar"' | head -1 | cut -d'"' -f4)
JAR_NAME=$(basename "$DOWNLOAD_URL")

echo "최신 버전: $TAG"
echo "다운로드:  $JAR_NAME"

# 기존 estherserver JAR 제거
EXISTING=$(find "$MODS_DIR" -maxdepth 1 -name "estherserver-*.jar" 2>/dev/null || true)
if [[ -n "$EXISTING" ]]; then
  echo "기존 JAR 제거:"
  echo "$EXISTING" | while read -r f; do
    echo "  - $(basename "$f")"
    rm -f "$f"
  done
fi

# 새 JAR 다운로드
echo "다운로드 중..."
CURL_OPTS=(-sL -o "$MODS_DIR/$JAR_NAME")
if [[ -n "${GITHUB_TOKEN:-}" ]]; then
  CURL_OPTS+=(-H "Authorization: token $GITHUB_TOKEN")
fi
curl "${CURL_OPTS[@]}" "$DOWNLOAD_URL"

echo "완료: $MODS_DIR/$JAR_NAME"
