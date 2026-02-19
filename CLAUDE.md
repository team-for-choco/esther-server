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
- **main 브랜치에 직접 커밋/머지 금지**
- 모든 새 작업은 **새 브랜치**에서 진행
- 작업 완료 후 **PR을 통해서만 main에 머지**

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
2. 브랜치 생성: `feature/CHOCO-{번호}` (main에서 분기)
3. **Jira 상태 → 진행 중**으로 전환
4. **문서 생성**: `docs/YYYYMMDD_작업명.md`
5. 작업 진행 (문서 업데이트하며 진행)
6. 커밋 & Push
7. **PR 생성** → main으로 머지 요청
8. 리뷰 후 **머지 전 체크리스트** 수행 (아래 참고)
9. 머지
10. **Jira 상태 → 완료**로 전환

#### 머지 전 체크리스트
PR을 main에 머지하기 **전에** 반드시 다음 3가지를 완료해야 한다:

1. **Jira 완료 처리**: 해당 이슈 상태를 **완료**로 전환
2. **CLAUDE.md 작업 기록 업데이트**: 작업 기록 테이블에 해당 작업 행 추가
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
| 날짜 | 문서 | JIRA | 제목 | 상태 |
|------|------|------|------|------|
| 2026-02-05 | [20260205_project_setup.md](docs/20260205_project_setup.md) | - | 프로젝트 초기 설정 | 완료 |
| 2026-02-05 | [20260205_git_setup.md](docs/20260205_git_setup.md) | - | Git 저장소 연결 | 완료 |
| 2026-02-05 | [20260205_plan_server_features.md](docs/20260205_plan_server_features.md) | - | 서버 기능 기획 | 완료 |
| 2026-02-06 | [20260206_custom_fish.md](docs/20260206_custom_fish.md) | CHOCO-74 | 커스텀 생선 구현 | 완료 |
| 2026-02-07 | [20260207_custom_crop.md](docs/20260207_custom_crop.md) | CHOCO-75 | 커스텀 작물 구현 | 완료 |
| 2026-02-07 | [20260207_fix_loot_modifier.md](docs/20260207_fix_loot_modifier.md) | #3 | 루트 모디파이어 조건 타입 수정 | 완료 |
| 2026-02-10 | [20260210_replace_loot_modifier.md](docs/20260210_replace_loot_modifier.md) | CHOCO-76 | 루트 모디파이어 replace 방식 구현 | 완료 |
| 2026-02-10 | [20260210_custom_ore.md](docs/20260210_custom_ore.md) | CHOCO-77 | 커스텀 광물 + 월드젠 구현 | 완료 |
| 2026-02-11 | [20260211_item_quality_system.md](docs/20260211_item_quality_system.md) | CHOCO-78 | 아이템 등급 시스템 (Phase 1) | 완료 |
| 2026-02-11 | [20260211_korean_crops.md](docs/20260211_korean_crops.md) | CHOCO-79 | 한국식 작물 추가 (쌀, 고추, 시금치) | 완료 |
| 2026-02-11 | [20260211_cleanup_phase1.md](docs/20260211_cleanup_phase1.md) | CHOCO-80 | Phase 1 코드 정리 및 리팩토링 | 완료 |
| 2026-02-12 | [20260212_sleeping_system.md](docs/20260212_sleeping_system.md) | CHOCO-81 | 슬리핑 시스템 구현 | 완료 |
| 2026-02-13 | [20260213_daylight_extension.md](docs/20260213_daylight_extension.md) | CHOCO-82 | 낮 시간 연장 | 완료 |
| 2026-02-13 | [20260213_sitting_system.md](docs/20260213_sitting_system.md) | CHOCO-83 | 앉기 기능 | 완료 |
| 2026-02-13 | [20260213_cooking_system.md](docs/20260213_cooking_system.md) | CHOCO-84 | 요리 시스템 (Phase 2-1) | 완료 |
| 2026-02-13 | [20260213_collection_system.md](docs/20260213_collection_system.md) | CHOCO-85 | 도감 시스템 Phase A (Phase 2-2) | 완료 |
| 2026-02-13 | [20260213_collection_system.md](docs/20260213_collection_system.md) | CHOCO-86 | 도감 시스템 Phase B: 마일스톤 + 칭호 | 완료 |
| 2026-02-13 | [20260213_chunk_claim.md](docs/20260213_chunk_claim.md) | CHOCO-87 | 청크 클레임 기본 구현 (Phase 2.5-1) | 완료 |
| 2026-02-14 | [20260214_cooking_multiplayer_title_fix.md](docs/20260214_cooking_multiplayer_title_fix.md) | CHOCO-88 | 요리 멀티플레이어 개선 + 칭호 버그/기능 | 완료 |
| 2026-02-14 | [20260214_deploy_automation.md](docs/20260214_deploy_automation.md) | CHOCO-89 | 모드 배포 자동화 (Prism Launcher + packwiz) | 완료 |
| 2026-02-14 | [20260214_claim_permissions.md](docs/20260214_claim_permissions.md) | CHOCO-90 | 청크 클레임 행동 제한 설정 | 완료 |
| 2026-02-14 | [20260214_claim_trust.md](docs/20260214_claim_trust.md) | CHOCO-91 | 청크 클레임 신뢰 시스템 (플레이어 초대) | 완료 |
| 2026-02-19 | [20260219_currency_system.md](docs/20260219_currency_system.md) | CHOCO-92 | 화폐 시스템 (Phase 3-1) | 완료 |
| 2026-02-19 | [20260219_npc_merchant.md](docs/20260219_npc_merchant.md) | CHOCO-93 | NPC 상인 시스템 (Phase 3-3) | 완료 |

## 로드맵
전체 로드맵은 **[docs/ROADMAP.md](docs/ROADMAP.md)** 참고.

## 알려진 이슈
| GitHub | 관련 JIRA | 제목 | 상태 |
|--------|-----------|------|------|
| [#3](https://github.com/team-for-choco/esther-server/issues/3) | CHOCO-74 | add_test_fish 루트 모디파이어 1.21.4 호환성 수정 | 해결 |

## 참고
- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)
