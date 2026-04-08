# 바다의 행운 인챈트 커스텀 어종 확률 반영

## 작업 개요
특수 낚싯대 사용 시 바다의 행운(Luck of the Sea) 인챈트 레벨이 커스텀 어종 선택 가중치에 반영되도록 수정.

## 작업 전 요청사항
- 바다의 행운 인챈트가 커스텀 어종 선택에 영향을 줘야 함

## 작업 진행 중 결정된 사항

### 기존 문제
`WeightedFishLootModifier.selectRandomFish()`는 고정된 가중치(weight)만 사용하고,
`context.luck` 값을 전혀 참조하지 않았음.

### 적용 공식
바다의 행운 레벨(luck)에 따라 등급별 가중치 배율 적용:

| 등급 | 배율 |
|------|------|
| COMMON | 변동 없음 (×1.0) |
| ADVANCED | ×(1 + luck × 0.5) |
| RARE | ×(1 + luck × 1.0) |

**Luck 3 기준 예시:**
- ADVANCED: 기본 대비 2.5배
- RARE: 기본 대비 4.0배

### 변경 파일
- `src/main/kotlin/com/juyoung/estherserver/loot/WeightedFishLootModifier.kt`
  - `doApply()`: `context.luck` 추출 후 `selectRandomFish()`에 전달
  - `selectRandomFish()`: luck + 등급 기반 effectiveWeight 계산 후 추첨

## 작업 체크리스트
- [x] `WeightedFishLootModifier` 수정
- [x] 빌드 성공 확인
- [ ] 인게임 테스트 (바다의 행운 인챈트 후 어종 확률 변화 확인)

## 인게임 테스트
- [ ] 바다의 행운 없이 낚시 → RARE 어종 출현 빈도 기록
- [ ] 바다의 행운 Ⅲ 적용 후 낚시 → RARE 어종 출현 빈도 비교
