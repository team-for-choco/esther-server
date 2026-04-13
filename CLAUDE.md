# Esther Server - Minecraft Mod Project

## 프로젝트 개요
- **모드명**: Esther Server
- **Mod ID**: `estherserver`
- **작성자**: juyoung
- **패키지**: `com.juyoung.estherserver`

## 기술 스택
| 항목 | 버전 |
|------|------|
| Minecraft | 1.21.4 |
| NeoForge | 21.4.156 |
| Kotlin for Forge | 5.7.0 |
| Java | 21 |
| Gradle | 8.11 |

## 프로젝트 구조
```
mc/
├── src/main/
│   ├── kotlin/com/juyoung/estherserver/   # Kotlin 소스
│   ├── java/com/juyoung/estherserver/mixin/  # Mixin (Java)
│   ├── resources/assets/estherserver/     # 리소스
│   └── templates/                         # 메타데이터 템플릿
├── docs/                                  # 작업 기록
├── build.gradle
├── gradle.properties
└── CLAUDE.md
```

## 규칙

### Git 브랜치
- **main, develop 브랜치에 직접 커밋/머지 금지**
- 모든 새 작업은 **새 브랜치**에서 진행 (develop에서 분기)
- 작업 완료 후 **PR을 통해 develop에 머지**
- develop → main 머지는 패치 단위로 모아서 진행

#### 브랜치 네이밍
| 유형 | 패턴 | 용도 | 예시 |
|------|------|------|------|
| 기능 | `feature/CHOCO-{번호}` | 새 기능 개발 (JIRA 연동) | `feature/CHOCO-1` |
| 버그수정 | `fix/{설명}` | 버그 수정 | `fix/config-null-error` |
| 문서 | `docs/{설명}` | 문서 작업 | `docs/update-readme` |
| 유지보수 | `chore/{설명}` | 설정, 의존성, 정리 등 | `chore/update-gradle` |
| 논의/기획 | `draft/{설명}` | 다음 작업 논의, 기획 | `draft/plan-custom-items` |

#### 커밋 메시지
- **한글로 작성**
- JIRA 이슈 번호가 있으면 본문에 포함
- 예시:
  ```
  feat: 커스텀 생선 아이템 추가

  - 테스트 생선, 구운 테스트 생선 아이템 등록
  - 낚시 루트테이블 연동

  CHOCO-74
  ```

### Jira 연동
- Atlassian MCP를 통해 Jira에 직접 접근 가능
- **프로젝트**: CHOCO (`choco-system.atlassian.net`)
- **Cloud ID**: `55497d03-14a7-4a7b-a3f1-14b81384c4e8`

#### Jira 이슈 관리 원칙
- 기능 개발, 버그 수정 등 **코드 작업이 수반되는 작업은 반드시 Jira 이슈가 필요**
- 이슈가 없으면 **직접 생성**한 후 작업 시작
- 문서만 다루는 작업(docs, draft 브랜치)은 Jira 이슈 불필요

#### Jira 상태 전환
작업 진행에 따라 Jira 이슈 상태를 반드시 업데이트:

| 시점 | 상태 | 설명 |
|------|------|------|
| 작업 시작 | **진행 중** | 브랜치 생성 및 작업 착수 시 |
| 작업 완료 | **완료** | PR 생성 또는 main 머지 완료 시 |

### 코드 작성
- 모든 모드 코드는 **Kotlin**으로 작성
- **Mixin**은 반드시 **Java**로 작성 (Kotlin 컴파일러 호환성 문제)
- 패키지명: `com.juyoung.estherserver.*`

### 등급 체계 (색상)
| 등급 | 영문 | 메인 색상 | 비고 |
|------|------|-----------|------|
| 일반 | Common | 흰색 | |
| 고급 | Fine | 초록색 | |
| 희귀 | Rare | 시안(하늘색) | |
| 영웅 | Heroic | 보라색 | |
| 전설 | Legendary | 짙은 노란색 | |
| 유물 | Relic | 주황색 ~ 자홍색 | |
| 고대 | Ancient | 백금색 | |

