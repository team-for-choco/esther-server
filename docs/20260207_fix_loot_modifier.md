# add_test_fish 글로벌 루트 모디파이어 1.21.4 호환성 수정

## 작업 개요
GitHub Issue #3: `add_test_fish` 글로벌 루트 모디파이어의 `loot_table_id` 조건 타입 네임스페이스를 수정한다.

관련 이슈: [GitHub #3](https://github.com/team-for-choco/esther-server/issues/3), CHOCO-74

## 작업 전 요청사항
- `loot_table_id` 조건은 바닐라 Minecraft가 아닌 NeoForge가 제공하는 조건 타입
- `minecraft:loot_table_id` → `neoforge:loot_table_id`로 변경 필요

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- 1차 수정: `minecraft:loot_table_id` → `neoforge:loot_table_id` (네임스페이스 수정)
- 2차 수정: `minecraft:gameplay/fishing/fish` → `minecraft:gameplay/fishing` (루트 테이블 경로 수정)
  - 원인: 글로벌 루트 모디파이어의 `loot_table_id` 조건은 **최상위 루트 테이블**과 매칭됨
  - `minecraft:gameplay/fishing/fish`는 내부 서브 테이블이라 조건에 매칭되지 않음
  - 실제 낚시 시 호출되는 최상위 테이블은 `minecraft:gameplay/fishing`
- 수정 대상 파일 2개:
  1. `src/main/resources/data/estherserver/loot_modifiers/add_test_fish.json`
  2. `src/test/kotlin/com/juyoung/estherserver/CustomFishTest.kt`

## 작업 체크리스트
- [x] 브랜치 생성: `fix/loot-modifier-condition-type`
- [x] 문서 생성
- [x] JSON 수정: `minecraft:loot_table_id` → `neoforge:loot_table_id`
- [x] 테스트 수정: 동일 변경
- [x] 빌드 확인
- [x] 커밋 & PR 생성: https://github.com/team-for-choco/esther-server/pull/5

## 인게임 테스트
- [x] 낚시 시 테스트 생선이 드롭되는지 확인
- [x] 블록 파괴 시 테스트 생선이 드롭되지 않는지 확인
- [x] 테스트 생선 드롭 확률이 적절한지 확인 (90%)

## 추가 작업 (미정)
- 현재 테스트 생선이 기존 생선과 **함께** 드롭됨 (add 방식)
- 향후 테스트 생선이 낚이면 기존 생선을 **대체**하도록 변경 필요 (replace 방식)
  - `doApply`에서 `generatedLoot.clear()` 후 `add()` 하면 구현 가능
  - `AddItemLootModifier`에 `replace` 필드를 추가하거나 별도 `ReplaceItemLootModifier` 생성 검토
