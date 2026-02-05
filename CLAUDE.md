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

### 코드 작성
- 모든 모드 코드는 **Kotlin**으로 작성
- **Mixin**은 반드시 **Java**로 작성 (Kotlin 컴파일러 호환성 문제)
- 패키지명: `com.juyoung.estherserver.*`

### 문서화
- 모든 작업은 `docs/TASK-XXX-제목.md` 형식으로 기록
- 작업 번호는 순차적으로 증가 (001, 002, 003...)

### 빌드
```bash
./gradlew build        # JAR 빌드
./gradlew runClient    # 클라이언트 실행
./gradlew runServer    # 서버 실행
```

## 작업 기록
| Task | 제목 | 상태 |
|------|------|------|
| [TASK-001](docs/TASK-001-project-setup.md) | NeoForge Kotlin 모드 프로젝트 초기 설정 | 완료 |
| [TASK-002](docs/TASK-002-git-setup.md) | Git 저장소 연결 | 완료 |

## 참고
- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)
