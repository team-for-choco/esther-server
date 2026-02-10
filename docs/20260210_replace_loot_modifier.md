# 루트 모디파이어 Replace 방식 구현

## 작업 개요
테스트 생선이 기존 낚시 루트를 **대체(replace)** 하도록 변경. 기존 `AddItemLootModifier`는 루트에 아이템을 추가만 하므로, 기존 루트를 제거하고 지정 아이템으로 대체하는 `ReplaceItemLootModifier`를 새로 구현한다.

CHOCO-76

## 작업 전 요청사항
- 기존 `AddItemLootModifier`는 유지 (향후 다른 용도로 사용 가능)
- JSON 파일명(`add_test_fish.json`)은 유지 (향후 네이밍 리팩토링 시 일괄 변경)

## 작업 진행 중 결정된 사항등, 작업관련 주요 정보
- `ReplaceItemLootModifier`는 `AddItemLootModifier`와 동일 구조
- `doApply`에서 `generatedLoot.clear()` 후 `add()`하여 기존 루트를 대체
- 코덱 이름: `replace_item`

## 작업 체크리스트
- [x] `ReplaceItemLootModifier.kt` 생성
- [x] `ModLootModifiers.kt`에 `REPLACE_ITEM` 코덱 등록
- [x] `add_test_fish.json` type을 `estherserver:replace_item`으로 변경
- [x] `CustomFishTest.kt` 테스트 수정
- [x] `./gradlew build` 빌드 성공 확인

## 인게임 테스트
- 낚시 시 테스트 생선만 나오는지 확인 (기존 바닐라 낚시 루트가 대체되었는지)
