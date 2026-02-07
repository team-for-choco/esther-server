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
```

#### 작업 흐름
1. JIRA 이슈 번호 확인
2. 브랜치 생성: `feature/CHOCO-{번호}` (main에서 분기)
3. **문서 생성**: `docs/YYYYMMDD_작업명.md`
4. 작업 진행 (문서 업데이트하며 진행)
5. 커밋 & Push
6. **PR 생성** → main으로 머지 요청
7. 리뷰 후 머지

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

## 알려진 이슈
| GitHub | 관련 JIRA | 제목 | 상태 |
|--------|-----------|------|------|
| [#3](https://github.com/team-for-choco/esther-server/issues/3) | CHOCO-74 | add_test_fish 루트 모디파이어 1.21.4 호환성 수정 | 미해결 |

## 참고
- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)
