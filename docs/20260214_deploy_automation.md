# 모드 배포 자동화 (Prism Launcher + packwiz)

## 작업 개요
GitHub Actions + packwiz + Prism Launcher를 이용한 모드 자동 배포 파이프라인 구축.
모드 업데이트마다 서버/클라이언트 양쪽 JAR을 수동 교체하는 것이 불편하여 자동화.

- JIRA: CHOCO-89

## 작업 전 요청사항
- esther-modpack 레포를 public으로 생성 (team-for-choco/esther-modpack)
- GitHub PAT 생성 (modpack 레포 write 권한) → esther-server Secrets에 `MODPACK_PAT`으로 등록
- esther-modpack 레포에서 GitHub Pages 활성화 (Source: GitHub Actions)

## 작업관련 주요 정보

### 아키텍처
```
[esther-server (public)]           [esther-modpack (public)]
  소스 코드                           pack.toml, index.toml
  GitHub Actions:                     mods/estherserver.pw.toml
    빌드 → Release 생성                mods/kotlin-for-forge.pw.toml
    → modpack 레포에 dispatch          GitHub Actions:
                                        packwiz 업데이트 → GitHub Pages 배포

[서버 컴퓨터 (Windows)]             [플레이어 PC]
  update-server.ps1 실행              Prism Launcher 실행
  → 최신 JAR 다운로드                  → packwiz-installer-bootstrap 자동 실행
                                       → 최신 모드 다운로드
```

### 전체 업데이트 플로우
1. 코드 수정 → main에 머지
2. GitHub Actions 자동: 빌드 → Release 생성 → modpack 레포에 dispatch → Pages 배포
3. **서버**: `.\update-server.ps1 -ModsDir "...\mods" -Token "ghp_xxxx"` 실행
4. **플레이어**: Prism Launcher에서 게임 실행 (자동 업데이트)

### 버전 관리
- mod_version은 `gradle.properties`의 `mod_version`에서 관리
- Release 태그: `v{mod_version}` (예: v1.0.0)
- JAR 파일명: `estherserver-{mod_version}.jar`
- 모드 코드 변경 시 반드시 `mod_version`을 올려야 새 Release가 생성됨

### 필요한 Secrets / 토큰
| 이름 | 위치 | 용도 |
|------|------|------|
| `MODPACK_PAT` | esther-server 레포 Secrets | modpack 레포에 repository_dispatch 전송 (fine-grained, esther-modpack Contents R/W) |
| 서버 업데이트용 PAT | 서버 컴퓨터 | Release 에셋 다운로드 (fine-grained, esther-server Contents Read) |

### 주요 파일
| 레포 | 파일 | 역할 |
|------|------|------|
| esther-server | `.github/workflows/release.yml` | 빌드 → Release → modpack dispatch |
| esther-server | `scripts/update-server.ps1` | Windows 서버 업데이트 스크립트 |
| esther-server | `scripts/update-server.sh` | Linux/Mac 서버 업데이트 스크립트 |
| esther-modpack | `pack.toml` | packwiz 팩 메타데이터 |
| esther-modpack | `index.toml` | 파일 인덱스 (packwiz refresh로 자동 갱신) |
| esther-modpack | `mods/*.pw.toml` | 모드 정의 (URL, 해시) |
| esther-modpack | `.packwizignore` | 팩 인덱스에서 제외할 파일 (.github, docs 등) |
| esther-modpack | `.github/workflows/update-and-deploy.yml` | packwiz 업데이트 → Pages 배포 |
| esther-modpack | `docs/PLAYER_SETUP.md` | 플레이어 설정 가이드 |

## 작업 중 발생한 이슈 및 해결

### 1. private 레포 Release 다운로드 404
- **원인**: private 레포의 `browser_download_url`은 인증 없이 접근 불가 (GitHub 로그인 페이지로 리다이렉트)
- **해결**: API URL + `Accept: application/octet-stream` 헤더로 다운로드 방식 변경
- **추가 조치**: esther-server 레포를 public으로 전환하여 클라이언트 다운로드 문제도 해결

### 2. packwiz가 .github 폴더를 팩 인덱스에 포함
- **원인**: `packwiz refresh`가 레포의 모든 파일을 인덱싱 → `.github/workflows/*.yml`도 포함
- **증상**: packwiz-installer가 GitHub Pages에서 `.github/workflows/*.yml`을 다운로드 시도 → 404
- **해결**: `.packwizignore` 파일 추가하여 `.github/`, `docs/` 등 제외

### 3. packwiz-installer vs packwiz-installer-bootstrap
- **증상**: `packwiz-installer.jar`를 직접 실행하면 "must be run through packwiz-installer-bootstrap" 오류
- **해결**: `packwiz-installer-bootstrap.jar`를 대신 사용 (bootstrap이 installer를 자동 다운로드/실행)

### 4. Release 재생성 필요 (private → public 전환 시)
- **원인**: private 시절에 생성된 Release 에셋은 public 전환 후에도 접근 불가
- **해결**: 기존 Release + 태그 삭제 후 빈 커밋으로 Actions 재트리거

## 작업 체크리스트
- [x] esther-server: `.github/workflows/release.yml` 생성
- [x] esther-server: `scripts/update-server.ps1` 생성 (Windows)
- [x] esther-server: `scripts/update-server.sh` 생성 (Linux/Mac)
- [x] esther-modpack 레포 생성 (GitHub, public)
- [x] esther-modpack: pack.toml, index.toml 생성
- [x] esther-modpack: mods/estherserver.pw.toml, kotlin-for-forge.pw.toml 생성
- [x] esther-modpack: `.packwizignore` 추가
- [x] esther-modpack: `.github/workflows/update-and-deploy.yml` 생성
- [x] esther-modpack: `docs/PLAYER_SETUP.md` 생성
- [x] GitHub Secrets 등록 (`MODPACK_PAT`)
- [x] GitHub Pages 활성화
- [x] esther-server 레포 public 전환
- [x] 클라이언트 실행 테스트 완료

## 인게임 테스트
- Prism Launcher에서 packwiz-installer-bootstrap을 통한 모드 자동 다운로드 확인
- 서버 update-server.ps1 스크립트로 최신 JAR 다운로드 확인
