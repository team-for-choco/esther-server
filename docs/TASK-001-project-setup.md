# TASK-001: NeoForge Kotlin 모드 프로젝트 초기 설정

## 작업 정보
- **상태**: 완료
- **작성일**: 2026-02-05
- **작성자**: juyoung

## 목표
Minecraft 모드 개발을 위한 NeoForge + Kotlin 프로젝트 환경 구축

## 결정 사항

| 항목 | 선택 | 이유 |
|------|------|------|
| Minecraft 버전 | 1.21.4 | 1.21.11은 베타만 존재, 1.21.4가 안정 버전 |
| 모드 로더 | NeoForge | Forge의 후속, 대형 모드팩에 적합 |
| 언어 | Kotlin | Java보다 간결, null 안전성 |
| KFF 버전 | 5.7.0 | 1.21.4 호환 |

## 프로젝트 정보

| 항목 | 값 |
|------|-----|
| Mod ID | `estherserver` |
| Mod Name | `Esther Server` |
| Package | `com.juyoung.estherserver` |
| Author | `juyoung` |
| License | All Rights Reserved |

## 버전 정보

```properties
minecraft_version=1.21.4
neo_version=21.4.156
kff_version=5.7.0
java_version=21
```

## 프로젝트 구조

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

## 작업 내역

1. **템플릿 클론**
   - NeoForgeKotlinMDKs/MDK-1.21-ModDevGradle 클론

2. **버전 설정 시도**
   - 최초 1.21.11 시도 → 베타 버전 빌드 오류 발생
   - 1.21.4 안정 버전으로 변경

3. **gradle.properties 수정**
   - Minecraft/NeoForge/KFF 버전 설정
   - 모드 ID, 이름, 작성자 정보 입력

4. **소스 코드 마이그레이션**
   - 패키지명 변경: `com.example.examplemod` → `com.juyoung.estherserver`
   - 클래스명 변경: `ExampleMod` → `EstherServerMod`
   - Mixin 클래스 업데이트

5. **API 호환성 수정**
   - `Config.kt`의 `BuiltInRegistries.ITEM` API 변경 대응

6. **빌드 확인**
   - `./gradlew build` 성공
   - 출력: `build/libs/estherserver-1.0.0.jar` (16KB)

## 유용한 명령어

```bash
./gradlew runClient    # 클라이언트 실행
./gradlew runServer    # 서버 실행
./gradlew build        # JAR 빌드
./gradlew clean        # 빌드 정리
```

## 참고 자료

- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge GitHub](https://github.com/thedarkcolour/KotlinForForge)
- [NeoForge Kotlin MDK](https://github.com/NeoForgeKotlinMDKs)

## 다음 단계

- [ ] 모드 기능 정의
- [ ] 커스텀 아이템/블록 추가
- [ ] 테스트 실행
