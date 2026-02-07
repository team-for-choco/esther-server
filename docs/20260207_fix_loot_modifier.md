# add_test_fish 글로벌 루트 모디파이어 1.21.4 호환성 수정

## 작업 개요
GitHub Issue #3: `add_test_fish` 글로벌 루트 모디파이어의 `loot_table_id` 조건 타입 네임스페이스를 수정한다.

관련 이슈: [GitHub #3](https://github.com/team-for-choco/esther-server/issues/3), CHOCO-74

## 작업 전 요청사항
- `loot_table_id` 조건은 바닐라 Minecraft가 아닌 NeoForge가 제공하는 조건 타입
- `minecraft:loot_table_id` → `neoforge:loot_table_id`로 변경 필요

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- 수정 대상 파일 2개:
  1. `src/main/resources/data/estherserver/loot_modifiers/add_test_fish.json` (9행)
  2. `src/test/kotlin/com/juyoung/estherserver/CustomFishTest.kt` (144행)

## 작업 체크리스트
- [x] 브랜치 생성: `fix/loot-modifier-condition-type`
- [x] 문서 생성
- [x] JSON 수정: `minecraft:loot_table_id` → `neoforge:loot_table_id`
- [x] 테스트 수정: 동일 변경
- [x] 빌드 확인
- [ ] 커밋 & PR 생성
