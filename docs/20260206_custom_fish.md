# 커스텀 생선 구현

## 작업 개요

Esther Server에 커스텀 생선 시스템 추가

- **JIRA**: CHOCO-74
- **상태**: 진행 중
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
- [ ] 인게임 테스트
