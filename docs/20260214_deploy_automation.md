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
[esther-server (private)]          [esther-modpack (public, 신규)]
  소스 코드                           pack.toml, index.toml
  GitHub Actions:                     mods/estherserver.pw.toml
    빌드 → Release 생성                mods/kotlin-for-forge.pw.toml
    → modpack 레포에 dispatch          GitHub Actions:
                                        packwiz 업데이트 → GitHub Pages 배포

[서버 컴퓨터]                        [플레이어 PC]
  update-server.sh 실행               Prism Launcher 실행
  → 최신 JAR 다운로드                  → packwiz-installer 자동 실행
                                       → 최신 모드 다운로드
```

### 버전 관리
- mod_version은 `gradle.properties`의 `mod_version`에서 관리
- Release 태그: `v{mod_version}` (예: v1.0.0)
- JAR 파일명: `estherserver-{mod_version}.jar`

### 필요한 Secrets
| Secret 이름 | 설명 |
|-------------|------|
| `MODPACK_PAT` | esther-modpack 레포에 repository_dispatch를 보낼 수 있는 PAT |

## 작업 체크리스트
- [ ] esther-server: `.github/workflows/release.yml` 생성
- [ ] esther-server: `scripts/update-server.sh` 생성
- [ ] esther-modpack 레포 생성 (GitHub)
- [ ] esther-modpack: pack.toml, index.toml 생성
- [ ] esther-modpack: mods/estherserver.pw.toml 생성
- [ ] esther-modpack: mods/kotlin-for-forge.pw.toml 생성
- [ ] esther-modpack: `.github/workflows/update-and-deploy.yml` 생성
- [ ] esther-modpack: `docs/PLAYER_SETUP.md` 생성

## 인게임 테스트
- 해당 없음 (CI/CD 파이프라인이므로 GitHub Actions에서 확인)
