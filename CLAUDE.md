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
- 모든 새 작업은 **새 브랜치**에서 진행
- 브랜치 네이밍: `feature/CHOCO-{번호}`
- 번호는 JIRA 이슈 번호와 매칭 (수동 관리)
- 예시: `feature/CHOCO-1`, `feature/CHOCO-2`

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
2. 브랜치 생성: `feature/CHOCO-{번호}`
3. **문서 생성**: `docs/YYYYMMDD_작업명.md`
4. 작업 진행 (문서 업데이트하며 진행)
5. 커밋 & PR

### 빌드
```bash
./gradlew build        # JAR 빌드
./gradlew runClient    # 클라이언트 실행
./gradlew runServer    # 서버 실행
```

## 작업 기록
| 날짜 | 문서 | JIRA | 제목 | 상태 |
|------|------|------|------|------|
| 2026-02-05 | [TASK-001](docs/TASK-001-project-setup.md) | - | 프로젝트 초기 설정 | 완료 |
| 2026-02-05 | [TASK-002](docs/TASK-002-git-setup.md) | - | Git 저장소 연결 | 완료 |

## 참고
- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)
