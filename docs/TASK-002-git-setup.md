# TASK-002: Git 저장소 연결

## 작업 정보
- **상태**: 완료
- **작성일**: 2026-02-05
- **작성자**: juyoung

## 목표
프로젝트를 GitHub 저장소에 연결하고 초기 커밋 푸시

## 작업 내역

1. **원격 저장소 변경**
   - 기존: `https://github.com/NeoForgeKotlinMDKs/MDK-1.21-ModDevGradle.git`
   - 변경: `https://github.com/team-for-choco/esther-server.git`

2. **.gitignore 업데이트**
   - `.DS_Store` 추가 (macOS)

3. **파일 스테이징**
   - 새 파일: CLAUDE.md, docs/, estherserver 소스
   - 삭제: example 템플릿 파일들
   - 수정: gradle.properties, .gitignore

4. **워크플로우 제거**
   - `.github/workflows/build.yml` 삭제
   - 이유: PAT에 workflow 권한 없음

5. **초기 커밋 및 푸시**
   - 커밋 메시지: `chore: Initialize Esther Server mod project`
   - 브랜치: `main`

## 결과

- **저장소**: https://github.com/team-for-choco/esther-server
- **커밋**: `c4cc750`

## 참고

향후 CI/CD가 필요하면 `.github/workflows/` 재설정 필요
