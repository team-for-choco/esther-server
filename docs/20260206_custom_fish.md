# 커스텀 생선 구현

## 작업 개요

Esther Server에 커스텀 생선 시스템 추가

- **JIRA**: CHOCO-74
- **상태**: 완료
- **작성일**: 2026-02-06
- **브랜치**: `feature/CHOCO-74`

## 작업 전 요청사항

- 낚시로 획득 가능한 커스텀 생선 아이템 추가
- 구운 생선 (음식) 제작 가능
- 바닐라 텍스처 재활용 (1차)
- 바닐라 스타일 효과 (일반 등급)

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 구현 방식
- **테스트 생선 (test_fish)**: 낚시로 90% 확률 획득 (테스트용 고확률)
- **구운 테스트 생선 (cooked_test_fish)**: 포만감 6, 포화도 0.8 (대구와 동일)
- **텍스처**: 바닐라 대구(cod) 텍스처 재활용
- **낚시 연동**: NeoForge Global Loot Modifier 사용

### 추가된 파일
```
src/main/kotlin/.../loot/
├── AddItemLootModifier.kt    # 루트 모디파이어
└── ModLootModifiers.kt       # 레지스트리

src/test/kotlin/.../
└── CustomFishTest.kt         # JUnit 테스트 (11 TC)

src/main/resources/
├── assets/estherserver/
│   ├── lang/ko_kr.json       # 한국어 번역 추가
│   └── models/item/
│       ├── test_fish.json
│       └── cooked_test_fish.json
└── data/
    ├── neoforge/loot_modifiers/global_loot_modifiers.json
    └── estherserver/
        ├── loot_modifiers/add_test_fish.json
        └── recipe/
            ├── cooked_test_fish_from_smelting.json
            ├── cooked_test_fish_from_smoking.json
            └── cooked_test_fish_from_campfire.json
```

## 작업 체크리스트

- [x] 커스텀 생선 아이템 등록
- [x] 낚시 루트테이블 연동
- [x] 구운 생선 아이템 등록
- [x] 제련/요리 레시피 추가
- [x] JUnit 테스트 작성 (11개 TC 통과)
- [x] 인게임 테스트

## 인게임 테스트 항목

### 1. 모드 로딩
- [x] 게임이 크래시 없이 정상 실행되는지 확인

### 2. 아이템 등록
- [x] `/give @s estherserver:test_fish` → 테스트 생선 획득
- [x] `/give @s estherserver:cooked_test_fish` → 구운 테스트 생선 획득
- [x] 아이템 이름이 한글로 표시되는지 확인

### 3. 크리에이티브 탭
- [x] 크리에이티브 모드에서 Esther 탭에 두 아이템이 표시되는지 확인

### 4. 낚시
- [x] 낚시했을 때 테스트 생선이 드랍되는지 확인 (여러 번 시도)

### 5. 요리 레시피
- [x] **화로**: 테스트 생선 → 구운 테스트 생선
- [x] **캠프파이어**: 테스트 생선 → 구운 테스트 생선
- [x] **훈연기**: 테스트 생선 → 구운 테스트 생선

### 6. 음식 섭취
- [x] 구운 테스트 생선을 먹을 수 있는지 확인 (허기 회복)

## 수정 이력

- **@JvmStatic 제거**: Kotlin object + @EventBusSubscriber에서 @JvmStatic 충돌로 런타임 크래시 → 제거로 해결
- **Client Item Definition 추가**: 1.21.4 아이템 모델 시스템 변경으로 `assets/estherserver/items/` JSON 파일 필요 → 추가로 텍스처 깨짐 해결