### 문서화
- **작업 시작 시 문서부터 생성** (코드 작성 전)
- 파일명: `docs/YYYYMMDD_작업명.md` (예: `20260205_git_setup.md`)
- JIRA 이슈 번호가 있으면 문서에 포함

#### 문서 템플릿
```markdown
# 제목

## 작업 개요
TBD

## 작업 전 요청사항
TBD

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
TBD

## 작업 체크리스트
TBD

## 인게임 테스트
TBD
```

#### 작업 흐름
1. JIRA 이슈 확인 (없으면 **직접 생성**)
2. 브랜치 생성: `feature/CHOCO-{번호}` (develop에서 분기)
3. **Jira 상태 → 진행 중**으로 전환
4. **문서 생성**: `docs/YYYYMMDD_작업명.md`
5. 작업 진행 (문서 업데이트하며 진행)
6. 커밋 & Push
7. **PR 생성** → develop으로 머지 요청 (base: develop)
8. **머지 전 체크리스트** 수행 (아래 참고)
9. **사용자 확인 후 머지** — PR 생성까지만 진행하고, 머지는 반드시 사용자가 확인한 뒤 진행한다. 절대 자의적으로 머지하지 않는다.
10. **Jira 상태 → 완료**로 전환

#### 머지 전 체크리스트
PR을 develop에 머지하기 **전에** 반드시 다음 3가지를 완료해야 한다:

1. **Jira 완료 처리**: 해당 이슈 상태를 **완료**로 전환
2. **WORK_LOG.md 작업 기록 업데이트**: [docs/WORK_LOG.md](docs/WORK_LOG.md) 테이블에 해당 작업 행 추가
3. **ROADMAP.md 업데이트**: 완료된 작업 항목이 로드맵에 반영 (체크 표시, 흐름도 등)

### 인게임 테스트 추적
- 미완료 인게임 테스트는 **[docs/pending_ingame_tests.md](docs/pending_ingame_tests.md)** 에서 관리
- 각 작업 문서의 "인게임 테스트" 항목과 **중복으로 유지**
- 테스트 완료 시: 원본 문서에는 `[x]` 체크 표시, `pending_ingame_tests.md`에서는 해당 항목 **삭제**
- 새 작업에서 인게임 테스트 항목이 남으면 이 문서에도 추가

### 빌드
```bash
./gradlew build        # JAR 빌드
./gradlew runClient    # 클라이언트 실행
./gradlew runServer    # 서버 실행
```

### 실행 제한
- **`runClient`, `runServer` 등 게임 실행 명령은 절대 직접 실행하지 않는다**
- 빌드(`./gradlew build`)까지만 허용, 실행은 사용자가 직접 수행
- 오류 분석이 필요한 경우 사용자가 제공한 로그를 기반으로 진행
- 테스트 디바이스에서 로그를 `docs/data/`에 넣어 git push → 이쪽에서 git pull하여 분석
- 분석이 완료된 로그 파일은 삭제하여 저장소를 깨끗하게 유지 (단, `docs/data/` 폴더 자체는 유지 — `.gitkeep` 파일 보존)

## 작업 기록
전체 작업 기록은 **[docs/WORK_LOG.md](docs/WORK_LOG.md)** 참고.

## 로드맵
전체 로드맵은 **[docs/ROADMAP.md](docs/ROADMAP.md)** 참고.

## 알려진 이슈
| GitHub | 관련 JIRA | 제목 | 상태 |
|--------|-----------|------|------|
| [#3](https://github.com/team-for-choco/esther-server/issues/3) | CHOCO-74 | add_test_fish 루트 모디파이어 1.21.4 호환성 수정 | 해결 |

## 참고
- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)
