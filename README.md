# Esther Server

Minecraft 서버용 모드 프로젝트

## 프로젝트 정보

| 항목 | 내용 |
|------|------|
| 모드 ID | `estherserver` |
| Minecraft | 1.21.4 |
| NeoForge | 21.4.156 |
| 언어 | Kotlin |
| 작성자 | juyoung |

## 시작하기

### 요구사항

- Java 21 이상
- IntelliJ IDEA (권장)

### 빌드

```bash
./gradlew build
```

빌드 결과물: `build/libs/estherserver-<version>.jar`

### 실행

```bash
# 클라이언트 실행
./gradlew runClient

# 서버 실행
./gradlew runServer
```

## 프로젝트 구조

```
src/main/
├── kotlin/com/juyoung/estherserver/   # Kotlin 소스 코드
├── java/com/juyoung/estherserver/mixin/  # Mixin (Java)
├── resources/assets/estherserver/     # 리소스 파일
└── templates/                         # 메타데이터 템플릿
```

## 개발 가이드

### Kotlin vs Java

- 일반 모드 코드: **Kotlin** 사용
- Mixin: **Java** 사용 (필수)

> Mixin은 바이트코드 수준에서 동작하므로 Kotlin 컴파일러와 호환성 문제가 있습니다.

### 문서

모든 작업은 `docs/` 폴더에 기록됩니다.

## 참고 자료

- [NeoForge 공식 문서](https://docs.neoforged.net/)
- [Kotlin for Forge](https://github.com/thedarkcolour/KotlinForForge)
- [NeoForge Discord](https://discord.neoforged.net/)

## 라이선스

All Rights Reserved
