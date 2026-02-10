# 아이템 등급 시스템 (Phase 1)

## 작업 개요
아이템에 등급(일반/고급/희귀)을 부여하는 시스템 구현. Phase 1에서는 시각적 표시(이름 색상 + 툴팁)만 구현.

- JIRA: CHOCO-78

## 작업 전 요청사항
- 1.21.4 DataComponents API 사용
- 등급: 일반(Common 70%)/고급(Fine 25%)/희귀(Rare 5%)
- 등급에 따른 이름 색상: 흰색/초록/파랑
- 글로벌 루트 모디파이어로 태그 기반 등급 부여

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보

### 등급 설계
| 등급 | 색상 | 확률 | 비고 |
|------|------|------|------|
| 일반 (Common) | WHITE | 70% | 이름 색상 변경 없음 |
| 고급 (Fine) | GREEN | 25% | |
| 희귀 (Rare) | BLUE | 5% | |

### 대상 아이템
- test_fish, cooked_test_fish
- test_harvest, cooked_test_harvest
- test_ore_raw, test_ore_ingot

### 제외 아이템
- test_seeds, 블록 아이템, example 아이템

### 아키텍처
1. `ItemQuality` enum — Codec/StreamCodec 포함
2. `ModDataComponents` — `estherserver:item_quality` DataComponentType 등록
3. `has_quality` 아이템 태그 — 등급 대상 아이템 관리
4. `AssignQualityLootModifier` — 글로벌 루트 모디파이어
5. `ClientGameEvents` — ItemTooltipEvent로 이름 색상 + 등급 툴팁

### 주의사항
- 등급이 다른 아이템은 자동으로 스택 불가 (DataComponent 차이)
- 화로 제련 시 등급 미상속 (Phase 1 허용)
- COMMON 아이템은 이름 색상 그대로 (기본 흰색)

## 작업 체크리스트
- [x] ItemQuality enum 생성
- [x] ModDataComponents 등록
- [x] AssignQualityLootModifier 구현
- [x] ModLootModifiers에 ASSIGN_QUALITY 등록
- [x] EstherServerMod에 DataComponents 등록 + 툴팁 이벤트
- [x] has_quality 아이템 태그 JSON
- [x] assign_quality 루트 모디파이어 JSON
- [x] global_loot_modifiers.json 업데이트
- [x] 언어 파일 업데이트 (en_us, ko_kr)
- [x] ItemQualityTest 작성
- [x] 빌드 성공 확인

## 인게임 테스트
- [x] 낚시 시 등급 부여 확인 (일반/고급 확인, 희귀는 5% 확률로 미출현 — 정상 동작 판단)
- [x] 작물 수확 시 등급 부여 확인 (3등급 모두 확인)
- [x] 광석 채굴 시 등급 부여 확인 (3등급 모두 확인)
- [x] 등급별 이름 색상 확인 (일반=흰색, 고급=초록, 희귀=파랑)
- [x] 등급 툴팁 표시 확인
- [x] 다른 등급 아이템 스택 불가 확인
