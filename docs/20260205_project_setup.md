# NeoForge Kotlin 모드 프로젝트 초기 설정

## 작업 개요

Minecraft 모드 개발을 위한 NeoForge + Kotlin 프로젝트 환경 구축

- **상태**: 완료
- **작성일**: 2026-02-05
- **작성자**: juyoung

## 작업 전 요청사항

- Minecraft 모드 제작 환경 세팅
- 최신 안정 버전 사용
- Kotlin 언어 사용

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 버전 선택

| 항목 | 선택 | 이유 |
|------|------|------|
| Minecraft 버전 | 1.21.4 | 1.21.11은 베타만 존재, 1.21.4가 안정 버전 |
| 모드 로더 | NeoForge | Forge의 후속, 대형 모드팩에 적합 |
| 언어 | Kotlin | Java보다 간결, null 안전성 |
| KFF 버전 | 5.7.0 | 1.21.4 호환 |

### 프로젝트 정보

| 항목 | 값 |
|------|-----|
| Mod ID | `estherserver` |
| Mod Name | `Esther Server` |
| Package | `com.juyoung.estherserver` |
| Author | `juyoung` |
| License | All Rights Reserved |

### 버전 정보

```properties
minecraft_version=1.21.4
neo_version=21.4.156
kff_version=5.7.0
java_version=21
```

### 프로젝트 구조

```
mc/
├── src/main/
│   ├── kotlin/com/juyoung/estherserver/
│   │   ├── EstherServerMod.kt    # 메인 모드 클래스
│   │   └── Config.kt             # 설정 클래스
│   ├── java/com/juyoung/estherserver/mixin/
│   │   └── EstherServerMixin.java # Mixin (Java 필수)
│   ├── resources/assets/estherserver/lang/
│   │   └── en_us.json            # 언어 파일
│   └── templates/
│       ├── META-INF/neoforge.mods.toml  # 모드 메타데이터
│       └── estherserver.mixins.json     # Mixin 설정
├── build.gradle                  # Gradle 빌드 설정
├── gradle.properties             # 프로젝트 속성
└── docs/                         # 문서
```

## 작업 체크리스트

- [x] 템플릿 클론 (NeoForgeKotlinMDKs/MDK-1.21-ModDevGradle)
- [x] 버전 설정 (1.21.11 시도 → 1.21.4로 변경)
- [x] gradle.properties 수정
- [x] 소스 코드 마이그레이션 (`com.example.examplemod` → `com.juyoung.estherserver`)
- [x] API 호환성 수정 (`Config.kt`의 `BuiltInRegistries.ITEM` API)
- [x] 빌드 확인 (`./gradlew build` 성공)

## 참고 자료

- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge GitHub](https://github.com/thedarkcolour/KotlinForForge)
- [NeoForge Kotlin MDK](https://github.com/NeoForgeKotlinMDKs)
